//package com.example.bookapp.model
//
//import java.sql.Timestamp
//
//data class ModelCategory( var id: String,  var category:String,  var timestamp: Long,  var uid: String){
//
//}
package com.example.bookapp.model

class ModelCategory{
    var id: Long = 0
    var category: String = ""
    var timestamp: Long = 0
    var uid: String = ""

    constructor()
   constructor(id: Long, category: String, timestamp: Long, uid: String) {
      this.id = id
      this.category = category
      this.timestamp = timestamp
      this.uid = uid
   }

}
