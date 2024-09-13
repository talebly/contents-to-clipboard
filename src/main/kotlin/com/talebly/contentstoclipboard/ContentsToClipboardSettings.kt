package com.talebly.contentstoclipboard

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service
@State(
    name = "ContentsToClipboardSettings",
    storages = [Storage("ContentsToClipboardSettings.xml")]
)
class ContentsToClipboardSettings : PersistentStateComponent<ContentsToClipboardSettings.State> {

    data class State(
        var showNotifications: Boolean = true,
        var fileSeparator: String = "=== %FILE_PATH% ===\n",
        var useRelativePath: Boolean = true
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): ContentsToClipboardSettings {
            return ApplicationManager.getApplication()
                .getService(ContentsToClipboardSettings::class.java)
        }
    }
}