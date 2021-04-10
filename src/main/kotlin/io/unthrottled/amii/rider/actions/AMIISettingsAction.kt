package io.unthrottled.amii.rider.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware

class AMIISettingsAction : AnAction(), DumbAware {

  override fun actionPerformed(e: AnActionEvent) {
    ShowSettingsUtil.getInstance()
      .showSettingsDialog(e.project, io.unthrottled.amii.rider.config.ui.PluginSettingsUI::class.java)
  }
}
