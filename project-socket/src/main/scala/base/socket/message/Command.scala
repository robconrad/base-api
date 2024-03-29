/*
 * Copyright (c) 2015 Robert Conrad - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * This file is proprietary and confidential.
 * Last modified by rconrad, 1/3/15 7:18 PM
 */

package base.socket.message

import base.common.lib.Actors
import base.socket._
import base.socket.json.JsonFormats
import base.socket.message.Command.Cmd
import io.netty.channel.ChannelHandlerContext
import org.json4s.JsonAST.JString
import org.json4s.JsonDSL._
import org.json4s.{ CustomSerializer, JValue, MappingException }

import scala.reflect.runtime.universe._

// scalastyle:off line.size.limit
sealed abstract class Command[T <: Message](implicit val man: Manifest[T]) {
  val cmd: String
  protected implicit val formats = JsonFormats.defaultWithCommands

  final override def toString = cmd
  final def toJValue: JValue = cmd

  final protected def extract(json: JValue) = json.extract[T]
}

sealed abstract class ProcessableCommand[T <: Message](implicit override val man: Manifest[T]) extends Command[T] {
  protected def process(implicit ctx: ChannelHandlerContext, msg: T)
  final def process(ctx: ChannelHandlerContext, json: JValue) { process(ctx, extract(json)) }
}

sealed abstract class UnprocessableCommand[T <: Message](implicit override val man: Manifest[T]) extends Command[T]

sealed abstract class ClientCommand[T <: ClientMessage](implicit override val man: Manifest[T])
  extends ProcessableCommand[T]

// communications client->server
sealed abstract class ControlCommand[T <: ControlMessage](implicit override val man: Manifest[T])
  extends ProcessableCommand[T]

// communications server->server
sealed abstract class ServerCommand[T <: ServerMessage](implicit override val man: Manifest[T])
  extends UnprocessableCommand[T]

// communications server->client
abstract class UserClientCommand[T <: UserClientMessage](override val cmd: String)(implicit override val man: Manifest[T])
  extends ClientCommand[T]

abstract class UserServerCommand[T <: UserServerMessage](override val cmd: String)(implicit override val man: Manifest[T])
  extends ServerCommand[T]

abstract class TestControlCommand[T <: TestControlMessage](override val cmd: String)(implicit override val man: Manifest[T])
  extends ControlCommand[T]

abstract class CommandObject {

  implicit val dispatcher = Actors.actorSystem.dispatcher

  implicit def command2JValue(cmd: Cmd) = cmd.toJValue
  implicit def ctx2ch(ctx: ChannelHandlerContext) = ctx.channel
  implicit def ctx2UserId(ctx: ChannelHandlerContext) = ctx.channel.userId

  val cmds: Set[Cmd]

  protected def init[T: TypeTag](self: T, in: Cmd*) = {
    val cmds = in.toSet

    val members = typeOf[T].members.filter(_.typeSignature match {
      case tpe if tpe <:< typeOf[Cmd] => true
      case _                          => false
    })

    lazy val name = this.getClass.getSimpleName
    lazy val err = s"$name command members do not match initialized commands\n\nmembers: $members\n\ncommands: $cmds"
    assert(members.size == cmds.size, err)

    cmds
  }
}

object Command {
  type Cmd = Command[_ <: Message]
  type ProcessableCmd = ProcessableCommand[_ <: Message]

  private var processable = Set[ProcessableCmd]()

  def map[T <: ProcessableCmd](cmds: T*) = {
    processable ++= cmds
    assertNoDuplicates(processable)
    cmds.map(c => c.cmd -> c).toMap
  }

  private def assertNoDuplicates(cmds: Set[ProcessableCmd]) {
    cmds.foreach { cmd =>
      assert(cmds.count(_.cmd == cmd.cmd) == 1, s"Commands strings must be unique, duplicate found: $cmd")
    }
    cmds.foreach { cmd =>
      assert(cmds.count(_.man == cmd.man) == 1, s"Commands must have unique messages, duplicate found: $cmd")
    }
  }

}

class CommandSerializer extends CustomSerializer[Command[_ <: Message]](format => (
  { case JString(cmd) => throw new MappingException(s"Unknown command $cmd") },
  { case cmd: Cmd => JString(cmd.toString) }))
