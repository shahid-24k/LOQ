package com.music.echo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingTest {

    private fun isValidUsername(name: String): Boolean {
        return name.trim().isNotEmpty()
    }

    private fun sanitizeUsername(name: String): String {
        return name.trim()
    }

    private fun formatGreeting(greeting: String, name: String): String {
        return "$greeting, $name"
    }

    @Test
    fun `valid usernames are accepted`() {
        assertTrue(isValidUsername("Shahid"))
        assertTrue(isValidUsername("Alex"))
        assertTrue(isValidUsername("  John  "))
        assertTrue(isValidUsername("日本語"))
        assertTrue(isValidUsername("Éléonore"))
        assertTrue(isValidUsername("محمد"))
    }

    @Test
    fun `empty and blank usernames are rejected`() {
        assertFalse(isValidUsername(""))
        assertFalse(isValidUsername("   "))
        assertFalse(isValidUsername("\t\n"))
    }

    @Test
    fun `username is sanitized properly`() {
        assertEquals("Shahid", sanitizeUsername("  Shahid  "))
        assertEquals("John Doe", sanitizeUsername("John Doe"))
    }

    @Test
    fun `time-based greeting format matches expectation`() {
        assertEquals("Good morning, Shahid", formatGreeting("Good morning", "Shahid"))
        assertEquals("Good afternoon, Alex", formatGreeting("Good afternoon", "Alex"))
        assertEquals("Good evening, Guest", formatGreeting("Good evening", "Guest"))
    }
}

