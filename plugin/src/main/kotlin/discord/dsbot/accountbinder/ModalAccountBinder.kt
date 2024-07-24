//package discord.dsbot.accountbinder
//
//import database.Database
//import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
//import net.dv8tion.jda.api.hooks.ListenerAdapter
//import org.bukkit.configuration.file.FileConfiguration
//
//class ModalListener(private val database: Database, config: FileConfiguration) : ListenerAdapter() {
//    private val commandAccountBinder = CommandAccountBinder(database, config)
//    override fun onModalInteraction(event: ModalInteractionEvent) {
//        if (event.modalId == "binder") {
//
//            val subject = event.getValue("subject")?.asString ?: return
//            event.member?.let { commandAccountBinder.createTextChannel(it, subject) }
//
//            event.reply("Thanks for your request!").setEphemeral(true).queue()
//        }
//    }
//}