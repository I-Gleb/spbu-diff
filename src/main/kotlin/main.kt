import java.io.File
import java.lang.Integer.max

/*
 * Принимает параметры командной строки. При необходимости запрашивает данные у пользователя.
 * Производится считываение и обработка входных даныых.
 * Функция возращает два сравниваемых файла.
 */
fun processInput(args: Array<String>): Pair<List<String>, List<String> > {
    return Pair(File(args[0]).bufferedReader().readLines(), File(args[1]).bufferedReader().readLines())
}

/*
 * Принимает две последовательности.
 * Возращает их наибольшую общую подпоследовательность.
 */
fun longestCommonSubseq(a : List<Any>, b : List<Any>): List<Any> {
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
    val lcs = mutableListOf<Any>()
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
 * Получает два файла
 * Файлы сравниваются.
 * Функция возращает файл-сравнение, в котором у каждой строки есть пометка о её статусе
 */
fun findChanges(originalFile : List<String>, updatedFile : List<String>): List<Pair<Int, String> > {
    TODO()
}


/*
 * Принимает файл-сравнение.
 * Выводит разницу между файлами по файлу-сравнению.
 */
fun printDifference(comparisonFile : List<Pair<Int, String> >) {
    TODO()
}

fun main(args: Array<String>) {
    // получили входные файлы
    val (originalFile, updatedFile) = processInput(args)

    // постоили по ним файл-сравнение
    val comparisonFile = findChanges(originalFile, updatedFile)

    // вывели отчёт об изменениях между файлами
    printDifference(comparisonFile)
}
