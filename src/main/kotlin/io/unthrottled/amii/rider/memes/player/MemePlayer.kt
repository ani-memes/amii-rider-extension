package io.unthrottled.amii.rider.memes.player

interface MemePlayer {
  companion object {
    const val NO_LENGTH: Long = -1L
  }

  val duration: Long

  fun play()

  fun stop()
}
