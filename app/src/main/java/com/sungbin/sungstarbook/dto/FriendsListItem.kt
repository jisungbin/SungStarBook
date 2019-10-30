package com.sungbin.sungstarbook.dto

class FriendsListItem {
    var name: String? = null
    var msg: String? = null
    var uid: String? = null

    constructor() {}
    constructor(name: String?, msg: String?, uid: String?) {
        this.name = name
        this.msg = msg
        this.uid = uid
    }
}