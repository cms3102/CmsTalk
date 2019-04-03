package com.csergio.cmstalk.model


class ChatRoomModel {

    // 채팅방 멤버들
    val members = mutableMapOf<String, Boolean>()
    // 채팅방 대화 내용
    val comments = mutableMapOf<String, Comment>()

    class Comment{
        var uid = ""
        var message = ""
        var timestamp:Any? = null
    }

}