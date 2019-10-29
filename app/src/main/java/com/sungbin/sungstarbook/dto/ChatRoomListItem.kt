package com.sungbin.sungstarbook.dto

class ChatRoomListItem {
    var name: String? = null
    var time: String? = null
    var msg: String? = null
    var roomPicUri: String? = null
    var roomUid: String? = null

    constructor() {}
    constructor(name: String?, time: String?, msg: String?, roomPicUri: String?, roomUid: String?) {
        this.name = name
        this.time = time
        this.msg = msg
        this.roomPicUri = roomPicUri
        this.roomUid = roomUid
    }
}