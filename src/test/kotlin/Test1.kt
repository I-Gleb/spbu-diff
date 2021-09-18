import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.*

internal class Test1 {
    private val standardOut = System.out
    private val standardIn = System.`in`
    private val stream = ByteArrayOutputStream()

    @BeforeTest
    fun setUp() {
        System.setOut(PrintStream(stream))
    }

    @AfterTest
    fun tearDown() {
        System.setOut(standardOut)
        System.setIn(standardIn)
    }

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
        val a = listOf("a", "b", "c").map { Line(it) }
        val b = listOf("a", "d", "c", "e").map { Line(it) }

        File("a.txt").bufferedWriter().use { out -> for (line in a) out.write(line.s + "\n") }
        File("b.txt").bufferedWriter().use { out -> for (line in b) out.write(line.s + "\n") }

        assertEquals(processInput(arrayOf("a.txt", "b.txt")), Pair(a, b))

        File("a.txt").delete()
        File("b.txt").delete()
    }

    @Test
    fun testInputFromConsole() {
        val a = listOf("a", "b", "c").map { Line(it) }
        val b = listOf("a", "d", "c", "e").map { Line(it) }

        File("a.txt").bufferedWriter().use { out -> for (line in a) out.write(line.s + "\n") }
        File("b.txt").bufferedWriter().use { out -> for (line in b) out.write(line.s + "\n") }
        System.setIn(ByteArrayInputStream("a.txt\nb.txt\n".toByteArray()))

        assertEquals(processInput(arrayOf()), Pair(a, b))

        File("a.txt").delete()
        File("b.txt").delete()
    }

    @Test
    fun testFindChanges() {
        val a = listOf("a", "b", "c").map { Line(it) }
        val b = listOf("a", "d", "c", "e").map { Line(it) }
        val diff = listOf(
            Line("a", LineStatus.NotChanged),
            Line("b", LineStatus.Deleted),
            Line("d", LineStatus.Added),
            Line("c", LineStatus.NotChanged),
            Line("e", LineStatus.Added)
        )

        assertEquals(findChanges(a, b), diff)
    }

    @Test
    fun testPrintDiff() {
        val diff = listOf(
            Line("a", LineStatus.NotChanged),
            Line("b", LineStatus.Deleted),
            Line("d", LineStatus.Added),
            Line("c", LineStatus.NotChanged),
            Line("e", LineStatus.Added)
        )

        printDifference(diff)

        assertEquals(stream.toString().trim().lines().joinToString("\n"),
            "a\n-b\n+d\nc\n+e")
    }

    @Test
    fun testMain1() {
        val a = listOf("a", "b", "c").map { Line(it) }
        val b = listOf("a", "d", "c", "e").map { Line(it) }
        val diff = """
            a
            -b
            +d
            c
            +e
        """.trimIndent()

        File("a.txt").bufferedWriter().use { out -> for (line in a) out.write(line.s + "\n") }
        File("b.txt").bufferedWriter().use { out -> for (line in b) out.write(line.s + "\n") }

        main(arrayOf("a.txt", "b.txt"))

        assertEquals(stream.toString().trim().lines().joinToString("\n"), diff)

        File("a.txt").delete()
        File("b.txt").delete()
    }

    @Test
    fun testMain2() {
        val a = listOf("").map { Line(it) }
        val b = listOf("a", "b").map { Line(it) }
        val diff = """
            -
            +a
            +b
        """.trimIndent()

        File("a.txt").bufferedWriter().use { out -> for (line in a) out.write(line.s + "\n") }
        File("b.txt").bufferedWriter().use { out -> for (line in b) out.write(line.s + "\n") }

        main(arrayOf("a.txt", "b.txt"))

        assertEquals(stream.toString().trim().lines().joinToString("\n"), diff)

        File("a.txt").delete()
        File("b.txt").delete()
    }

    @Test
    fun testMain3() {
        val a = listOf("be", "or", "not", "to", "be").map { Line(it) }
        val b = listOf("say", "or", "not", "to", "say").map { Line(it) }
        val diff = """
            -be
            +say
            or
            not
            to
            -be
            +say
        """.trimIndent()

        File("a.txt").bufferedWriter().use { out -> for (line in a) out.write(line.s + "\n") }
        File("b.txt").bufferedWriter().use { out -> for (line in b) out.write(line.s + "\n") }

        main(arrayOf("a.txt", "b.txt"))

        assertEquals(stream.toString().trim().lines().joinToString("\n"), diff)

        File("a.txt").delete()
        File("b.txt").delete()
    }
}
