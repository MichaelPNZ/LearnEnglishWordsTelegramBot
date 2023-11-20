fun main(args: Array<String>) {

    val telegramBotService = TelegramBotService()
    val botToken = args[0]
    var updateId = 0
    var chatId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(botToken, updateId)
        println(updates)

        val messageRegex = toRegex("\"text\":\"(.+?)\"", updates)
        val updateIdRegex = toRegex("\"update_id\":(\\d+)", updates)
        val chatIdRegex = toRegex("\"id\":(\\d+)", updates)

        if (updateIdRegex != null) updateId = updateIdRegex.toInt() + 1
        if (chatIdRegex != null) chatId = chatIdRegex.toInt()

        if (messageRegex != null && messageRegex == "Hello") {
            val sendMessage: String = telegramBotService.sendMessage(botToken, chatId.toString(), "Hello")
            println(sendMessage)
        }

    }

}

fun toRegex(query: String, updates: String): String? {
    val messageTextRegex: Regex = query.toRegex()
    val matchResult: MatchResult? = messageTextRegex.find(updates)
    val group = matchResult?.groups

    return group?.get(1)?.value
}