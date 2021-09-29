import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.lang.Integer.max
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

enum class OutputFormat { FULL, SHORT }

data class Line(val s: String, val status: LineStatus = LineStatus.NotChanged)

data class InputData(val fileFirst: List<Line>, val fileSecond: List<Line>, val formatOut: OutputFormat, val isColored: Boolean)

class ArgsParser : CliktCommand() {
    val formatOut by option(help = "output format").switch(
        "--full" to OutputFormat.FULL,
        "--short" to OutputFormat.SHORT
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
    val parser = ArgsParser()
    parser.main(args)
    return InputData(
        parser.fileFirst.bufferedReader().readLines().map { Line(it) },
        parser.fileSecond.bufferedReader().readLines().map { Line(it) },
        parser.formatOut,
        parser.isColored
    )
}

/*
 * В общении с пользователем получает и обрабатывает входные данные.
 * Возращает файлы, которые будут сравниваться, и формат выходных данных.
 */
fun interactWithUser(): InputData {
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

    println("Full or short format? (full/short)")
    val formatOut = when (readLine()) {
        "full" -> OutputFormat.FULL
        "short" -> OutputFormat.SHORT
        else -> {
            println("Incorrect format: \"full\"/\"short\" expected")
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

    return InputData(
        fileFirst.bufferedReader().readLines().map { Line(it) },
        fileSecond.bufferedReader().readLines().map { Line(it) },
        formatOut,
        isColored
    )
}

/*
 * Принимает на вход зануляемую строку.
 * Если строка является корректным путём до файла, то возращает его. Иначе - null.
 */
fun getFileOrNull(path: String?): File? {
    val file = if (path != null) File(path) else null
    if (file?.isFile == true) return file
    return null
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
    val lcs = longestCommonSubseq(originalFile, updatedFile)
    val comparisonFile = mutableListOf<Line>()
    // будем идти двумя указателями, currI по originalFile, currJ по updatedFile
    var currI = 0
    var currJ = 0
    for (commonLine in lcs) {
        // добавляем в файл-сравенение удалённые строки
        while (originalFile[currI] != commonLine) {
            comparisonFile.add(Line(originalFile[currI].s, LineStatus.Deleted))
            ++currI
        }
        // добавляем в файл-сравнение добаленные строки
        while (updatedFile[currJ] != commonLine) {
            comparisonFile.add(Line(updatedFile[currJ].s, LineStatus.Added))
            ++currJ
        }
        // добавляем в файл-сравнение неизменную строку
        comparisonFile.add(Line(commonLine.s))
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
 * Выводит разницу между файлами по файлу-сравнению в формате, зависящим от formatOut и is_colored.
 */
fun printDifference(comparisonFile : List<Line>, formatOut: OutputFormat, isColored: Boolean) {
    if (formatOut == OutputFormat.FULL) {
        for (line in comparisonFile) {
            printLine(line, isColored)
        }
    }
    else {
        TODO()
    }
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

fun main(args: Array<String>) {
    val input = processInput(args)

    val comparisonFile = findChanges(input.fileFirst, input.fileSecond)

    printDifference(comparisonFile, input.formatOut, input.isColored)
}
