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
            "1" -> {
                while (true) {
                    val unlearnedWords = dictionary.filter { it.correctAnswersCount < MIN_COUNT_CORRECT_ANSWERS }.map { it.original }

                    if (unlearnedWords.isEmpty()) {
                        println("Вы выучили все слова!")
                        return
                    } else {
                        val wordForQuestion = unlearnedWords.shuffled().firstOrNull()

                        println("Выберите правильный первеод слова: $wordForQuestion или нажмите 0 для выхода в главное меню")

                        val answerOptions = unlearnedWords.shuffled().take(4).mapIndexed { index, element -> "${index + 1}: $element" }

                        answerOptions.forEach { println(it) }
                    }

                    val answer1 = readln()

                    when(answer1) {
                        "1" -> println("Вы нажали 1")
                        "2" -> println("Вы нажали 2")
                        "3" -> println("Вы нажали 3")
                        "4" -> println("Вы нажали 4")
                        "0" -> {
                            println("Выходим в главное меню")
                            break
                        }
                        else -> println("неверное нажатие")
                    }

                }
            }
            "2" -> {
                val learnedWordsCount = dictionary.filter { it.correctAnswersCount >= MIN_COUNT_CORRECT_ANSWERS }.count()
                println("Выучено $learnedWordsCount из ${dictionary.count()} слов | ${100 / dictionary.count() * learnedWordsCount}%")
            }
            "0" -> {
                println("Вы нажали: Выход")
                return
            }
            else -> println("неверное нажатие")
        }
    }

}

const val MIN_COUNT_CORRECT_ANSWERS = 3