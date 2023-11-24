package additional

import LearnWordsTrainer
import Question


fun Question.asConsoleString() : String {
    val variants = this.variants
        .mapIndexed { index, option -> "${index + 1}: ${option.translate}" }
        .joinToString(separator = "\n")
    return "$variants\n 0 - выйти в меню"
}

fun main() {

    val trainer = try {
        LearnWordsTrainer("words.txt", 3, 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        val answer = readln()
        when(answer) {
            "1" -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("Вы выучили все слова!")
                        break
                    } else {
                        println("Выберите правильный перевод слова: ${question.correctAnswer.original} или нажмите 0 для выхода в главное меню")
                        println(question.asConsoleString())

                        when(val userAnswer: Int? = readln().toIntOrNull()) {
                            in 1..4 -> {
                                if (trainer.checkAnswer(userAnswer?.minus(1))) println("Правильно!")
                                else println("НЕ правильно!")
                            }
                            0 -> {
                                println("Выходим в главное меню")
                                break
                            }
                            else -> println("неверное нажатие")
                        }

                    }

                }
            }
            "2" -> {
                val statistics = trainer.getStatistics()
                println("Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%")
            }
            "0" -> {
                println("Вы нажали: Выход")
                return
            }
            else -> println("неверное нажатие")
        }
    }

}

