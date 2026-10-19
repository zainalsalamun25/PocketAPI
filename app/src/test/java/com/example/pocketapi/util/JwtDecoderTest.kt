package com.example.pocketapi.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JwtDecoderTest {

    @Test
    fun testIsJwtValidation() {
        val validJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        val invalidJwt = "not.a.jwt.token"

        assertTrue(JwtDecoder.isJwt(validJwt))
        assertFalse(JwtDecoder.isJwt(invalidJwt))
        assertFalse(JwtDecoder.isJwt("just_a_string"))
    }

    @Test
    fun testDecodeValidJwt() {
        val validJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        val result = JwtDecoder.decode(validJwt)

        assertTrue(result.isSuccess)
        val decoded = result.getOrNull()!!

        assertEquals("HS256", decoded.algorithm)
        assertEquals("1234567890", decoded.subject)
        assertEquals("SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c", decoded.signature)
    }
}
