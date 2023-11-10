package additional

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
    var learnedWordsCount: Int = 0,
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
            correctAnswersCount = line.getOrNull(2)?.toIntOrNull() ?: 0,
            learnedWordsCount = line.getOrNull(3)?.toIntOrNull() ?: 0
        )
        dictionary.add(word)
    }
    println(dictionary)

}