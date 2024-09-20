//package bank.commands
//
//import App
//import org.bukkit.command.Command
//import org.bukkit.command.CommandExecutor
//import org.bukkit.command.CommandSender
//import org.bukkit.scheduler.BukkitRunnable
//
//class PluginReloadCommand(private val app: App) : CommandExecutor {
//    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
//        if (sender.hasPermission("yourplugin.reload")) {
//            reloadPlugin()
//            sender.sendMessage("Плагин успешно перезагружен!")
//        } else {
//            sender.sendMessage("У вас нет прав для выполнения этой команды.")
//        }
//        return true
//    }
//
//    private fun reloadPlugin() {
//        val pluginManager = app.server.pluginManager
//        val plugin = pluginManager.getPlugin("Plugin")
//
//        if (plugin != null) {
//            object : BukkitRunnable() {
//                override fun run() {
//                    pluginManager.disablePlugin(plugin)
//                    // Асинхронное включение плагина с задержкой
//                    object : BukkitRunnable() {
//                        override fun run() {
//                            try {
//                                pluginManager.enablePlugin(plugin)
//                            } catch (e: Exception) {
//                                app.logger.severe("Ошибка при перезагрузке плагина: ${e.message}")
//                            }
//                        }
//                    }.runTaskLater(app, 20L) // Задержка в 1 секунду (20 тиков)
//                }
//            }.runTask(app)
//        } else {
//            app.logger.severe("Плагин не найден!")
//        }
//    }
//}