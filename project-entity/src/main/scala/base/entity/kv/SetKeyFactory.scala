/*
 * Copyright (c) 2015 Robert Conrad - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * This file is proprietary and confidential.
 * Last modified by rconrad, 1/3/15 1:53 PM
 */

package base.entity.kv

import base.entity.kv.Key._
import base.entity.kv.impl.SetKeyImpl

import scala.concurrent.Future

/**
 * {{ Describe the high level purpose of SetKeyFactory here. }}
 * {{ Include relevant details here. }}
 * {{ Do not skip writing good doc! }}
 * @author rconrad
 */
trait SetKeyFactory extends KeyFactory {

  def make(id: String): SetKey

  def remove(sets: List[SetKey], value: Any)(implicit p: Pipeline): Future[Map[SetKey, Boolean]]

  def count(sets: List[SetKey])(implicit p: Pipeline): Future[Map[SetKey, Int]]

  def unionStore(destination: SetKey, sets: SetKey*)(implicit p: Pipeline): Future[Int]

}
