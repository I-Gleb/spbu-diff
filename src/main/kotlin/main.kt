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

data class Line(val s: String, val status: LineStatus = LineStatus.NotChanged)

/*
 * Принимает параметры командной строки. При необходимости запрашивает данные у пользователя.
 * Производится считываение и обработка входных даныых.
 * Функция возращает два сравниваемых файла. Если ввод некорректен, то программа завершается с кодом 1 и сообщением пользователю
 */
fun processInput(args: Array<String>): Pair<List<Line>, List<Line> > {
    val pathFirst:String?
    val pathSecond:String?

    if (args.isNotEmpty()) {
        pathFirst = args.getOrNull(0)
        pathSecond = args.getOrNull(1)
    }
    else {
        println("Enter path to the first file:")
        pathFirst = readLine()
        println("Enter path to the second file:")
        pathSecond = readLine()
    }

    val fileFirst = if (pathFirst != null) File(pathFirst) else null
    val fileSecond = if (pathSecond != null) File(pathSecond) else null
    if (fileFirst?.isFile == true && fileSecond?.isFile == true) {
        return Pair(fileFirst.bufferedReader().readLines().map {Line(it)},
            fileSecond.bufferedReader().readLines().map {Line(it)})
    }
    else {
        println("Incorrect paths to files")
        exitProcess(1)
    }
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
 * Принимает файл-сравнение.
 * Выводит разницу между файлами по файлу-сравнению.
 */
fun printDifference(comparisonFile : List<Line>) {
    for (line in comparisonFile) {
        printLine(line, false)
    }
}

/*
 * Принимает цвет.
 * Печатает строку, которая не отображается в консоли, но меняет цвет текста, выводимого после, на переданный.
 */
fun setColor(color: TextColor) {
    print(color.prefix)
}


/*
 * Принимает строку и значение типа Boolean.
 * Печатает строку или цветную строку в зависимости от переданного Boolean-значения.
 * Цвет и добавляемый перед строкой префикс зависят от line.status.
 */
fun printLine(line: Line, is_colored: Boolean) {
    if (is_colored) {
        setColor(line.status.color)
    }
    print(line.status.prefix + line.s)
    if (is_colored) {
        setColor(TextColor.DEFAULT)
    }
    println()
}

fun main(args: Array<String>) {
    val (originalFile, updatedFile) = processInput(args)

    val comparisonFile = findChanges(originalFile, updatedFile)

    printDifference(comparisonFile)
}
