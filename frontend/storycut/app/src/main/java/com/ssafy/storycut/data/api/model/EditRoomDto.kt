package com.ssafy.storycut.data.api.model

data  class EditRoomDto (
    val roomTitle: String,
    val roomContext: String,
    val roomPassword: String? = null
)