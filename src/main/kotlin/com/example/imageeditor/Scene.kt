package com.example.imageeditor

import com.example.imageeditor.Node
import javafx.scene.input.DataFormat
import com.google.gson.*
import java.io.Serializable

class Scene(val nodeState: DataFormat, val linkState: DataFormat, private var currentId: UInt) {
    val nodes: MutableList<Node<*>> = mutableListOf()
    val connections: MutableList<InputLinksState> = mutableListOf()

    fun add(node: Node<*>) { nodes.add(node) }

    fun remove(node: Node<*>) { nodes.remove(node) }

    fun findNodeById(id: UInt): Node<*>? {
        for(node in nodes) { if (node.id == id) { return node } }
        return null
    }

    fun getId(): UInt { return currentId.also { currentId += 1u } }
}

data class InputLinksState (
    val id: Int,
    val connections: MutableList<LinkKey<Int, Int>>
): Serializable

fun JsonArray.toByteArray(): ByteArray {
    val byteArray = ByteArray(size())
    for(i in 0 until size()) {
        byteArray[i] = (get(i).asInt and 0xFF).toByte()
    }
    return byteArray
}



