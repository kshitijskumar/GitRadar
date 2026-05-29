package com.github.kshitijskumar.gitradar.settings

import com.github.kshitijskumar.gitradar.services.GitRadarApplicationService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JTextField

class GitRadarAppSettingsConfigurable : SearchableConfigurable {

    private val log = Logger.getInstance(GitRadarAppSettingsConfigurable::class.java)
    private var panel: JPanel? = null
    private var usernameField: JTextField? = null
    private var patField: JPasswordField? = null

    override fun getId(): String = "com.github.kshitijskumar.gitradar.settings"

    override fun getDisplayName(): String = "GitRadar"

    override fun createComponent(): JComponent {
        val p = JPanel(GridBagLayout())
        val username = JTextField(30)
        val pat = JPasswordField(30)

        val labelConstraints = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            insets = Insets(4, 0, 4, 8)
        }
        val fieldConstraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            gridwidth = GridBagConstraints.REMAINDER
            insets = Insets(4, 0, 4, 0)
        }

        p.add(JLabel("GitHub username:"), labelConstraints)
        p.add(username, fieldConstraints)
        p.add(JLabel("Personal Access Token:"), labelConstraints)
        p.add(pat, fieldConstraints)

        // Push content to top
        p.add(JPanel(), GridBagConstraints().apply {
            weighty = 1.0
            gridwidth = GridBagConstraints.REMAINDER
            fill = GridBagConstraints.BOTH
        })

        usernameField = username
        patField = pat
        panel = p

        reset()
        return p
    }

    override fun isModified(): Boolean {
        val creds = GitRadarApplicationService.getInstance().accountFlow.value
        val storedUsername = creds?.username.orEmpty()
        val storedPat = creds?.pat.orEmpty()
        return usernameField?.text.orEmpty() != storedUsername ||
            String(patField?.password ?: CharArray(0)) != storedPat
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val username = usernameField?.text?.trim().orEmpty()
        val pat = String(patField?.password ?: CharArray(0)).trim()

        log.info("[GitKshitij1] apply: username='$username' patBlank=${pat.isBlank()}")

        if (username.isBlank()) throw ConfigurationException("GitHub username must not be empty.")
        if (pat.isBlank()) throw ConfigurationException("Personal Access Token must not be empty.")

        GitRadarApplicationService.getInstance().launchSaveAccount(username = username, pat = pat)
        log.info("[GitKshitij1] apply: save launched")
    }

    override fun reset() {
        val creds = GitRadarApplicationService.getInstance().accountFlow.value
        log.info("[GitKshitij1] reset: accountFlow.value=${if (creds == null) "null" else "present (username=${creds.username})"}")
        usernameField?.text = creds?.username.orEmpty()
        patField?.text = creds?.pat.orEmpty()
    }

    override fun disposeUIResources() {
        panel = null
        usernameField = null
        patField = null
    }
}
