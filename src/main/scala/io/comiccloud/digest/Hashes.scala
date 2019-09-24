package io.comiccloud.digest

object Hashes {
  private val random = new scala.util.Random

  def randomSha256(): Sha256 = Sha256(randomHexString(64))
  def randomSha1(): Sha1     = Sha1(randomHexString(40))
  def randomMd5(): MD5       = MD5(randomHexString(32))

  def randomHexString(n: Int): String = {
    val alphabet = "0123456789abcdef"
    Stream.continually(random.nextInt(alphabet.length)).map(alphabet).take(n).mkString
  }
}
