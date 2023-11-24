
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String, private val json: Json) {

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Long): Response {
        val urlGetUpdates = "$BASE_URL$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return json.decodeFromString(response.body())
    }

    fun handleUpdate(
        update: Update,
        trainers: HashMap<Long, LearnWordsTrainer>
    ) {

        val message = update.message?.text
        val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
        val data = update.callbackQuery?.data

        val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt")}

        if (message?.lowercase() == "/start") {
            sendMenu(chatId)
        }

        if (data?.lowercase() == STATISTICS_CLICKED) {
            sendMessage(
                chatId,
                "Выучено ${trainer.getStatistics().learned} из ${trainer.getStatistics().total} слов | ${trainer.getStatistics().percent}%")
        }

        if (data?.lowercase() == LEARN_WORDS_CLICKED) {
            checkNextQuestionAndSend(trainer, chatId)
        }

        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX, true) == true) {
            val index = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(index)) {
                sendMessage(chatId, "Правильно")
            } else {
                sendMessage(
                    chatId,
                    "\"Не правильно: ${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}\"")
            }
            checkNextQuestionAndSend(trainer, chatId)
        }

        if (data == RESET_CLICKED) {
            trainer.resetProgress()
            sendMessage(chatId, "Прогресс сброшен")
        }

        if (data == MENU_CLICKED) {
            sendMenu(chatId)
        }
    }

    private fun sendMenu(chatId: Long): String {
        val urlSendMessage = "$BASE_URL$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(listOf(
                    InlineKeyboard(text = "Изучать слова",
                        callbackData = LEARN_WORDS_CLICKED),
                    InlineKeyboard(text = "Статистика",
                        callbackData = STATISTICS_CLICKED),
                ),
                    listOf(
                        InlineKeyboard(
                            text = "Сбросить прогресс",
                            callbackData = RESET_CLICKED),
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    private fun sendMessage(
        chatId: Long,
        message: String
    ): String {
        val urlSendMessage = "$BASE_URL$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    private fun sendQuestion(
        chatId: Long,
        question: Question,
    ): String {
        val urlSendMessage = "$BASE_URL$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                inlineKeyboard =  question.variants.mapIndexed { index, word ->
                    listOf(
                        InlineKeyboard(
                            text = word.translate,
                            callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                        )
                    )
                } + listOf(
                    listOf(
                        InlineKeyboard(
                            text = "Меню",
                            callbackData = MENU_CLICKED
                        )
                    )
                )

            )

        )

        val requestBodyString = json.encodeToString(requestBody)

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    private fun checkNextQuestionAndSend(
        trainer: LearnWordsTrainer,
        chatId: Long
    ) {
        val nextQuestion = trainer.getNextQuestion()
        if (nextQuestion == null) {
            val send: String = sendMessage(chatId, "Вы выучили все слова в базе")
            println(send)
        } else {
            val send: String = sendQuestion(chatId, nextQuestion)
            println(send)
        }
    }
}

const val BASE_URL = "https://api.telegram.org/bot"
const val LEARN_WORDS_CLICKED = "lear_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val RESET_CLICKED = "reset_clicked"
const val MENU_CLICKED = "menu_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"