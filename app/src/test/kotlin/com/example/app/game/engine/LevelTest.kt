package com.example.app.game.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelTest {

    @Test
    fun `given a level when parsed then width and height match the rows (01-moteur)`() {
        val level = TestLevels.flat
        assertEquals(10, level.width)
        assertEquals(3, level.height)
    }

    @Test
    fun `given a level when queried then only hash tiles are solid (01-moteur)`() {
        val level = TestLevels.flat
        assertTrue(level.isSolid(0, 2))
        assertTrue(level.isSolid(9, 2))
        assertFalse(level.isSolid(0, 0))
        assertFalse(level.isSolid(1, 1)) // S n'est pas solide
        assertFalse(level.isSolid(8, 1)) // E n'est pas solide
    }

    @Test
    fun `given a level when parsed then start and exit are the S and E tiles (01-moteur)`() {
        val level = TestLevels.flat
        assertEquals(Tile(1, 1), level.start)
        assertEquals(Tile(8, 1), level.exit)
    }

    @Test
    fun `given ragged rows when parsed then missing cells are empty (01-moteur)`() {
        val level =
            Level(
                listOf(
                    "S..",
                    "E#",
                    "#"
                )
            )
        assertEquals(3, level.width)
        assertEquals(3, level.height)
        assertEquals(Tile(0, 0), level.start)
        assertEquals(Tile(0, 1), level.exit)
        assertTrue(level.isSolid(1, 1))
        assertTrue(level.isSolid(0, 2))
        assertFalse(level.isSolid(2, 1)) // ligne courte : cellule absente = vide
        assertFalse(level.isSolid(2, 2))
    }

    @Test
    fun `given coordinates out of bounds when queried then not solid (01-moteur)`() {
        val level = TestLevels.flat
        assertFalse(level.isSolid(-1, 0))
        assertFalse(level.isSolid(0, -1))
        assertFalse(level.isSolid(level.width, 0))
        assertFalse(level.isSolid(0, level.height))
    }

    @Test
    fun `given an empty level when constructed then it throws (01-moteur)`() {
        assertThrows(IllegalArgumentException::class.java) { Level(emptyList()) }
    }

    @Test
    fun `given a level without start when constructed then it throws (01-moteur)`() {
        assertThrows(IllegalArgumentException::class.java) {
            Level(
                listOf(
                    "....",
                    "#E.#",
                    "####"
                )
            )
        }
    }

    @Test
    fun `given a level without exit when constructed then it throws (01-moteur)`() {
        assertThrows(IllegalArgumentException::class.java) {
            Level(
                listOf(
                    "....",
                    "#S.#",
                    "####"
                )
            )
        }
    }

    @Test
    fun `given a level with two starts when constructed then it throws (01-moteur)`() {
        assertThrows(IllegalArgumentException::class.java) {
            Level(
                listOf(
                    "....",
                    "#SES",
                    "####"
                )
            )
        }
    }

    @Test
    fun `given a level with two exits when constructed then it throws (01-moteur)`() {
        assertThrows(IllegalArgumentException::class.java) {
            Level(
                listOf(
                    "....",
                    "##EE",
                    "##S#",
                    "####"
                )
            )
        }
    }
}
