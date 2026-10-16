package com.vezhny.eventtracker.ui.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventIconTest {
    @Test
    fun `keys are unique`() {
        val keys = EventIcon.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `every icon is found by its key`() {
        EventIcon.entries.forEach { assertEquals(it, EventIcon.fromKey(it.key)) }
    }

    @Test
    fun `every icon path parses`() {
        EventIcon.entries.forEach { icon ->
            assertTrue(icon.key, icon.vector.root.size > 0)
        }
    }

    @Test
    fun `no key or an unknown one means no icon`() {
        assertNull(EventIcon.fromKey(null))
        assertNull(EventIcon.fromKey("unicorn"))
    }
}
