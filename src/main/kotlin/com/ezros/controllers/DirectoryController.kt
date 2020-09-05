package com.ezros.controllers

import javafx.stage.DirectoryChooser
import javafx.stage.Window
import java.io.*
import java.util.*

class DirectoryController(private val primaryStage: Window? = null) {
    private val propertiesFile = System.getProperty("user.dir") + "/yam.properties"

    fun read(path: String = ""): List<File> {
        val files: MutableList<File> = mutableListOf()

        File(path).walkTopDown().filter { file ->
            file.isFile && file.extension == "sql"
        }.forEach { file ->
            files.add(file)
        }

        return files
    }

    fun renameDirectory(path: String, newDirectoryName: String) = File(path).renameTo(File(newDirectoryName))

    fun copyFiles(source: File, target: String, excludedFiles: List<String> = listOf()) {

        source.walkTopDown().filter { file -> file.isFile && !excludedFiles.contains(file.name) }.forEach { file ->
            val relativePath = file.relativeTo(source)
            print("$relativePath \n")
            file.copyTo(File("$target/$relativePath"), true)
        }

    }

    fun setSourceDirectory(): File? {
        val chooser = DirectoryChooser()
        val selectedDirectory = chooser.showDialog(primaryStage)

        chooser.title = "Deployment Folder"

        return selectedDirectory
    }

    fun setTargetDirectory(): File {
        val chooser = DirectoryChooser()
        val selectedDirectory = chooser.showDialog(primaryStage)

        chooser.title = "Target Folder"

        return selectedDirectory
    }

    fun getDefaults(): Properties {
        val properties = Properties()

        val inputStream = FileInputStream(propertiesFile)
        properties.load(inputStream)

        return properties
    }
}