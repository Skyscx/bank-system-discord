//package discord.dsbot.accountbinder
//
//import database.Database
//import discord.Functions
//import net.dv8tion.jda.api.Permission
//import net.dv8tion.jda.api.entities.Member
//import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
//import net.dv8tion.jda.api.hooks.ListenerAdapter
//import net.dv8tion.jda.api.interactions.components.ActionRow
//import net.dv8tion.jda.api.interactions.components.text.TextInput
//import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
//import net.dv8tion.jda.api.interactions.modals.Modal
//import org.bukkit.configuration.file.FileConfiguration
//import java.util.*
//
//class CommandAccountBinder(private val database: Database, config: FileConfiguration) : ListenerAdapter() {
//    private val functions = Functions()
//    private val categoryBankId = config.getString("category-bank-id")
//
//    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
//        if (event.name != "link-bank") return
//        val subject = TextInput.create("subject", "Nickname", TextInputStyle.SHORT)
//            .setPlaceholder("Введите ваше игровое имя для привязки к Discord.")
//            .setMinLength(10)
//            .setMaxLength(100)
//            .build()
//        val modal = Modal.create("binder", "Подключение аккаунта")
//            .addComponents(ActionRow.of(subject))
//            .build()
//        event.replyModal(modal).queue()
//    }
//
//    fun createTextChannel(member: Member, name: String?) {
//        val guild = member.guild
//        val category = guild.getCategoryById(categoryBankId.toString())
//        val code = "098392" //TODO: СДЕЛАТЬ ГЕНЕРАЦИЮ И СЕССИЮ КОДА.
//        category!!.createTextChannel(name!!)
//            .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
//            .addPermissionOverride(guild.publicRole, null, EnumSet.of(Permission.VIEW_CHANNEL))
//            .queue() // this actually sends the request to discord.
//            { channel ->
//                channel.sendMessage("Уважаемый, $name! Ваше заявление на подключение игрового аккаунта к SkyBank Bot успешно зарегистрировано. \n" +
//                        "Чтобы продолжить процедуру подключения, вам необходимо вести уникальный единоразовый код в игровой чат\n" +
//                        "Зайдите в игру, откройте чат и введите следующую команду: \n" +
//                        "/link-bank $code").queue()
//            }
//    }
//
//}