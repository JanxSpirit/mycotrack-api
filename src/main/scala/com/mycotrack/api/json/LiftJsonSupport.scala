package com.mycotrack.api.json

import cc.spray._
import typeconversion._
import http._
import MediaTypes._

import net.liftweb.json.Serialization._
import akka.event.EventHandler
import org.bson.types.ObjectId
import net.liftweb.json.JsonAST.{JString, JValue}
import net.liftweb.json._

/**
 * @author chris_carrier
 * @version 10/18/11
 */


trait LiftJsonSupport {
   implicit val formats: Formats

   implicit def liftJsonUnmarshaller[A <: Product :Manifest] = new UnmarshallerBase[A] {
           val canUnmarshalFrom = ContentTypeRange(`application/json`) :: Nil
           def unmarshal(content: HttpContent) = protect {
             try {
                   val jsonSource = DefaultUnmarshallers.StringUnmarshaller(content).right.get
                   parse(jsonSource).extract[A]
             } catch {
               case e => {
                 EventHandler.error(e, this, "Problem with lift-json")
                 throw e
               }
             }
           }
   }

   implicit def liftJsonMarshaller[A <: AnyRef] = new MarshallerBase[A] {
           val canMarshalTo = ContentType(`application/json`) :: Nil
           def marshal(value: A, contentType: ContentType) = {
                   val jsonSource = write(value)
                   DefaultMarshallers.StringMarshaller.marshal(jsonSource, contentType)
           }
   }
}

class ObjectIdSerializer extends Serializer[ObjectId] {
  private val ObjectIdClass = classOf[ObjectId]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), ObjectId] = {
    case (TypeInfo(ObjectIdClass, _), json) => json match {
      case JString(s) if (ObjectId.isValid(s)) =>
        new ObjectId(s)
      case x => throw new MappingException("Can't convert " + x + " to ObjectId")
    }
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case x: ObjectId => JString(x.toString)
  }
}