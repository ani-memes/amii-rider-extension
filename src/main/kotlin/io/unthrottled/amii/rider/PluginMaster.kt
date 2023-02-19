package io.unthrottled.amii.rider

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import io.unthrottled.amii.rider.onboarding.UserOnBoarding
import io.unthrottled.amii.rider.platform.LifeCycleManager
import io.unthrottled.amii.rider.tools.Logging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class PluginMaster : ProjectManagerListener, Disposable, Logging {

  companion object {
    val instance: PluginMaster
      get() = ServiceManager.getService(PluginMaster::class.java)
  }

  private val projectListeners: ConcurrentMap<String, ProjectListeners> = ConcurrentHashMap()

  override fun projectOpened(project: Project) {
    registerListenersForProject(project)
  }

  private fun registerListenersForProject(project: Project) {
    UserOnBoarding.attemptToPerformNewUpdateActions(project)
    val projectId = project.locationHash
    if (projectListeners.containsKey(projectId).not()) {
      projectListeners[projectId] =
        ProjectListeners(project)
    }
  }

  override fun projectClosed(project: Project) {
    projectListeners[project.locationHash]?.dispose()
    projectListeners.remove(project.locationHash)
  }

  override fun dispose() {
    projectListeners.forEach { (_, listeners) -> listeners.dispose() }
    LifeCycleManager.dispose()
  }

  fun onUpdate() {
    ProjectManager.getInstance().openProjects
      .forEach { registerListenersForProject(it) }
  }
}

internal data class ProjectListeners(
  private val project: Project
) : Disposable {

  override fun dispose() {
  }
}
