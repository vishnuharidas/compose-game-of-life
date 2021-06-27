package com.iamvishnu.compose.gameoflife.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

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

        val newMatrix = Matrix(maxRows, maxCols){ r, c ->
            state.value.cells.canLiveAhead(r, c)
        }

        val newCount = newMatrix.count { it }
        val oldCount = state.value.cells.count { it }

        // Compare only if the old and new count are the same. Skip other cases.
        val isStill = (newCount == oldCount) && newMatrix.isSame(state.value.cells)

        // For the time being, invert all of them.
        state.value = state.value.copy(
            generation = state.value.generation + 1,
            cells = newMatrix,
            isStillLife = isStill,
            isPaused = state.value.isPaused             // If it is already paused
                    || newMatrix.count { it } == 0      // Or if the count is zero
                    || isStill                          // Or stop when population is zero.
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