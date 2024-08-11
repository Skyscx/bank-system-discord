package data.localisation

import App
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*

class LocalisationManager(private val app: App) {
    private val locales = mutableMapOf<String, YamlConfiguration>()
    private lateinit var currentLocale: String

    init {
        loadCurrentLocale()
        loadLocaleFile(currentLocale)
    }

    private fun loadCurrentLocale() {
        val configFile = File(app.dataFolder, "config.yml")
        if (configFile.exists()) {
            val config = YamlConfiguration.loadConfiguration(configFile)
            currentLocale = config.getString("locale", "en").toString()
        } else {
            currentLocale = "en"
        }
    }

    private fun loadLocaleFile(locale: String) {
        val localeFile = File(app.dataFolder, "locales/$locale.yml")
        if (localeFile.exists()) {
            val config = YamlConfiguration.loadConfiguration(localeFile)
            locales[locale] = config
        } else {
            app.logger.warning("Locale file for '$locale' not found. Falling back to default locale 'en'.")
            loadLocaleFile("en")
        }
    }

    fun getMessage(key: String, vararg replacements: Pair<String, String>): String {
        val config = locales[currentLocale]
        val message = config?.getString(key) ?: "Message not found"
        return replaceVariables(message, *replacements)
    }
    // бесполезная фигня
    fun getMessageForPlayer(player: Player, key: String, vararg replacements: Pair<String, String>): String {
        val locale = player.locale.lowercase(Locale.getDefault())
        return getMessageForLocale(locale, key, *replacements)
    }

    private fun getMessageForLocale(locale: String, key: String, vararg replacements: Pair<String, String>): String {
        val config = locales[locale]
        val message = config?.getString(key) ?: "Message not found"
        return replaceVariables(message, *replacements)
    }

    private fun replaceVariables(message: String, vararg replacements: Pair<String, String>): String {
        var result = message
        for ((variable, value) in replacements) {
            result = result.replace("$$variable", value)
        }
        return result
    }
}