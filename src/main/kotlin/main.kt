import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*
import kotlin.system.exitProcess

enum class TextColor(val prefix: String) {
    DEFAULT("\u001B[0m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m")
}

enum class LineStatus(val prefix: String, val color: TextColor) {
    Added("+", TextColor.GREEN),
    Deleted("-", TextColor.RED),
    NotChanged("", TextColor.DEFAULT)
}

enum class OutputFormat { FULL, UNIFIED }

data class Line(val s: String, val status: LineStatus = LineStatus.NotChanged) {

    fun getHash(s: String): Long {
        val p = 179
        val mod = 1000000087
        var res: Long = 0
        for (elem in s) {
            res = ((res * p) + elem.code) % mod
        }
        return res
    }

    val h = Pair(s.hashCode(), getHash(s))
}

data class InputData(val fileFirst: File, val fileSecond: File, val formatOut: OutputFormat, val isColored: Boolean)

class Diff : CliktCommand() {
    val formatOut by option(help = "output format").switch(
        "--full" to OutputFormat.FULL,
        "-f" to OutputFormat.FULL,
        "--unified" to OutputFormat.UNIFIED,
        "-u" to OutputFormat.UNIFIED
    ).default(OutputFormat.FULL)
    val isColored by option("-c", "--color", help = "colored output").flag()
    val fileFirst by argument(help = "path to the first file").file(mustExist = true, canBeDir = false, mustBeReadable = true)
    val fileSecond by argument(help = "path to the second file").file(mustExist = true, canBeDir = false, mustBeReadable = true)
    override fun run() = Unit
}

/*
 * Принимает параметры командной строки. При необходимости запрашивает данные у пользователя.
 * Производится считываение и обработка входных даныых.
 * Функция возращает два сравниваемых файла и информацию о формате выходных данных.
 * Если ввод некорректен, то программа завершается с кодом 1 и сообщением пользователю.
 */
fun processInput(args: Array<String>): InputData {
    if (args.isEmpty()) {
        return interactWithUser()
    }
    val parser = Diff()
    parser.main(args)
    return InputData(parser.fileFirst, parser.fileSecond, parser.formatOut, parser.isColored)
}

/*
 * В общении с пользователем получает и обрабатывает входные данные.
 * Возращает файлы, которые будут сравниваться, и формат выходных данных.
 */
fun interactWithUser(): InputData {

    /*
     * Принимает на вход зануляемую строку.
     * Если строка является корректным путём до файла, то возращает его. Иначе - null.
     */
    fun getFileOrNull(path: String?): File? {
        val file = if (path != null) File(path) else null
        if (file?.isFile == true) return file
        return null
    }

    println("Enter path to the first file:")
    val fileFirst = getFileOrNull(readLine())
    if (fileFirst == null) {
        println("Incorrect path to file")
        exitProcess(1)
    }

    println("Enter path to the second file:")
    val fileSecond = getFileOrNull(readLine())
    if (fileSecond == null) {
        println("Incorrect path to file")
        exitProcess(1)
    }

    println("Full or unified format? (full/unified)")
    val formatOut = when (readLine()) {
        "full" -> OutputFormat.FULL
        "unified" -> OutputFormat.UNIFIED
        else -> {
            println("Incorrect format: \"full\"/\"unified\" expected")
            exitProcess(1)
        }
    }

    println("Make colorful lines? (y/n)")
    val isColored = when (readLine()) {
        "y" -> true
        "n" -> false
        else -> {
            println("Incorrect input: \"y\"/\"n\" expected")
            exitProcess(1)
        }
    }

    return InputData(fileFirst, fileSecond, formatOut, isColored)
}

/*
 * Принимает две последовательности.
 * Возращает их наибольшую общую подпоследовательность.
 */
fun <T> longestCommonSubseq(a : List<T>, b : List<T>): List<T> {
    // prefixesLCS[i][j] будет хранить длину наибольшей общей подпоследовательности
    // префикса а длины i и префикса b длины j
    fun calcPrefixesLCS(): List<MutableList<Int> > {
        val prefixesLCS = List(a.size + 1) { MutableList(b.size + 1) {0} }
        for (i in a.indices) {
            for (j in b.indices) {
                prefixesLCS[i + 1][j + 1] =
                    if (a[i] == b[j]) prefixesLCS[i][j] + 1 else max(prefixesLCS[i][j + 1], prefixesLCS[i + 1][j])
            }
        }
        return prefixesLCS
    }

    val prefixesLCS = calcPrefixesLCS()
    val lcs = mutableListOf<T>()
    var currI = a.size
    var currJ = b.size
    // строим LCS(a, b) c конца
    // currI, currJ - префиксы a и b соответвенно, LCS для которых строится в данный момент
    while (currI != 0 && currJ != 0) {
        when {
            prefixesLCS[currI][currJ] == prefixesLCS[currI - 1][currJ] -> currI--
            prefixesLCS[currI][currJ] == prefixesLCS[currI][currJ - 1] -> currJ--
            else -> {
                lcs.add(a[currI - 1])
                currI--
                currJ--
            }
        }
    }
    // мы построили LCS c конца, теперь осталось развернуть полученную последовательность
    lcs.reverse()

    return lcs.toList()
}

/*
 * Получает два файла.
 * Файлы сравниваются.
 * Функция возращает файл-сравнение, в котором у каждой строки есть пометка о её статусе.
 */
