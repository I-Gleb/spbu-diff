/*
 * Получает параметры командной строки. При необходимости запрашивает данные у пользователя.
 * Производится считываение и обработка входных даныых.
 * Функция возращает два сравниваемых файла.
 */
fun processInput(args: Array<String>): Pair<List<String>, List<String> > {
    TODO()
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
 * Получает файл-сравнение.
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
