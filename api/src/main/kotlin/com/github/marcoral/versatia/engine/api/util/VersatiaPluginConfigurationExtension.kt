package com.github.marcoral.versatia.engine.api.util

import com.github.marcoral.versatia.engine.api.VersatiaEngineDevtools
import com.github.marcoral.versatia.engine.api.plugin.VersatiaPlugin
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

fun joinPath(vararg pathElements: String) = arrayOf(*pathElements).joinToString(separator = File.separatorChar.toString())

fun VersatiaPlugin.processEveryFileInDirectory(pluginRelativePath: String, throwIfNotExists: Boolean = false, action: (ConfigurationSection) -> Unit)
    = processFileElement(pluginRelativePath, false) {
        Files.walkFileTree(it.toPath(), object: SimpleFileVisitor<Path>() {
            override fun visitFile(path: Path, attrs: BasicFileAttributes): FileVisitResult {
                action.invoke(YamlConfiguration.loadConfiguration(path.toFile()))
                return FileVisitResult.CONTINUE
            }
        })
    }

internal fun processFileFromEngineDirectoryInternal(fileName: String, throwIfNotExists: Boolean, action: (ConfigurationSection) -> Unit)
    = VersatiaEngineDevtools.engineInstance.processFileElement(fileName, throwIfNotExists) {
        action.invoke(YamlConfiguration.loadConfiguration(it))
    }

private fun VersatiaPlugin.processFileElement(pluginRelativePath: String, throwIfNotExists: Boolean, actionOnFileElement: (File) -> Unit) {
    val fileElement = File(this.bukkitPlugin.dataFolder, pluginRelativePath)
    if(!fileElement.exists()) {
        if (throwIfNotExists)
            throw FileNotFoundException("${fileElement.absolutePath} does not exist")
        return
    }
    actionOnFileElement.invoke(fileElement)
}