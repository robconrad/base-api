/*
 * Copyright (c) 2015 Robert Conrad - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * This file is proprietary and confidential.
 * Last modified by rconrad, 1/4/15 4:13 PM
 */

package base.socket.api

import base.common.service.{ ApiService, ServiceCompanion }

/**
 * Responsible for configuring, starting, binding, etc. the Socket API
 * @author rconrad
 */
trait SocketApiService extends ApiService {

  final def serviceManifest = manifest[SocketApiService]

  val name = "socket-api"

  /**
   * Socket Server Connection Information
   */
  def host: String
  def port: Int

}

object SocketApiService extends ServiceCompanion[SocketApiService]
