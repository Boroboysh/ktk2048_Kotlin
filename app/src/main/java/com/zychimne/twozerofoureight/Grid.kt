package com.zychimne.twozerofoureight

import kotlin.collections.ArrayList
import kotlin.math.floor

class Grid(sizeX: Int, sizeY: Int) {
    val tiles: Array<Array<Tile?>> =Array(sizeX) { arrayOfNulls(sizeY) }
    val undoField: Array<Array<Tile?>> =Array(sizeX) { arrayOfNulls(sizeY) }
    private val bufferField: Array<Array<Tile?>> =Array(sizeX) { arrayOfNulls(sizeY) }

    init {
        clearGrid()
        clearUndoGrid()
    }

    fun randomAvailableCell(): Cell? {
        val availableCells = availableCells
        return if (availableCells.size >= 1) {
            availableCells[floor(Math.random() * availableCells.size).toInt()]
        } else null
    }

    private val availableCells: ArrayList<Cell>
        get() {
            val availableCells = ArrayList<Cell>()
            for (xx in tiles.indices) {
                for (yy in tiles[0].indices) {
                    if (tiles[xx][yy] == null) {
                        availableCells.add(Cell(xx, yy))
                    }
                }
            }
            return availableCells
        }
    val isCellsAvailable: Boolean
        get() = availableCells.size >= 1

    fun isCellAvailable(cell: Cell?): Boolean {
        return !isCellOccupied(cell)
    }

    fun isCellOccupied(cell: Cell?): Boolean {
        return getCellContent(cell) != null
    }

    fun getCellContent(cell: Cell?): Tile? {
        return if (cell != null && isCellWithinBounds(cell)) {
            tiles[cell.x][cell.y]
        } else {
            null
        }
    }

    fun getCellContent(x: Int, y: Int): Tile? {
        return if (isCellWithinBounds(x, y)) {
            tiles[x][y]
        } else {
            null
        }
    }

    fun isCellWithinBounds(cell: Cell): Boolean {
        return 0 <= cell.x && cell.x < tiles.size && 0 <= cell.y && cell.y < tiles[0].size
    }

    private fun isCellWithinBounds(x: Int, y: Int): Boolean {
        return 0 <= x && x < tiles.size && 0 <= y && y < tiles[0].size
    }

    fun insertTile(tile: Tile?) {
        if (tile != null) {
            tiles[tile.x][tile.y] = tile
        }
    }

    fun removeTile(tile: Tile) {
        tiles[tile.x][tile.y] = null
    }

    fun saveTiles() {
        for (xx in bufferField.indices) {
            for (yy in bufferField[0].indices) {
                if (bufferField[xx][yy] == null) {
                    undoField[xx][yy] = null
                } else {
                    undoField[xx][yy] = bufferField[xx][yy]?.let { Tile(xx, yy, it.value) }
                }
            }
        }
    }

    fun prepareSaveTiles() {
        for (xx in tiles.indices) {
            for (yy in tiles[0].indices) {
                if (tiles[xx][yy] == null) {
                    bufferField[xx][yy] = null
                } else {
                    bufferField[xx][yy] = tiles[xx][yy]?.let { Tile(xx, yy, it.value) }
                }
            }
        }
    }

    fun revertTiles() {
        for (xx in undoField.indices) {
            for (yy in undoField[0].indices) {
                if (undoField[xx][yy] == null) {
                    tiles[xx][yy] = null
                } else {
                    tiles[xx][yy] = undoField[xx][yy]?.let { Tile(xx, yy, it.value) }
                }
            }
        }
    }

    fun clearGrid() {
        for (xx in tiles.indices) {
            for (yy in tiles[0].indices) {
                tiles[xx][yy] = null
            }
        }
    }

    private fun clearUndoGrid() {
        for (xx in tiles.indices) {
            for (yy in tiles[0].indices) {
                undoField[xx][yy] = null
            }
        }
    }
}