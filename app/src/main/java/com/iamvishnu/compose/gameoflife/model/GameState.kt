package com.iamvishnu.compose.gameoflife.model

data class GameState(
    val isPaused: Boolean = false,
    val generation: Int = 0,
    val cells: Matrix<Boolean>, // Row major list.
){
    val population: Int by lazy {
        cells.count { it }
    }
}