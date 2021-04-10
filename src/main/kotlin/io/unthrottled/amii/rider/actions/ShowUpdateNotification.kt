package io.unthrottled.amii.rider.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import io.unthrottled.amii.onboarding.UpdateNotification
import io.unthrottled.amii.onboarding.UserOnBoarding.getVersion

class ShowUpdateNotification : AnAction(), DumbAware {

  override fun actionPerformed(e: AnActionEvent) {
    getVersion()
      .ifPresent {
        UpdateNotification.display(
          e.project!!,
          it
        )
      }
  }
}