fun findChanges(originalFile : List<Line>, updatedFile : List<Line>): List<Line> {
    val hashesOfLcs = longestCommonSubseq(originalFile.map { it.h }, updatedFile.map { it.h })
    val comparisonFile = mutableListOf<Line>()
    // будем идти двумя указателями, currI по originalFile, currJ по updatedFile
    var currI = 0
    var currJ = 0
    for (hashOfCommonLine in hashesOfLcs) {
        // добавляем в файл-сравенение удалённые строки
        while (originalFile[currI].h != hashOfCommonLine) {
            comparisonFile.add(Line(originalFile[currI].s, LineStatus.Deleted))
            ++currI
        }
        // добавляем в файл-сравнение добаленные строки
        while (updatedFile[currJ].h != hashOfCommonLine) {
            comparisonFile.add(Line(updatedFile[currJ].s, LineStatus.Added))
            ++currJ
        }
        // добавляем в файл-сравнение неизменную строку
        comparisonFile.add(Line(originalFile[currI].s))
        ++currI
        ++currJ
    }
    // добавляем в файл-сравенение удалённые строки после последней общей строки
    while (currI < originalFile.size) {
        comparisonFile.add(Line(originalFile[currI].s, LineStatus.Deleted))
        ++currI
    }
    // добавляем в файл-сравнение добаленные строки после последней общей строки
    while (currJ < updatedFile.size) {
        comparisonFile.add(Line(updatedFile[currJ].s, LineStatus.Added))
        ++currJ
    }
    return comparisonFile.toList()
}

/*
 * Принимает файл-сравнение comparisonFile.
 * Выводит разницу между файлами по файлу-сравнению в формате, зависящем от input.formatOut и input.is_colored.
 */
fun printDifference(input: InputData, comparisonFile : List<Line>) {

    /*
     * Возращает индекс первой изменнённой строки начиная с индекса ind.
     * Если после индекса ind нет изменнённых строк вернёт ind - 1.
     */
    fun nextChanged(ind: Int): Int {
         return comparisonFile.drop(ind).indexOfFirst { it.status != LineStatus.NotChanged } + ind
    }

    /*
     * Получает 2 файла.
     * Печатает заголовок diff с именами файлов и временами последного изменения
     */
    fun printHeader(fileFirst: File, fileSecond: File) {
        println("--- ${fileFirst.name} ${Date(fileFirst.lastModified())}")
        println("+++ ${fileSecond.name} ${Date(fileSecond.lastModified())}")
    }

    /*
     * Принимает строку и цвет.
     * Печатает строку заданным цветом.
     */
    fun printColored(s: String, color: TextColor) {
        println(color.prefix + s + TextColor.DEFAULT.prefix)
    }

    /*
     * Принимает строку и значение типа Boolean.
     * Печатает строку или цветную строку в зависимости от переданного Boolean-значения.
     * Цвет и добавляемый перед строкой префикс зависят от line.status.
     */
    fun printLine(line: Line, isColored: Boolean) {
        if (isColored) {
            printColored(line.status.prefix + line.s, line.status.color)
        }
        else {
            println(line.status.prefix + line.s)
        }
    }

    /*
     * Печатает сравнения в unified-формате.
     */
    fun printUnified() {
        val contextSize = 3 // размер контеста, количество неизменных строк, которые выводятся до и после блока изменений
        var lineNum1 = 1 // номер строки в исходном файле
        var lineNum2 = 1 // номер строки в новом файле
        var startOfBlock = 0 // индекс на начало блока изменений

        while (startOfBlock < comparisonFile.size) {
            // неизменнённую строку нужно пропустить
            if (comparisonFile[startOfBlock].status == LineStatus.NotChanged) {
                startOfBlock++
                lineNum1++
                lineNum2++
                continue
            }

            var cntAdded = 0 // количество добавленных строк в блоке
            var cntDeleted = 0 // количество удалённых строк в блоке
            var endOfBlock = startOfBlock // индекс на конец блока измений

            // добавляем в блок строки, пока не будет 2 * contextSize неизменнённых строк подряд
            while (endOfBlock <= nextChanged(endOfBlock) && nextChanged(endOfBlock) < endOfBlock + 2 * contextSize) {
                when (comparisonFile[endOfBlock].status) {
                    LineStatus.Added -> ++cntAdded
                    LineStatus.Deleted -> ++cntDeleted
                }
                ++endOfBlock
            }

            val st = max(0, startOfBlock - contextSize) // индекс на начало выводимого блока
            val end = min(endOfBlock + contextSize, comparisonFile.size) // индекс на конец выводимого блока

            // вывод блока
            println("@@ -${lineNum1 - (startOfBlock - st)},${end - st - cntAdded} +${lineNum2 - (startOfBlock - st)},${end - st - cntDeleted} @@")
            for (ind in st until end) printLine(comparisonFile[ind], input.isColored)

            lineNum1 += endOfBlock - startOfBlock - cntAdded
            lineNum2 += endOfBlock - startOfBlock - cntDeleted
            startOfBlock = endOfBlock
        }
    }

    /*
     * Печатает сравнение в полном формате.
     */
    fun printFull() {
        for (line in comparisonFile) {
            printLine(line, input.isColored)
        }
    }

    printHeader(input.fileFirst, input.fileSecond)
    if (input.formatOut == OutputFormat.FULL) {
        printFull()
    }
    else {
        printUnified()
    }
}

fun main(args: Array<String>) {
    val input = processInput(args)

    val comparisonFile = findChanges(
        input.fileFirst.bufferedReader().readLines().map { Line(it) },
        input.fileSecond.bufferedReader().readLines().map { Line(it) }
    )

    printDifference(input, comparisonFile)
}
