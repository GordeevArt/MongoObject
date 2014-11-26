package main

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

/**
 * @author artem
 * @since 26.11.14 10:55
 *
 */
object IdFormat {

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
