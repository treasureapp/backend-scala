import breeze.numerics.pow
import scala.util.Random
import breeze.linalg._

/**
  * create random vector to mock up a price data
  *
  */
val n = 100
val rand = Random

val timeSeries: Vector[Double] = {
  var X = 0.0
  var prev = 0.0

  val out : Vector[Double] = Vector.fill(100) {
    prev = prev + X
    X = rand.nextDouble() - 0.5
    prev
  }
  out
}


/**
  * create weighting vector for exponential moving average
  * https://quant.stackexchange.com/questions/33548/is-there-a-non-recursive-way-of-calculating-the-exponential-moving-average
  * beta is a parameter that can be changed
  */
val beta = 0.5

val w = ((1 to n).map {
  (i: Int) =>
    pow(beta,n-i)*(1-beta)/(1-pow(beta, n))
}).reverse.toArray

val weightVector = DenseVector(w)


/**
  * use fancy breeze library to calculate exponential moving average
  */

val ema = weightVector.dot(timeSeries)
