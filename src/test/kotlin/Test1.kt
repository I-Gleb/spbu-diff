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
    fun testInputFromParameters1() {
        assertEquals(
            processInput(arrayOf("test-cases/1/a.txt", "test-cases/1/b.txt")),
            InputData(
                File("test-cases/1/a.txt"),
                File("test-cases/1/b.txt"),
                OutputFormat.FULL,
                false
            )
        )
    }

    @Test
    fun testInputFromParameters2() {
        assertEquals(
            processInput(arrayOf("--color", "test-cases/1/a.txt", "test-cases/1/b.txt")),
            InputData(
                File("test-cases/1/a.txt"),
                File("test-cases/1/b.txt"),
                OutputFormat.FULL,
                true
            )
        )
    }

    @Test
    fun testInputFromParameters3() {
        assertEquals(
            processInput(arrayOf("-fc", "test-cases/1/a.txt", "test-cases/1/b.txt")),
            InputData(
                File("test-cases/1/a.txt"),
                File("test-cases/1/b.txt"),
                OutputFormat.FULL,
                true
            )
        )
    }

    @Test
    fun testInputFromParameters4() {
        assertEquals(
            processInput(arrayOf("--color", "--unified", "test-cases/1/a.txt", "test-cases/1/b.txt")),
            InputData(
                File("test-cases/1/a.txt"),
                File("test-cases/1/b.txt"),
                OutputFormat.UNIFIED,
                true
            )
        )
    }

    @Test
    fun testInputFromConsole() {
        System.setIn(ByteArrayInputStream("test-cases/1/a.txt\ntest-cases/1/b.txt\nfull\nn\n".toByteArray()))

        assertEquals(
            processInput(arrayOf()),
            InputData(
                File("test-cases/1/a.txt"),
                File("test-cases/1/b.txt"),
                OutputFormat.FULL,
                false
            )
        )
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

        printDifference(InputData(File("test-cases/1/a.txt"), File("test-cases/1/b.txt"), OutputFormat.FULL, false), diff)

        assertEquals(stream.toString().trim().lines().drop(2).joinToString("\n"),
            File("test-cases/1/out.txt").readText().trim().lines().drop(2).joinToString("\n"))
    }

    @Test
    fun testMain1() {
        main(arrayOf("test-cases/1/a.txt", "test-cases/1/b.txt"))

        assertEquals(stream.toString().trim().lines().drop(2).joinToString("\n"),
            File("test-cases/1/out.txt").readText().trim().lines().drop(2).joinToString("\n"))
    }

    @Test
    fun testMain2() {
        main(arrayOf("test-cases/2/a.txt", "test-cases/2/b.txt"))

        assertEquals(stream.toString().trim().lines().drop(2).joinToString("\n"),
            File("test-cases/2/out.txt").readText().trim().lines().drop(2).joinToString("\n"))
    }

    @Test
    fun testMain3() {
        main(arrayOf("test-cases/3/a.txt", "test-cases/3/b.txt"))

        assertEquals(stream.toString().trim().lines().drop(2).joinToString("\n"),
            File("test-cases/3/out.txt").readText().trim().lines().drop(2).joinToString("\n"))
    }

    @Test
    fun testMain4() {
        // big test
        // a and b have 3000 elements each
        // diff has 4000 lines
        main(arrayOf("test-cases/4/a.txt", "test-cases/4/b.txt"))

        assertEquals(stream.toString().trim().lines().drop(2).joinToString("\n"),
            File("test-cases/4/out.txt").readText().trim().lines().drop(2).joinToString("\n"))
    }

    @Test
    fun testMain5() {
        // unified format
        main(arrayOf("-u", "test-cases/5/a.txt", "test-cases/5/b.txt"))

        assertEquals(stream.toString().trim().lines().drop(2).joinToString("\n"),
            File("test-cases/5/out.txt").readText().trim().lines().drop(2).joinToString("\n"))
    }
}
