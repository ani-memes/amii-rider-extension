package io.unthrottled.amii.rider.core.personality

import io.unthrottled.amii.core.personality.emotions.Mood
import io.unthrottled.amii.events.UserEvent

fun interface PersonalityCore {
  fun processUserEvent(
    userEvent: UserEvent,
    mood: Mood
  )
}
