package additional

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
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
                    val unlearnedWords = dictionary.filter { it.correctAnswersCount < MIN_COUNT_CORRECT_ANSWERS }
                    val learnedWords = dictionary.filter { it.correctAnswersCount >= MIN_COUNT_CORRECT_ANSWERS }

                    if (unlearnedWords.isEmpty()) {
                        println("Вы выучили все слова!")
                        return
                    } else {
                        val wordForQuestion = unlearnedWords.shuffled().firstOrNull()

                        println("Выберите правильный перевод слова: ${wordForQuestion?.original} или нажмите 0 для выхода в главное меню")

                        var answerOptions = listOf <Word>()

                        answerOptions = if (unlearnedWords.count() >= COUNT_OPTIONALS) unlearnedWords.shuffled().take(COUNT_OPTIONALS)
                        else (unlearnedWords + learnedWords.shuffled()).shuffled().take(COUNT_OPTIONALS)

                        val variants = answerOptions.mapIndexed { index, option -> "${index + 1}: ${option.translate}" }

                        println(variants.joinToString(separator = "\n", postfix = "\n0 - выход"))

                        val userAnswer = readln().toIntOrNull()

                        when(userAnswer) {
                            in 1..4 -> {
                                if (userAnswer != null && answerOptions[userAnswer - 1].translate == wordForQuestion?.translate) {
                                    println("Правильно!")
                                    wordForQuestion.correctAnswersCount++
                                    saveDictionary(dictionary)
                                } else println("НЕ правильно!")
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

fun saveDictionary(dictionary: List<Word>) {
    val wordsFile: File = File("words.txt")
    val fileContent = dictionary.joinToString("\n") { "${it.original}|${it.translate}|${it.correctAnswersCount}" }
    wordsFile.writeText(fileContent)
}

const val MIN_COUNT_CORRECT_ANSWERS = 3
const val COUNT_OPTIONALS = 4