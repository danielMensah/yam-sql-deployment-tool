package com.ezros.controllers

import com.ezros.interfaces.Alerts
import javafx.scene.control.Alert
import tornadofx.alert
import java.io.File
import java.io.InputStream
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.HashMap
import java.util.Properties

class SqlController: Alerts {
    private val absolutePath = Paths.get("").toAbsolutePath().toString()
    private var conn: Connection? = null
    var connections: MutableMap<String, String> = mutableMapOf()
    var selectedConn: String = ""
    var user = "yam"

    init {
        setup()
    }

    fun setup() {
        val inputStream: InputStream = File("$absolutePath/yam.properties").inputStream()

        inputStream.bufferedReader().forEachLine { line ->
            if (line.contains("jdbc:") && line.contains("yam.url")) {
                val prop = line.split("yam.url.").joinToString("").split("=jdbc")
                val connectionName = prop[0]
                val url = "jdbc" + prop[1]

                user = url.split("user=")[1].split("&")[0]

                connections[connectionName] = url
            }
        }
    }

    fun connect(selectedConnection: String = selectedConn, ssl: Boolean? = true, sslmode: String? = "require"): Boolean {
        if (isConnected()) {
            close()
        }

        val properties = Properties()
        properties.setProperty("ssl", ssl.toString())
        properties.setProperty("sslmode", sslmode)

        try {
            conn = DriverManager.getConnection(selectedConnection, properties)
        } catch (e: SQLException) {
            error(
                "Database Connection",
                "Missing jdbc driver! Please add jdbc driver to host. Example: jdbc:postgresql://<host>:<port>..."
            )
        }

        return isConnected()
    }

    fun runScript(version: String, description: String = "", vararg file: File): Map<String, String> {
        val errors: MutableMap<String, String> = mutableMapOf()
        val ranFiles: MutableList<String> = mutableListOf()

        file.forEach {
            val query = getFileContent(it)

            if (query.isNotBlank()) {
                try {
                    conn?.prepareStatement(query)?.execute()
                    ranFiles.add(it.name)
                } catch (e: SQLException) {
                    errors[it.name] = e.message.toString()
                }
            }
        }

        updateSchemaHistory(ranFiles, errors, description, version)

        return errors
    }

    private fun updateSchemaHistory(filenames: List<String>, errors: Map<String, String>, description: String, version: String) {

        filenames.forEach {
            val query = "INSERT INTO yam_schema_history (version, description, script, success, installed_by) SELECT '$version', '$description', '$it', true, '$user'"
            try {
                conn?.prepareStatement(query)?.executeUpdate()
            } catch (e: SQLException) {
                print(e.message)
            }
        }

        errors.forEach{
            val query = "INSERT INTO yam_schema_history (version, description, script, success, installed_by) SELECT '$version', '', '$it', false, '$user'"
            try {
                conn?.prepareStatement(query)?.executeUpdate()
            } catch (e: SQLException) {
                print(e.message)
            }
        }

    }

    fun getCurrentVersion(): String? {
        return try {
            val result = conn?.prepareStatement("SELECT version FROM yam_schema_history ORDER BY version desc LIMIT 1")?.executeQuery()!!
            resultSetToArrayList(result)["version"]
        } catch (e: SQLException) {
            print(e.message)
            ""
        }

    }

    fun isConnected(): Boolean = conn != null && !conn!!.isClosed

    fun close() = conn?.close()

    private fun getFileContent(file: File): String {
        val inputStream: InputStream = file.inputStream()
        return inputStream.bufferedReader().use { it.readText() }
    }

    fun getHistory(): HashMap<String, String>? {
        if (!isConnected()) {
            connect()
        }

        val file = File("$absolutePath/src/main/resources/get_schema_history.sql")
        val query = getFileContent(file)

        return try {
            val res = conn?.prepareStatement(query)?.executeQuery()!!
            resultSetToArrayList(res)
        } catch (e: SQLException) {
            hashMapOf()
        }

    }

    fun baseline() {
        if (!isConnected()) {
            connect()
        }

        val query = getFileContent(File("$absolutePath/src/main/resources/baseline.sql"))

        try {
            conn?.prepareStatement(query)?.execute()
        } catch (e: SQLException) {
            e.message?.let {
                error("Error while baselining", it)
            }
        }
    }

    private fun resultSetToArrayList(rs: ResultSet): HashMap<String, String> {
        val md = rs.metaData
        val columns = md.columnCount

        val row: HashMap<String, String> = hashMapOf()

        while (rs.next()) {
            for (i in 1..columns) {
                row[md.getColumnName(i)] = rs.getObject(i).toString()
            }
        }

        return row
    }
}