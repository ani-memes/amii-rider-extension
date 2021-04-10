package io.unthrottled.amii.rider.core.personality.emotions

import io.unthrottled.amii.events.UserEvent

internal interface EmotionDerivationUnit {
  fun deriveEmotion(
    userEvent: UserEvent,
    emotionalState: EmotionalState,
  ): EmotionalState

  fun deriveFromMutation(
    emotionalMutationAction: EmotionalMutationAction,
    emotionalState: EmotionalState,
  ): EmotionalState
}
