package main

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.Count

import scala.concurrent.Future

/**
 * @author artem
 * @since 25.11.14 14:41
 *
 */
trait MongoObject[T] {
  
  val collection: JSONCollection
  
  def fetch[L[_]](query: JsObject, projection: Option[JsObject] = None, sortQuery: Option[JsObject] = None)
                 (implicit reader : Reads[T], cbf : scala.collection.generic.CanBuildFrom[L[_], T, L[T]])  = {

    val findQuery = projection.map(collection.find(query, _)).getOrElse(collection.find(query))

    sortQuery.foldLeft(findQuery)(_ sort _).cursor[T](reader, defaultContext).collect[L]()(cbf, defaultContext)
  }

  def count(query: JsObject = Json.obj()): Future[Int] =
    collection.db.command(Count(collection.name, Some(BSONFormats.toBSON(query).get.asInstanceOf[BSONDocument])))

  def byIdQuery(id: String) = Json.obj("_id" -> Json.obj("$oid" -> id))

  def byId(id: String)(implicit reader : Reads[T]) = collection.find(byIdQuery(id)).one[T](reader, defaultContext)

}
