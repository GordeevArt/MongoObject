MongoObject
===========

Trait for ReactiveMongo Play plugin (https://github.com/ReactiveMongo/Play-ReactiveMongo)


Usage
=====

It's not in sbt or maven repository yet.

Main mongoDB object 
```scala
package system

import play.api.Play.current
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import models._

object Mongo {

  // initialize database
  def db: reactivemongo.api.DB = ReactiveMongoPlugin.db

  // collections
  val persons: JSONCollection = db.collection[JSONCollection]("Persons")
  val documents: JSONCollection = db.collection[JSONCollection]("Documents")

  // formats
  import main.IdFormat._

  object JsonFormats {

    implicit val personFormat = Json.format[Person]
    implicit val documentFormat = Json.format[Document]
  }
}
```
 Models
```scala
package models

import main.MongoObject
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.bson.BSONObjectID
import system.Mongo

// Simple case classes
case class Person(_id: Option[BSONObjectID] = None, name: String, age: Int)

case class Document(series: Option[String], number: String, issued: Option[String], 
                    documentType: Option[String], ownerId: Option[BSONObjectID])

// Objects with queries
object Person extends MongoObject[Person] {

  import system.Mongo.JsonFormats._

  val collection = Mongo.persons

  def findAdults = 
    fetch[List](Json.obj("age" -> Json.obj("$gte" -> 21)), sortQuery = Some(Json.obj("age" -> 1)))
}


object Document extends MongoObject[Document] {

  import system.Mongo.JsonFormats._

  val collection = Mongo.documents

  def findPassportNumbers = 
    fetch[List](Json.obj("documentType" -> "passport"), Some(Json.obj("_id" -> 0, "number" -> 1)))
      .map(_.map(_.number))
}
```
