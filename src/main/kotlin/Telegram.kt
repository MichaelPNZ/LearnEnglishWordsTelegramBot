fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var lastUpdateId = 0
    val trainer = LearnWordsTrainer()

    val messageRegex = "\"text\":\"(.+?)\"".toRegex()
    val updateIdRegex = "\"update_id\":(\\d+)".toRegex()
    val chatIdRegex = "\"id\":(\\d+)".toRegex()
    val dataRegex = "\"data\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(TIMER)
        val updates: String = telegramBotService.getUpdates(lastUpdateId)
        println(updates)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        lastUpdateId = updateId + 1

        val message = messageRegex.find(updates)?.groups?.get(1)?.value
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toInt()
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (message?.lowercase() == "/start") {
            val sendMessage: String = telegramBotService.sendMenu(chatId.toString())
            println(sendMessage)
        }

        if (data != null && data.lowercase() == STATISTICS_CLICKED) {
            val sendMessage: String = telegramBotService.sendMessage(
                chatId.toString(),
                "Выучено ${trainer.getStatistics().learned} из ${trainer.getStatistics().total} слов | ${trainer.getStatistics().percent}%")
            println(sendMessage)
        }

        if (chatId != null && data?.lowercase() == LEARN_WORDS_CLICKED) {
            telegramBotService.checkNextQuestionAndSend(trainer, chatId)
        }

        if (chatId != null && data != null && data.startsWith(CALLBACK_DATA_ANSWER_PREFIX, true)) {
            val index = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(index)) {
                val sendMessage: String = telegramBotService.sendMessage(chatId.toString(), "Правильно")
                println(sendMessage)
            } else {
                val sendMessage: String = telegramBotService.sendMessage(
                    chatId.toString(),
                    "\"Не правильно: ${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}\"")
                println(sendMessage)
            }
            telegramBotService.checkNextQuestionAndSend(trainer, chatId)
        }

    }

}

const val TIMER: Long = 2000