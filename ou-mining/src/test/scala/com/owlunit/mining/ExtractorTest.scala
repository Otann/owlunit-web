package com.owlunit.mining

import org.specs2.mutable.Specification
import net.liftweb.common.Loggable
import com.owlunit.web.config.{IiDaoConfig, MongoConfig}

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
class ExtractorTest extends Specification with Loggable {

  step {
    IiDaoConfig.init()
    MongoConfig.init()
  }

  "Extractor" should {
    "extract" in {

//      TMDBApiClient.updateMovie(85)
//      TMDBApiClient.updateMovie(89)
//      TMDBApiClient.updateMovie(300)
//      TMDBApiClient.updateMovie(301)
//      TMDBApiClient.updateMovie(302)
//      TMDBApiClient.updateMovie(550)
//      TMDBApiClient.updateMovie(862)
//      TMDBApiClient.updateMovie(863)
//      TMDBApiClient.updateMovie(10193)
//      TMDBApiClient.updateMovie(24428)

      1 mustEqual 1
    }
  }

  step {
    IiDaoConfig.shutdown()
  }
}
