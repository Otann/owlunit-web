package com.owlunit.web.lib

import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord}
import java.util.HashMap
import com.mongodb.{BasicDBObject, DBObject}
import net.liftweb.json.JsonAST._
import net.liftweb.common.{Empty, Full, Box}
import net.liftweb.record.FieldHelpers
import net.liftweb.record.field.StringField
import net.liftweb.mongodb.record.field.{ObjectIdField, MongoMapField}


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class BsonRecordMapField[OwnerType <: BsonRecord[OwnerType], SubRecordType <: BsonRecord[SubRecordType]] (rec: OwnerType, valueMeta: BsonMetaRecord[SubRecordType])(implicit mf: Manifest[SubRecordType])
  extends MongoMapField[OwnerType, SubRecordType](rec: OwnerType) {

  import scala.collection.JavaConversions._

  override def asDBObject: DBObject = {
    val javaMap = new HashMap[String, DBObject]()
    for ((key, element) <- value)  {
      javaMap.put(key.asInstanceOf[String], element.asDBObject)
    }
    val dbl = new BasicDBObject(javaMap)
    dbl
  }

  override def setFromDBObject(dbo: DBObject): Box[Map[String, SubRecordType]] = {
    val mapResult: Map[String, SubRecordType] = (for ((key, dboEl) <- dbo.toMap.toSeq) yield (key.asInstanceOf[String], valueMeta.fromDBObject(dboEl.asInstanceOf[DBObject]))).toMap
    setBox(Full(mapResult))
  }


  override def asJValue = {
    val fieldList = (for ((key, elem) <- value) yield JField(key, elem.asJValue)).toList
    JObject(fieldList)
  }

  override def setFromJValue(jvalue: JValue) = jvalue match {
    case JNothing | JNull if optional_? => setBox(Empty)
    case JObject(fieldList) => val retrievedMap = fieldList.map {
      field =>
        val key = field.name
      val valRetrieved = valueMeta.fromJValue(field.value) openOr valueMeta.createRecord
      (key, valRetrieved)
    }.toMap
    setBox(Full(retrievedMap))
    case other => setBox(FieldHelpers.expectedA("JObject", other))
  }
}

