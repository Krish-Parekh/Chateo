package com.krish.chateo.model

import java.util.*

data class Inbox(
    val message: String = "",
    var from: String = "",
    var to : String = "",
    var name: String = "",
    var image: String = "",
    val time: Date = Date(),
    var count: Int = 0
)
