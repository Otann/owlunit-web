package com.owlunit.core.ii.mutable.impl

import collection.mutable.{Map => MutableMap}
import org.neo4j.graphdb._
import org.neo4j.kernel.{Uniqueness, Traversal}
import traversal.Evaluators
import com.owlunit.core.ii.mutable.Ii

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

private[impl] object RelType extends RelationshipType {
  def name() = "CONNECTED"
}



