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
                    val unlearnedWords = mutableListOf<String>()

                    // Наполняем список невыученных слов
                    dictionary.forEach { if (it.correctAnswersCount < 3) unlearnedWords.add(it.original) }

                    val wordForQuestion = unlearnedWords.shuffled().firstOrNull() // Слово для вопроса/правильный ответ

                    // Проверяем есть ли невыученные слова
                    if (unlearnedWords.isEmpty()) {
                        println("Вы выучили все слова!")
                        return
                    }

                    println("Выберите правильный первеод слова: $wordForQuestion или нажмите 0 для выхода в главное меню")

                    // Печатаем список рандомных ответов
                    unlearnedWords.take(4).shuffled().forEachIndexed { index, element ->  println("${index + 1}: $element")  }

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
                val learnedWordsCount = dictionary.filter { it.correctAnswersCount >= 3 }.count()
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
