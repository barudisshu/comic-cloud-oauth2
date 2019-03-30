package io.comiccloud

import io.comiccloud.rest.Server

object Main {
  def main(args: Array[String]): Unit = {
    new Server(new ComicBoot(), "service")
  }
}