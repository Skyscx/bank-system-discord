import org.bukkit.plugin.java.JavaPlugin

lateinit var app: App

class App : JavaPlugin() {
    override fun onEnable() {
        app = this
    }
}