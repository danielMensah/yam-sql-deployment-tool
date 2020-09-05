package com.ezros.interfaces

import javafx.scene.control.Alert
import kotlinx.coroutines.delay

interface Alerts {

    fun error(header: String? = null, message: String = "Selected Database cannot connect") {
        try {
            Class.forName("org.junit.Test")
        } catch (e: Exception) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error"
            alert.contentText = message

            if (header != null) alert.headerText = header

            alert.showAndWait()
        }
    }

    fun warning(message: String) {
        try {
            Class.forName("org.junit.Test")
        } catch (e: Exception) {
            val alert = Alert(Alert.AlertType.WARNING)
            alert.title = "Warning"
            alert.contentText = message
            alert.showAndWait()
        }
    }

    fun info(message: String) {
        try {
            Class.forName("org.junit.Test")
        } catch (e: Exception) {
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.headerText = message
            alert.showAndWait()
        }
    }

    suspend fun waiter(message: String) {
        try {
            Class.forName("org.junit.Test")
        } catch (e: Exception) {
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.headerText = message
            alert.show()

            delay(2000)
            alert.close()
        }
    }
}