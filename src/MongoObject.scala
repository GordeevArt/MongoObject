import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Format
import play.api.libs.json.JsError
import play.api.libs.json.JsObject
import play.api.libs.json.JsResult
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.commands.Count

import scala.concurrent.Future

/**
 * @author artem
 * @since 25.11.14 14:41
 *
 */
trait MongoObject[T] {
  
  val collection: JSONCollection
  
  def getList(query: JsObject, projection: Option[JsObject] = None, sortQuery: Option[JsObject] = None)
             (implicit reader : Reads[T]) = {

    val findQuery = projection.map(collection.find(query, _)).getOrElse(collection.find(query))

    sortQuery.foldLeft(findQuery)(_ sort _).cursor[T](reader, defaultContext).collect[List]()
  }

  def count(query: JsObject = Json.obj()): Future[Int] =
    collection.db.command(Count(collection.name, Some(BSONFormats.toBSON(query).get.asInstanceOf[BSONDocument])))

  def byIdQuery(id: String) = Json.obj("_id" -> Json.obj("$oid" -> id))

  def byId(id: String)(implicit reader : Reads[T]) = collection.find(byIdQuery(id)).one[T](reader, defaultContext)

  val objectIDRegExFormat = "^[0-9a-fA-F]{24}$".r
  def isObjectIDValid(input: String) = objectIDRegExFormat.findFirstIn(input).nonEmpty

  implicit object ObjectIdReads extends Format[BSONObjectID] {

    def reads(json: JsValue): JsResult[BSONObjectID] =
      json.asOpt[JsObject].flatMap(oid => (oid \ "$oid").asOpt[String]) match {

        case Some(id) if isObjectIDValid(id) => JsSuccess(BSONObjectID(id))
        case Some(value) => JsError(s"Invalid ObjectId $value")
        case None => JsError("Value is not an ObjectId")
      }

    def writes(oid: BSONObjectID): JsValue = Json.obj("$oid" -> JsString(oid.stringify))
  }

}
