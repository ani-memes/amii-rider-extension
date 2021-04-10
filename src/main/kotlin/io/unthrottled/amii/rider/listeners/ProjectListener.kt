package io.unthrottled.amii.rider.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import io.unthrottled.amii.tools.Logging

internal class ProjectListener :
  ProjectManagerListener, Logging {

  override fun projectOpened(project: Project) {
    io.unthrottled.amii.rider.PluginMaster.instance.projectOpened(project)
  }

  override fun projectClosed(project: Project) {
    io.unthrottled.amii.rider.PluginMaster.instance.projectClosed(project)
  }
}
