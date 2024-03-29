/*
 * Copyright (c) 2015 Robert Conrad - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * This file is proprietary and confidential.
 * Last modified by rconrad, 1/3/15 1:15 PM
 */

package base.entity.kv.impl

import java.nio.charset.Charset

import base.entity.kv.Key.Pipeline
import base.entity.kv.{ KeyLogger, SetKey }
import redis.reply.BulkReply

import scala.collection.JavaConversions._

/**
 * Base model for set keys
 */
// scalastyle:off null
private[kv] final class SetKeyImpl(val key: String, protected val logger: KeyLogger) extends KeyImpl with SetKey {

  def members()(implicit p: Pipeline) = {
    if (isDebugEnabled) log("SMEMBERS (start)")
    p.smembers(key).map { v =>
      val res = v.asStringSet(Charset.defaultCharset()).toSet
      if (isDebugEnabled) log("SMEMBERS (finish)", "props: " + res.toString)
      res
    }
  }

  def isMember(value: Any)(implicit p: Pipeline) = {
    if (isDebugEnabled) log("SISMEMBER (start)", s"value: $value")
    p.sismember(key, value).map { v =>
      val isMember = v.data() == 1L
      if (isDebugEnabled) log("SISMEMBER (finish)", s"value: $value res: $isMember")
      isMember
    }
  }

  // note will only work for 1 randmember with this impl
  def rand()(implicit p: Pipeline) = {
    if (isDebugEnabled) log("SRANDMEMBER (start)")
    p.srandmember_(key).map { v =>
      val res = v match {
        case null                  => None
        case v if v.data() == null => None
        case v                     => Some(v.asInstanceOf[BulkReply].asAsciiString())
      }
      if (isDebugEnabled) log("SRANDMEMBER (finish)", s"result: $res")
      res
    }
  }

  def pop()(implicit p: Pipeline) = {
    if (isDebugEnabled) log("SPOP (start)")
    p.spop(key).map { v =>
      val res = v.asAsciiString()
      if (isDebugEnabled) log("SPOP (finish)", s"result: $res")
      res match {
        case null => None
        case x    => Some(x)
      }
    }
  }

  def add(value: Any)(implicit p: Pipeline) = p.sadd_(key, value.asInstanceOf[AnyRef]).map { v =>
    val res = v.data().toInt > 0
    if (isDebugEnabled) log("SADD", s" value: $value, result: $res")
    res
  }

  def remove(value: Any)(implicit p: Pipeline) = p.srem_(key, value.asInstanceOf[AnyRef]).map { v =>
    val res = v.data().toInt > 0
    if (isDebugEnabled) log("SREM", s" value: $value, result: $res")
    res
  }

  def move(to: SetKey, member: Any)(implicit p: Pipeline) = {
    if (isDebugEnabled) log("SMOVE", s" to: ${to.key} value: $member")
    p.smove(key, to.key, member).map(_.data().toInt > 0)
  }

}

