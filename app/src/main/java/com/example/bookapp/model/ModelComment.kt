package com.example.bookapp.model

class ModelComment {
    var id: String = ""
    var bookId: String = ""
    var timestamp: String = ""
    var comment: String = ""
    var uid: String = ""

    constructor()
    constructor(id: String, bookId: String, timestamp: String, comment: String, uid: String){
        this.id = id
        this.bookId = bookId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }
}