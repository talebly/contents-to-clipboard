package com.talebly.contentstoclipboard

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.ui.ComboBox
import org.jetbrains.annotations.Nls
import javax.swing.*
import java.awt.*

class ContentsToClipboardConfigurable : Configurable {

    private val settings = ContentsToClipboardSettings.getInstance()

    private lateinit var mainPanel: JPanel
    private lateinit var notificationsCheckbox: JCheckBox
    private lateinit var separatorLabel: JLabel
    private lateinit var separatorTextField: JTextField
    private lateinit var pathTypeLabel: JLabel
    private lateinit var pathTypeComboBox: ComboBox<String>

    class Provider : ConfigurableProvider() {
        override fun createConfigurable(): Configurable {
            return ContentsToClipboardConfigurable()
        }
    }

    override fun createComponent(): JComponent {
        // Use GridBagLayout for precise control over component placement
        mainPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            insets = Insets(0, 0, 0, 0)
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        }

        var row = 0

        // Notifications Checkbox
        notificationsCheckbox = JCheckBox("Show notifications after copying", settings.state.showNotifications)
        gbc.gridx = 0
        gbc.gridy = row++
        gbc.gridwidth = 2
        mainPanel.add(notificationsCheckbox, gbc)

        // Reset gridwidth for subsequent components
        gbc.gridwidth = 1

        // Separator Label
        separatorLabel = JLabel("File Separator (use placeholders):")
        separatorLabel.toolTipText = """
            Customize the separator between files.
            Placeholders:
            - %FILE_PATH%: The file path (absolute or relative based on your selection).
            - %FILE_NAME%: The name of the file.
            - %RELATIVE_PATH%: The path relative to the project root.
        """.trimIndent()
        gbc.gridx = 0
        gbc.gridy = row
        gbc.weightx = 0.0
        mainPanel.add(separatorLabel, gbc)

        // Separator TextField (Single Line)
        separatorTextField = JTextField(settings.state.fileSeparator)
        gbc.gridx = 1
        gbc.gridy = row++
        gbc.weightx = 1.0
        mainPanel.add(separatorTextField, gbc)

        // Path Type Label
        pathTypeLabel = JLabel("File Path Type:")
        gbc.gridx = 0
        gbc.gridy = row
        gbc.weightx = 0.0
        mainPanel.add(pathTypeLabel, gbc)

        // Path Type ComboBox
        pathTypeComboBox = ComboBox(arrayOf("Absolute Path", "Relative to Project Root"))
        pathTypeComboBox.selectedIndex = if (settings.state.useRelativePath) 1 else 0
        gbc.gridx = 1
        gbc.weightx = 1.0
        mainPanel.add(pathTypeComboBox, gbc)

        return mainPanel
    }

    override fun isModified(): Boolean {
        return notificationsCheckbox.isSelected != settings.state.showNotifications ||
                separatorTextField.text != settings.state.fileSeparator ||
                (pathTypeComboBox.selectedIndex == 1) != settings.state.useRelativePath
    }

    override fun apply() {
        settings.state.showNotifications = notificationsCheckbox.isSelected

        val separatorInput = separatorTextField.text.trim()
        if (separatorInput.isEmpty()) {
            // Provide a default value if the input is empty
            settings.state.fileSeparator = "=== %FILE_PATH% ===\n"
        } else {
            settings.state.fileSeparator = separatorInput
        }

        settings.state.useRelativePath = pathTypeComboBox.selectedIndex == 1
    }

    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String {
        return "Contents to Clipboard"
    }

    override fun reset() {
        notificationsCheckbox.isSelected = settings.state.showNotifications
        separatorTextField.text = settings.state.fileSeparator
        pathTypeComboBox.selectedIndex = if (settings.state.useRelativePath) 1 else 0
    }
}