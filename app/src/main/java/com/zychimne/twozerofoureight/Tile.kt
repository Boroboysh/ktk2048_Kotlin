package com.zychimne.twozerofoureight

class Tile : Cell {
    val value: Int
    var mergedFrom: Array<Tile>? = null

    constructor(x: Int, y: Int, value: Int) : super(x, y) {
        this.value = value
    }

    constructor(cell: Cell, value: Int) : super(cell.x, cell.y) {
        this.value = value
    }

    fun updatePosition(cell: Cell) {
        x = cell.x
        y = cell.y
    }
}