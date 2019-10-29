package com.sungbin.sungstarbook.dto

class ChattingItem {
    var name: String? = null
    var time: String? = null
    var msg: String? = null
    var type: String? = null
    var profilePicUri: String? = null
    var contentUri: String? = null
    var uid: String? = null

    constructor() {}

    constructor(
        name: String?,
        time: String?,
        msg: String?,
        type: String?,
        profilePicUri: String?,
        contentUri: String?,
        uid: String?
    ) {
        this.name = name
        this.time = time
        this.msg = msg
        this.type = type
        this.profilePicUri = profilePicUri
        this.contentUri = contentUri
        this.uid = uid
    }
}