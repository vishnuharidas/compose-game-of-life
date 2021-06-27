package com.iamvishnu.compose.gameoflife.model

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

    fun isSame(other: Matrix<T>): Boolean {
        return other._values.filterIndexed { index, item ->
            item != this._values[index]
        }.isEmpty()
    }

}