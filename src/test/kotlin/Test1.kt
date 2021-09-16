import kotlin.test.*

internal class Test1 {

    @Test
    fun testLCS() {
        assertEquals(longestCommonSubsequence(listOf(1, 2, 3), listOf(1, 2, 3)), listOf(1, 2, 3))
        assertEquals(longestCommonSubsequence(listOf(1, 2, 3, 4), listOf(1, 2, 4)), listOf(1, 2, 4))
        assertEquals(longestCommonSubsequence(listOf(1, 2), listOf()), listOf())
        assertEquals(longestCommonSubsequence(listOf(1, 2, 3), listOf(4, 5)), listOf())
        assertEquals(longestCommonSubsequence(listOf(1, 2, 3), listOf(2, 1, 3)).size, 2)
        assertEquals(longestCommonSubsequence(listOf(1, 2, 3), listOf(3, 2, 1)).size, 1)
        assertEquals(longestCommonSubsequence(listOf("ab", "c", "efg"), listOf("e", "ab", "f", "c", "g")), listOf("ab", "c"))
    }
}
