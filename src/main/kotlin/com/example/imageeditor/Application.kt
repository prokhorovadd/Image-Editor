package com.example.imageeditor

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("main.fxml"))
        val scene = Scene(fxmlLoader.load())
        stage.title = "Image Editor"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}