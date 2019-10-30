package com.sungbin.sungstarbook.dto

class MyInformationItem {
    var name: String? = null
    var msg: String? = null
    var uid: String? = null
    var rooms: ArrayList<String>? = null
    var friends: ArrayList<String>? = null

    constructor() {}
    constructor(
        name: String?,
        msg: String?,
        uid: String?,
        rooms: ArrayList<String>?,
        friends: ArrayList<String>?
    ) {
        this.name = name
        this.msg = msg
        this.uid = uid
        this.rooms = rooms
        this.friends = friends
    }

}