package io.unthrottled.amii.rider.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import io.unthrottled.amii.core.personality.emotions.EMOTIONAL_MUTATION_TOPIC
import io.unthrottled.amii.core.personality.emotions.EmotionalMutationAction
import io.unthrottled.amii.core.personality.emotions.EmotionalMutationType
import io.unthrottled.amii.core.personality.emotions.MoodCategory

class RelaxAction : AnAction(), DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    e.project?.messageBus
      ?.syncPublisher(EMOTIONAL_MUTATION_TOPIC)
      ?.onAction(
        EmotionalMutationAction(
          EmotionalMutationType.RESET,
          MoodCategory.NEGATIVE,
          e.project
        )
      )
  }
}
