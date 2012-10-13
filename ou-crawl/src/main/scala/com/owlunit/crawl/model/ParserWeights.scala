package com.owlunit.crawl.model

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait ParserWeights {

  val initialGenreWeight = 30.0

  val keywordMinWeight = 1.0
  val keywordMaxWeight = 20.0

  val actorWeight = 15.0
  val directorWeight = 25.0
  val producerWeight = 10.0

}
