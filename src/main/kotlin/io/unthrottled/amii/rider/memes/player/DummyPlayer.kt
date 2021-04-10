package io.unthrottled.amii.rider.memes.player

class DummyPlayer : MemePlayer {
  override val duration: Long
    get() = MemePlayer.NO_LENGTH

  override fun play() {}

  override fun stop() {}
}
