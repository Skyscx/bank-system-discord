package data

import App
import org.bukkit.configuration.file.FileConfiguration

class Config private constructor(private val plugin: App) {
    private lateinit var config: FileConfiguration

    companion object {
        @Volatile
        private var instance: Config? = null

        fun getInstance(plugin: App): Config =
            instance ?: synchronized(this) {
                instance ?: Config(plugin).also { instance = it }
            }
    }

    fun loadConfig() {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()
        config = plugin.config
    }

    fun getConfig(): FileConfiguration {
        return config
    }

    fun getString(path: String): String? {
        return config.getString(path)
    }

    fun getInt(path: String): Int {
        return config.getInt(path)
    }

    fun getBoolean(path: String): Boolean {
        return config.getBoolean(path)
    }
}