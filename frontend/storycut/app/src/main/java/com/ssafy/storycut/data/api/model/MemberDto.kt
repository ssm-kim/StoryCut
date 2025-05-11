package com.ssafy.storycut.data.api.model

import java.util.Date

data class MemberDto (

    val memberId: String,
    val roomId: Long,
    val joinedAt: Date
    )