import java.io.File
import kotlin.test.*

internal class Test1 {

    @Test
    fun testLCS() {
        assertEquals(longestCommonSubseq(listOf(1, 2, 3), listOf(1, 2, 3)), listOf(1, 2, 3))
        assertEquals(longestCommonSubseq(listOf(1, 2, 3, 4), listOf(1, 2, 4)), listOf(1, 2, 4))
        assertEquals(longestCommonSubseq(listOf(1, 2), listOf()), listOf())
        assertEquals(longestCommonSubseq(listOf(1, 2, 3), listOf(4, 5)), listOf())
        assertEquals(longestCommonSubseq(listOf(1, 2, 3), listOf(2, 1, 3)).size, 2)
        assertEquals(longestCommonSubseq(listOf(1, 2, 3), listOf(3, 2, 1)).size, 1)
        assertEquals(longestCommonSubseq(listOf("ab", "c", "efg"), listOf("e", "ab", "f", "c", "g")), listOf("ab", "c"))
    }

    @Test
    fun testInputFromParameters() {
        val a = listOf("a", "b", "c")
        val b = listOf("a", "d", "c", "b")
        File("a.txt").bufferedWriter().use { out -> for (line in a) out.write(line + "\n") }
        File("b.txt").bufferedWriter().use { out -> for (line in b) out.write(line + "\n") }
        assertEquals(processInput(arrayOf("a.txt", "b.txt")), Pair(a, b))
    }

    @Test
    fun testFindChanges() {
        val a = listOf("a", "b", "c")
        val b = listOf("a", "d", "c", "e")
        val diff = listOf(Pair(0, "a"), Pair(-1, "b"), Pair(1, "d"), Pair(0, "c"), Pair(1, "e"))
        assertEquals(findChanges(a, b), diff)
    }
}
