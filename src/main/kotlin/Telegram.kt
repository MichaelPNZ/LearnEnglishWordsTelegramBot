fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var updateId = 0
    var chatId = 0
    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        val messageRegex = toRegex("\"text\":\"(.+?)\"", updates)
        val updateIdRegex = toRegex("\"update_id\":(\\d+)", updates)
        val chatIdRegex = toRegex("\"id\":(\\d+)", updates)
        val dataRegex = toRegex("\"data\":\"(.+?)\"", updates)

        if (updateIdRegex != null) updateId = updateIdRegex.toInt() + 1
        if (chatIdRegex != null) chatId = chatIdRegex.toInt()

        if (messageRegex != null && messageRegex.lowercase() == "/start") {
            val sendMessage: String = telegramBotService.sendMenu(chatId.toString())
            println(sendMessage)
        }

        if (dataRegex != null && dataRegex.lowercase() == STATISTICS_CLICKED) {
            val sendMessage: String = telegramBotService.sendMessage(
                chatId.toString(),
                "Выучено ${trainer.getStatistics().learned} из ${trainer.getStatistics().total} слов | ${trainer.getStatistics().percent}%")
            println(sendMessage)
        }

        if (dataRegex != null && dataRegex.lowercase() == LEARN_WORDS_CLICKED) {
            telegramBotService.checkNextQuestionAndSend(trainer, chatId)
        }

        if (dataRegex != null && dataRegex.startsWith(CALLBACK_DATA_ANSWER_PREFIX, true)) {
            val index = dataRegex.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
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

fun toRegex(query: String, updates: String): String? {
    val messageTextRegex: Regex = query.toRegex()
    val matchResult: MatchResult? = messageTextRegex.find(updates)
    val group = matchResult?.groups

    return group?.get(1)?.value
}

