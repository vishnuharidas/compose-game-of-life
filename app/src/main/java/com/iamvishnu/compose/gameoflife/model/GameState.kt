package com.iamvishnu.compose.gameoflife.model

data class GameState(
    val isPaused: Boolean = false,
    val generation: Int = 0,
    val isStillLife: Boolean = false,
    val cells: Matrix<Boolean>, // Row major list.
){
    val population: Int by lazy {
        cells.count { it }
    }

    val status: String get() {
        return when {
            isStillLife -> "Reached Still Life."
            !isPaused -> "Progressing..."
            isPaused && generation == 0 && population == 0 -> "Ready. Tap to add some life."
            isPaused && generation == 0 && population > 0 -> "Ready to go."
            isPaused && generation > 0 && population == 0 -> "Everyone died."
            else -> "Paused"
        }
    }
}