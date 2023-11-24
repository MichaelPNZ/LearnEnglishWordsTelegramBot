
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

data class Statistics(
    val learned: Int,
    val total: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val learnedAnswerCount: Int = 3,
    private val countOfQuestionWords: Int = 4,
    ) {

    var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val learned = dictionary.count { it.correctAnswersCount >= learnedAnswerCount }
        val total = dictionary.count()
        val percent = 100 / dictionary.count() * learned
        return Statistics(learned, total, percent)
    }

    fun getNextQuestion(): Question? {
        val unlearnedWords = dictionary.filter { it.correctAnswersCount < learnedAnswerCount }
        if (unlearnedWords.isEmpty()) return null

        val answerOptions = if (unlearnedWords.size >= countOfQuestionWords) unlearnedWords.shuffled().take(
            countOfQuestionWords
        )
        else {
            val learnedWords = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.shuffled()
            (unlearnedWords + learnedWords.take(countOfQuestionWords - unlearnedWords.size)).shuffled()
        }
        val wordForQuestion = answerOptions.random()

        question = Question(
            variants = answerOptions,
            correctAnswer = wordForQuestion,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        try {
            val wordsFile: File = File(fileName)
            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }
            val dictionary = mutableListOf<Word>()
            wordsFile.readLines().forEach {
                val line = it.split("|")
                dictionary.add(Word(original = line[0], translate = line[1], correctAnswersCount = line.getOrNull(2)?.toIntOrNull() ?: 0))
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("некорректный файл")
        }
    }

    private fun saveDictionary() {
        val wordsFile: File = File(fileName)
        val fileContent = dictionary.joinToString("\n") { "${it.original}|${it.translate}|${it.correctAnswersCount}" }
        wordsFile.writeText(fileContent)
    }

    fun resetProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }

}