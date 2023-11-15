package additional

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)

fun main() {

    val wordsFile: File = File("words.txt")
    val dictionary = mutableListOf<Word>()

    val lines = wordsFile.readLines()
    lines.forEach { line ->
        val line = line.split("|")
        val word = Word(
            original = line[0],
            translate = line[1],
            correctAnswersCount = line.getOrNull(2)?.toIntOrNull() ?: 0
        )

        dictionary.add(word)
    }

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        val answer = readln()
        when(answer) {
            "1" -> println("Вы нажали 1")
            "2" -> {
                val learnedWordsCount = dictionary.filter { it.correctAnswersCount >= 3 }.count()
                println("Выучено $learnedWordsCount из ${dictionary.count()} слов | ${100 / dictionary.count() * learnedWordsCount}%")
            }
            "0" -> {
                println("Вы нажали 0")
                return
            }
            else -> println("неверное нажатие")
        }
    }

}
