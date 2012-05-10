package com.owlunit.core.ii.mutable

import org.specs2.mutable.Specification
import org.specs2.specification.AfterExample
import com.owlunit.core.ii.NotFoundException

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class IiSpecs extends Specification {

  var dao: IiDao = null
  step { dao = IiDao.local("ii-core/target/db") }

  "New Ii" should {
    "have 0 id" in {
      val ii = dao.create
      ii.id mustEqual 0
    }
    "have None meta before loadMeta" in {
      val ii = dao.create
      ii.loadMeta.meta.get must beEmpty
    }
    "have None items before loadItems" in {
      val ii = dao.create
      ii.items must beNone
    }
    "have empty meta after loadMeta" in {
      val ii = dao.create
      ii.loadMeta.meta.get must beEmpty
    }
    "have empty items after loadItems" in {
      val ii = dao.create
      ii.loadItems.items.get must beEmpty
    }
    "have meta after setMeta" in {
      val ii = dao.create.setMeta("key", "value")
      ii.meta.get must havePair("key" -> "value")
    }
    "have item after addItem" in {
      val item = dao.create.save
      val ii = dao.create.setItem(item, 1.0)
      ii.items.get must havePair(item -> 1.0)
    }
  }

  "Saved Ii" should {
    "have not 0 id" in {
      val ii = dao.create.save
      ii.id mustNotEqual 0
    }
    "have persisted meta" in {
      val saved = dao.create.setMeta("key", "value").save
      val loaded = dao.load(saved.id).loadMeta
      loaded.meta.get must havePair("key" -> "value")
    }
    "have persisted item" in {
      val component = dao.create.save
      val saved = dao.create.setItem(component, 1.0).save
      val loaded = dao.load(saved.id).loadItems
      loaded.items.get must havePair(component -> 1.0)
    }
    "be loadable by id" in {
      val saved = dao.create.save
      val loaded = dao.load(saved.id)
      saved.id mustEqual loaded.id
    }
    "not be loadable by id after delete" in {
      val saved = dao.create.save
      saved.delete()
      dao.load(saved.id) must throwA[NotFoundException]
    }
    "be loadable by meta" in {
      val saved = dao.create.setMeta("key", "value").save
      val loaded = dao.load("key", "value")
      loaded must contain(saved)
    }
  }

  step { dao.shutdown() }
  implicit def *** {} // IDEA reporting error without this

}