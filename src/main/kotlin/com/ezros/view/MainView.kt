package com.ezros.view

import com.ezros.Styles
import com.ezros.controllers.DirectoryController
import com.ezros.controllers.SqlController
import com.ezros.interfaces.Alerts
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Task
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.text.TextAlignment
import tornadofx.*
import java.io.File

class MainView : View("YAM SQL Deployment Tool"), Alerts {
    private val dir = DirectoryController(primaryStage)
    private val sql = SqlController()

    private val connections = sql.connections
    private val listConnections = connections.keys.toHashSet().toList().toObservable()
    private val selectedConnection = SimpleStringProperty()

    private var sourceDirectory: File? = null

//    private val menuBar: MenuBar by singleAssign()

    private var sourceTextField: TextField by singleAssign()
    private var versionTextField: TextField by singleAssign()
    private var descriptionTextField: TextField by singleAssign()

    private var connectionsChoiceBox: ChoiceBox<String> by singleAssign()

    private var deployingLabel: Label by singleAssign()
    private var versionNumberField: Field by singleAssign()

    private var connectBtn: Button by singleAssign()
    private var sourceBtn: Button by singleAssign()
    private var deployBtn: Button by singleAssign()

    private var listItems: ListView<String> by singleAssign()

    private var progressBar: ProgressBar by singleAssign()

    private var files = arrayOf<File>()
    private var targetPath = ""

    override val root = form {
        prefHeight = 890.0
        prefWidth = 565.0

        fieldset("", labelPosition = Orientation.VERTICAL) {
            field("Database:", Orientation.HORIZONTAL) {
                choicebox(selectedConnection, listConnections) {
                    connectionsChoiceBox = this
                    value = listConnections[0]
                    sql.selectedConn = connections[this.value]!!
                }

                button("Connect") {
                    connectBtn = this

                    setOnMouseClicked {
                        sql.selectedConn = connections[selectedConnection.value]!!

                        when {
                            sql.isConnected() -> {
                                sql.close()
                                text = "Connect"
                                connectionsChoiceBox.isDisable = false
                                info("Database Disconnected!")
                            }
                            sql.connect() -> {
                                text = "Disconnect"
                                versionNumberField.text = "Version Number (current version ${sql.getCurrentVersion()}) :"
                                connectionsChoiceBox.isDisable = true
                                info("Database Connected!")
                            }
                        }
                    }
                }
            }

            field("Source Folder:", Orientation.HORIZONTAL) {
                style {
                    paddingTop = 15
                }

                textfield {
                    sourceTextField = this
                    isEditable = false
                }

                button("Choose Folder") {
                    sourceBtn = this

                    setOnMouseClicked {
                        val source = dir.setSourceDirectory()

                        if (source != null) {
                            sourceDirectory = source
                            sourceTextField.text = sourceDirectory!!.path
                            setListView()
                        }
                    }
                }
            }

            field("Version Number:", Orientation.HORIZONTAL) {
                style {
                    paddingTop = 15
                }

                versionNumberField = this
                textfield { versionTextField = this }
            }

            field("Description (optional):", Orientation.HORIZONTAL) {
                style {
                    paddingTop = 15
                }

                textfield { descriptionTextField = this }
            }

            separator(Orientation.HORIZONTAL) {
                style {
                    paddingTop = 30
                    paddingBottom = 20
                }
            }

            field("Files to deploy:", Orientation.HORIZONTAL) {
                listview<String> { listItems = this }
            }

            field(null, Orientation.HORIZONTAL) {
                useMaxWidth = true

                button("Deploy") {
                    alignment = Pos.CENTER

                    deployBtn = this

                    setOnMouseClicked { deploy() }
                }
            }

            label("") {
                useMaxWidth = true

                style {
                    paddingTop = 30
                    textAlignment = TextAlignment.CENTER
                }

                deployingLabel = this
            }

            progressbar {
                useMaxWidth = true

                style {
                    paddingTop = 30
                }
                progressBar = this
            }
        }
    }

    init {
        val defaults = dir.getDefaults()

        targetPath = defaults.getProperty("yam.defaults.target")

        sourceTextField.text = when (defaults.getProperty("yam.defaults.source")) {
            null -> ""
            else -> {
                sourceDirectory = File(defaults.getProperty("yam.defaults.source"))
                setListView()
                defaults.getProperty("yam.defaults.source")
            }
        }
    }

    private fun setListView() {
        listItems.items.clear()
        files = dir.read(sourceDirectory!!.path).toHashSet().toTypedArray()
        files.forEach { listItems.items.add(it.name) }
    }

    fun disableAll(isDisable: Boolean) {
        sourceBtn.isDisable = isDisable
        connectBtn.isDisable = isDisable
        deployBtn.isDisable = isDisable
    }

    private fun deploy() {
        when {
            !sql.isConnected() -> {
                error(null, "Selected Database cannot connect")
            }
            sourceTextField.text.isNullOrBlank() -> {
                warning("No source folder selected!")
            }
            versionTextField.text.isNullOrBlank() -> {
                error("Missing version number", "")
            }
            files.isEmpty() -> {
                warning("No files available to run")
            }
            sql.getHistory().isNullOrEmpty() -> {
                print(sql.getHistory())
                error(
                    "Baseline is needed",
                    "Please ensure that new DBs are baselines. You can baseline the database by: File -> new baseline"
                )
            }
            else -> {

                val t = object : Task<Unit>() {
                    override fun call() {
                        disableAll(true)

                        updateMessage("Deploying....")
                        updateProgress(0.2, 1.0)

                        updateMessage("Running scripts....")
                        Thread.sleep(1000)

                        val result = sql.runScript(versionTextField.text, descriptionTextField.text, *files)
                        val excludeFiles = result.values.toHashSet().toList()

                        updateProgress(0.4, 1.0)
                        Thread.sleep(500)

                        if (targetPath.isNotBlank()) {
                            updateMessage("Copying files to ${File(targetPath).name}....")
                            Thread.sleep(500)
                            updateProgress(0.6, 1.0)

                            dir.copyFiles(sourceDirectory!!, targetPath, excludeFiles)
                        }

                        updateMessage("Renaming ${sourceDirectory?.name} to ${versionTextField.text}....")
                        Thread.sleep(500)
                        updateProgress(0.8, 1.0)

                        dir.renameDirectory(sourceTextField.text, versionTextField.text)

                        updateMessage("${versionTextField.text} deployed!")
                        Thread.sleep(500)

                        updateProgress(0.9, 1.0)
                    }

                    override fun succeeded() {
                        updateProgress(1.0, 1.0)
                        disableAll(false)
                        versionNumberField.text = "Version Number (current version ${sql.getCurrentVersion()}) :"
                        info("Successfully deployed!")
                    }
                }

                progressBar.progressProperty().unbind()
                progressBar.bind(t.progressProperty())

                deployingLabel.textProperty().bind(t.messageProperty())
                Thread(t).start()
            }
        }
    }
}
