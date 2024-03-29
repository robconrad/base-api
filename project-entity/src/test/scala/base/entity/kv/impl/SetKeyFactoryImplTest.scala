/*
 * Copyright (c) 2015 Robert Conrad - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * This file is proprietary and confidential.
 * Last modified by rconrad, 1/3/15 2:27 PM
 */

package base.entity.kv.impl

/**
 * {{ Describe the high level purpose of SetKeyFactoryImplTest here. }}
 * {{ Include relevant details here. }}
 * {{ Do not skip writing good doc! }}
 * @author rconrad
 */
class SetKeyFactoryImplTest extends KeyFactoryImplTest {

  private val id = "id"
  private val id2 = "id2"
  private val id3 = "id3"

  private val val1 = "value1"
  private val val2 = "value2"

  val factory = new SetKeyFactoryImpl(KeyChannel(getClass.getSimpleName))
  private val model1 = factory.make(id)
  private val model2 = factory.make(id2)
  private val dest = factory.make(id3)
  private val models = List(model1, model2)

  def create() = {
    val keys = List(model1, model2)
    keys.foreach(_.add(val1))
    keys
  }

  test("remove multi") {
    assert(factory.remove(models, val1).await() == Map(model1 -> false, model2 -> false))

    assert(model1.add(val1).await())
    assert(factory.remove(models, val1).await() == Map(model1 -> true, model2 -> false))
    assert(!model1.isMember(val1).await())

    assert(model1.add(val1).await())
    assert(model2.add(val1).await())
    assert(model2.add(val2).await())
    assert(factory.remove(models, val1).await() == Map(model1 -> true, model2 -> true))
    assert(!model1.isMember(val1).await())
    assert(!model2.isMember(val1).await())
    assert(model2.isMember(val2).await())
  }

  test("count") {
    assert(factory.count(models).await() == Map(model1 -> 0, model2 -> 0))

    assert(model1.add(val1).await())
    assert(model2.add(val1).await())
    assert(model2.add(val2).await())
    assert(factory.count(models).await() == Map(model1 -> 1, model2 -> 2))
  }

  test("unionStore") {
    assert(model1.add(val1).await())
    assert(model1.add(val2).await())
    assert(model2.add(val2).await())
    assert(factory.unionStore(dest, model1, model2).await() == 2)
    assert(dest.isMember(val1).await())
    assert(dest.isMember(val2).await())
  }

}
