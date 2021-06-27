package com.iamvishnu.compose.gameoflife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iamvishnu.compose.gameoflife.ui.theme.ComposeGameOfLifeTheme
import kotlinx.coroutines.delay

const val CELL_SIZE_DP = 15

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeGameOfLifeTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {

                    val widthPixels = LocalContext.current.resources.displayMetrics.widthPixels
                    val oneBlock = CELL_SIZE_DP * LocalContext.current.resources.displayMetrics.density

                    val cols = (widthPixels / oneBlock).toInt()
                    val rows = (cols * 1.25).toInt()

                    GameBoard(rows = rows, cols = cols)
                }
            }
        }
    }
}

class Matrix<T>(
    val rows: Int,
    val cols: Int,
    private val init: (Int, Int) -> T
){

    // This is a row-major representation only.
    private val _values = mutableListOf<T>().apply {
        repeat(rows * cols) { i ->
            val r = i / cols
            add(init(r, i % cols))
        }
    }

    operator fun get(row: Int, col: Int): T {
        return _values[row * cols + col]
    }

    operator fun set(row: Int, col: Int, v: T) {
        _values[row * cols + col] = v
    }

    fun count(f: (T) -> Boolean): Int {
        return _values.count { f(it) }
    }

}

data class GameState(
    val isPaused: Boolean = false,
    val generation: Int = 0,
    val cells: Matrix<Boolean>, // Row major list.
){
    val population: Int by lazy {
        cells.count { it }
    }
}

class Game(
    val maxRows: Int,
    val maxCols: Int,
) {

    val state: MutableState<GameState> by lazy {
        mutableStateOf(
            GameState(
                isPaused = true,
                generation = 0,
                cells = Matrix(maxRows, maxCols){ _, _ ->
                    false
                }
            )
        )
    }

    fun reset(){
        state.value = GameState(
            isPaused = true,
            generation = 0,
            cells = Matrix(maxRows, maxCols){ _, _ -> false }
        )
    }

    fun selectCell(row: Int, col: Int){

        val newValue = state.value.cells[row, col].not()

        state.value = state.value.copy(
            cells = state.value.cells.copyWith(row, col, newValue)
        )
    }

    fun pauseOrResume(pause: Boolean? = null){
        state.value = state.value.copy(isPaused = pause ?: state.value.isPaused.not())
    }

    fun nextGeneration(){

        // For the time being, invert all of them.
        state.value = state.value.copy(
            generation = state.value.generation + 1,
            cells = Matrix(maxRows, maxCols){ r, c ->
                state.value.cells.canLiveAhead(r, c)
            }
        )

    }

    private fun Matrix<Boolean>.copyWith(row: Int, col: Int, value: Boolean): Matrix<Boolean> {
        return Matrix(this.rows, this.cols){ r, c ->
            if(r == row && c == col) value else this[r, c]
        }
    }

    private fun Matrix<Boolean>.canLiveAhead(row: Int, col: Int): Boolean {

        val isAlive = this[row, col]

        val northRow = if(row == 0) rows-1 else row-1
        val southRow = if(row == rows-1) 0 else row+1
        val eastColumn = if(col == cols-1) 0 else col+1
        val westColumn = if(col == 0) cols-1 else col-1

        // Find all 8 neighbors
        val neighbors = listOf(
            get(northRow, col), // North
            get(northRow, eastColumn), // North-east
            get(row, eastColumn), // East
            get(southRow, eastColumn), // South-east
            get(southRow, col), // South
            get(southRow, westColumn), // South-west
            get(row, westColumn), // West
            get(northRow, westColumn), // North-west
        ).count { it }

        /*
            1. Any live cell with two or three live neighbours survives.
            2. Any dead cell with three live neighbours becomes a live cell.
            3. All other live cells die in the next generation. Similarly, all other dead cells stay dead.
        */
        return if(isAlive){
            neighbors in 2..3
        } else {
            neighbors == 3
        }

    }

}



@Preview
@Composable
fun GameBoard(rows: Int = 20, cols: Int = 20){

    val game = remember { Game(maxRows = rows, maxCols = cols) }

    val gameState = remember { game.state }

    LaunchedEffect(!gameState.value.isPaused){

        while(!gameState.value.isPaused){
            game.nextGeneration()
            delay(timeMillis = 150)
        }

    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(color = Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        GameBoardTable(gameState.value.cells){ r, c ->
            game.selectCell(r, c)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Info display - Generation and Population
        Row {

            Text("Generation: ${gameState.value.generation}")

            Spacer(modifier = Modifier.width(16.dp))

            Text("Population: ${gameState.value.population}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Control Buttons - "Start/Pause", "Reset", "Next"
        Row {

            // Next
            Button(onClick = {

                game.pauseOrResume(pause = true)
                game.nextGeneration()

            }) {
                Text("Next")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                game.pauseOrResume()
            }) {
                Text( if(gameState.value.isPaused) "Start" else "Pause" )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                game.reset()
            }){
                Text("Reset")
            }

        }
    }
}

@Composable
fun GameBoardTable(matrix: Matrix<Boolean>, onClick: (Int, Int) -> Unit){
    Column(
        verticalArrangement = Arrangement.Center
    ) {

        (0 until matrix.rows).forEach { currentRow ->
            GameBoardRow(currentRow, matrix, onClick)
        }

    }
}

@Composable
fun GameBoardRow(currentRow: Int, matrix: Matrix<Boolean>, onClick: (Int, Int) -> Unit){
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {

        (0 until matrix.cols).forEach { currentColumn ->

            val isAlive = matrix[currentRow, currentColumn]

            Box(
                modifier = Modifier
                    .width(CELL_SIZE_DP.dp)
                    .height(CELL_SIZE_DP.dp)
                    .background(color = if (isAlive) Color.Black else Color.White)
                    .border(width = 1.dp, color = Color(android.graphics.Color.parseColor("#EEEEEE")))
                    .clickable {
                        onClick(currentRow, currentColumn)
                    },
            )

        }

    }
}