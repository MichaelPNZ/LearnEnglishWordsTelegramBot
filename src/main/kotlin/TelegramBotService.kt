
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService(private val botToken: String) {

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$BASE_URL$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: String, text: String): String {
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )
        println(encoded)

        val urlSendMessage = "$BASE_URL$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMenu(chatId: String): String {
        val urlSendMessage = "$BASE_URL$botToken/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "$LEARN_WORDS_CLICKED"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "${STATISTICS_CLICKED}"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }


    fun sendQuestion(chatId: String, question: Question?): String {
        sendMessage(chatId, question?.correctAnswer?.original ?: "Error")

        val variants = question?.variants?.mapIndexed { index, _ -> "$CALLBACK_DATA_ANSWER_PREFIX$index" }
        val urlSendMessage = "$BASE_URL$botToken/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Варианты ответов",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "${question?.variants?.get(0)?.translate}",
                                "callback_data": "${variants?.get(0)}"
                            },
                            {
                                "text": "${question?.variants?.get(1)?.translate}",
                                "callback_data": "${variants?.get(1)}"
                            },
                            {
                                "text": "${question?.variants?.get(2)?.translate}",
                                "callback_data": "${variants?.get(2)}"
                            },
                            {
                                "text": "${question?.variants?.get(3)?.translate}",
                                "callback_data": "${variants?.get(3)}"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Int) {
        val nextQuestion = trainer.getNextQuestion()?.correctAnswer
        if (nextQuestion == null) {
            val send: String = sendMessage(chatId.toString(), "Вы выучили все слова в базе")
            println(send)
        } else {
            val send: String = sendQuestion(chatId.toString(), trainer.getNextQuestion())
            println(send)
        }
    }

}

const val BASE_URL = "https://api.telegram.org/bot"
const val LEARN_WORDS_CLICKED = "lear_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
