package data.localisation

import App
import App.Companion.configPlugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class LocalisationManager(private val app: App) {
    private val locales = mutableMapOf<String, YamlConfiguration>()
    private lateinit var currentLocale: String

    private val diamondOre = configPlugin.getString("name-diamond-ore") ?: "unavailable"
    private  val deepslateDiamondOre = configPlugin.getString("name-deepslate-diamond-ore") ?: "unavailable"
    private val replacementsMap = mapOf(
        "DEEPSLATE_DIAMOND_ORE" to deepslateDiamondOre,
        "DIAMOND_ORE" to diamondOre,
    )

    init {
        loadCurrentLocale()
        loadLocaleFile(currentLocale)
    }

    private fun loadCurrentLocale() {
        val configFile = File(app.dataFolder, "config.yml")
        if (configFile.exists()) {
            val config = YamlConfiguration.loadConfiguration(configFile)
            currentLocale = "messages_" + config.getString("locale", "messages_en").toString()
        } else {
            currentLocale = "messages_en"
        }
    }

    private fun loadLocaleFile(locale: String) {
        if (locales.containsKey(locale)) {
            app.logger.info("Locale '$locale' already loaded.")
            return
        }

        val localeFile = File(app.dataFolder, "locales/$locale.yml")
        if (localeFile.exists()) {
            val config = YamlConfiguration.loadConfiguration(localeFile)
            locales[locale] = config
            app.logger.info("Successfully loaded locale '$locale'.")
        } else {
            app.logger.warning("Locale file for '$locale' not found. Falling back to default locale 'en'.")
            if (locale != "messages_en") {
                loadLocaleFile("messages_en")
            } else {
                app.logger.severe("Default locale file 'en' not found. Please check your configuration.")
            }
        }
    }

    fun getMessage(key: String, vararg replacements: Pair<String, String>): String {
        val config = locales[currentLocale]
        val message = config?.getString(key) ?: "Message not found"
        return replaceVariables(message, *replacements)
    }

    private fun replaceVariables(message: String, vararg replacements: Pair<String, String>): String {
        var result = message
        for ((variable, value) in replacements) {
            result = result.replace("$$variable", value)
        }
        for ((key, value) in replacementsMap) {
            result = result.replace(key, value)
        }
        return result
    }

}