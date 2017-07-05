package test.treasure.data

import com.treasure.data.{DownloaderEndPoint, PriceDownloadRequest, Spark}
import org.scalatest.{FunSpec, Matchers}

/**
  * Created by gcrowell on 2017-06-29.
  */
class EndPointTest extends FunSpec with Matchers {
  describe("PriceDownloader") {
    it("") {
      val priceRequest = new PriceDownloadRequest("MSFT")
      DownloaderEndPoint.handleDataDownloadRequest(priceRequest)
      Thread.sleep(10000)
    }
  }
}

class InitTest extends FunSpec with Matchers {

  describe("DataRoot initialization") {
    it("creates it if it doesn't exist") {
      val s = Spark
    }
  }
}
