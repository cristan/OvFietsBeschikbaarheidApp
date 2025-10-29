package nl.ovfietsbeschikbaarheid.testutils

import org.junit.Assert.assertEquals

infix fun <T> T.shouldBeEqualTo(expected: T?): T {
    assertEquals(expected, this)
    return this
}
