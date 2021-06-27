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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iamvishnu.compose.gameoflife.model.Game
import com.iamvishnu.compose.gameoflife.model.Matrix
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