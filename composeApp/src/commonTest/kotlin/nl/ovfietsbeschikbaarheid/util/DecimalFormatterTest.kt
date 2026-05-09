package nl.ovfietsbeschikbaarheid.util

import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalFormatterTest {

    private val formatter = DecimalFormatter()

    @Test
    fun `also format numbers ending with a zero with 1 decimal`() {
        assertEquals("1,0", formatter.format(1.0, 1))
    }

    @Test
    fun `rounds a multi-decimal distance to one decimal`() {
        assertEquals("5,8", formatter.format(5.799, 1))
    }
}
