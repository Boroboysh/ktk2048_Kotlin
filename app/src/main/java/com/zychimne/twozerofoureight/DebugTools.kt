package com.zychimne.twozerofoureight

/**
 * Tools and hooks for debugging.
 *
 * Note to self: when making a custom build, set the versionCode in
 * installed/build.gradle to 1.
 */
internal object DebugTools {
    private const val DEBUG_ENABLED = false
    private val PREMADE_MAP: Array<IntArray> = arrayOf(
        intArrayOf(128, 256, 32768, 131072),
        intArrayOf(8, 16, 0, 2),
        intArrayOf(0, 0, 0, 2),
        intArrayOf(0, 0, 0, 0)
    )
    private const val STARTING_SCORE = 2529244L
    fun generatePremadeMap(): List<Tile>? {
        if (!DEBUG_ENABLED) {
            return null
        }
        val result: MutableList<Tile> = ArrayList()
        for (yy in PREMADE_MAP.indices) {
            for (xx in PREMADE_MAP[0].indices) {
                if (PREMADE_MAP[yy][xx] == 0) {
                    continue
                }
                result.add(Tile(xx, yy, PREMADE_MAP[yy][xx]))
            }
        }
        return result
    }

    val startingScore: Long
        get() = if (!DEBUG_ENABLED) {
            0
        } else STARTING_SCORE
}