package com.music.echo

import echo.music.iad1tya.ui.screens.isEasterEggName
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EasterEggTest {

    @Test
    fun `matching names trigger easter egg`() {
        assertTrue(isEasterEggName("Bena"))
        assertTrue(isEasterEggName("bena"))
        assertTrue(isEasterEggName("BENA"))
        assertTrue(isEasterEggName("  bena  "))
        assertTrue(isEasterEggName("Benaser"))
        assertTrue(isEasterEggName("benaser"))
        assertTrue(isEasterEggName("BENASER"))
        assertTrue(isEasterEggName("Bena Ser"))
        assertTrue(isEasterEggName("Bena-ser"))
        assertTrue(isEasterEggName("bena_ser"))
        assertTrue(isEasterEggName("Bena."))
    }

    @Test
    fun `unrelated names do not trigger easter egg`() {
        assertFalse(isEasterEggName("Alex"))
        assertFalse(isEasterEggName("John Doe"))
        assertFalse(isEasterEggName("Benjamin"))
        assertFalse(isEasterEggName("Serena"))
        assertFalse(isEasterEggName("Ben"))
        assertFalse(isEasterEggName("Abena"))
        assertFalse(isEasterEggName("Ebenaser"))
        assertFalse(isEasterEggName(""))
    }
}
