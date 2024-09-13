package com.talebly.contentstoclipboard

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VFileProperty
import com.intellij.openapi.vfs.VirtualFile
import java.awt.datatransfer.StringSelection
import java.nio.charset.StandardCharsets
import java.util.HashSet
import java.util.Stack


class ContentsToClipboardAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)

        if (files.isNullOrEmpty()) {
            val settings = ContentsToClipboardSettings.getInstance()
            if (settings.state.showNotifications) {
                val notification = NotificationGroupManager.getInstance()
                    .getNotificationGroup("Contents to Clipboard Notifications")
                    .createNotification(
                        "No Files Selected",
                        "Please select one or more files or directories to copy.",
                        NotificationType.WARNING
                    )
                notification.notify(e.project)
            }
            return
        }

        val combinedContent = StringBuilder()
        val processedFiles = HashSet<VirtualFile>()
        val fileStack = Stack<VirtualFile>()

        // Add all selected files and directories to the stack
        fileStack.addAll(files)

        // Access settings once to improve performance
        val settings = ContentsToClipboardSettings.getInstance()
        val separatorTemplate = settings.state.fileSeparator
        val useRelativePath = settings.state.useRelativePath

        while (fileStack.isNotEmpty()) {
            val file = fileStack.pop()

            // Skip if we've already processed this file
            if (!processedFiles.add(file)) {
                continue
            }

            // Skip symbolic links to avoid infinite loops
            if (file.`is`(VFileProperty.SYMLINK)) {
                continue
            }

            if (file.isDirectory) {
                // Add all children to the stack
                try {
                    val children = file.children
                    if (children != null) {
                        fileStack.addAll(children)
                    }
                } catch (ex: Exception) {
                    // Handle permission issues when accessing directory contents
                    ex.printStackTrace()
                }
            } else {
                try {
                    val content = String(file.contentsToByteArray(), StandardCharsets.UTF_8)

                    // Generate the separator by replacing placeholders
                    val project = e.project
                    val absolutePath = file.path
                    val fileName = file.name
                    val relativePath = if (project != null && project.basePath != null) {
                        val basePath = project.basePath!!
                        if (absolutePath.startsWith(basePath)) {
                            absolutePath.substring(basePath.length)
                        } else {
                            absolutePath
                        }
                    } else {
                        absolutePath
                    }

                    val filePath = if (useRelativePath) relativePath else absolutePath

                    val separator = separatorTemplate
                        .replace("%FILE_PATH%", filePath)
                        .replace("%FILE_NAME%", fileName)
                        .replace("%RELATIVE_PATH%", relativePath)

                    combinedContent.append(separator).append("\n")
                    combinedContent.append(content).append("\n\n")
                } catch (ex: Exception) {
                    // Handle permission issues or other I/O errors
                    ex.printStackTrace()
                }
            }
        }

        if (combinedContent.isEmpty()) {
            if (settings.state.showNotifications) {
                val notification = NotificationGroupManager.getInstance()
                    .getNotificationGroup("Contents to Clipboard Notifications")
                    .createNotification(
                        "No Files to Copy",
                        "The selected items contain no files to copy.",
                        NotificationType.WARNING
                    )
                notification.notify(e.project)
            }
            return
        }

        val clipboardContent = combinedContent.toString()
        CopyPasteManager.getInstance().setContents(StringSelection(clipboardContent))

        if (settings.state.showNotifications) {
            val notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("Contents to Clipboard Notifications")
                .createNotification(
                    "Contents Copied to Clipboard",
                    "The selected files have been combined and copied to your clipboard.",
                    NotificationType.INFORMATION
                )

            // Add an action to disable future notifications
            notification.addAction(object : NotificationAction("Don't Show Again") {
                override fun actionPerformed(event: AnActionEvent, notification: com.intellij.notification.Notification) {
                    settings.state.showNotifications = false

                    // Optionally notify the user that notifications have been disabled
                    val project: Project? = event.project
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Contents to Clipboard Notifications")
                        .createNotification(
                            "Notifications Disabled",
                            "You will no longer receive notifications from Contents to Clipboard.",
                            NotificationType.INFORMATION
                        ).notify(project)

                    notification.expire() // Close the original notification
                }
            })

            notification.notify(e.project)
        }
    }

    override fun update(e: AnActionEvent) {
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        e.presentation.isEnabledAndVisible = !files.isNullOrEmpty()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}