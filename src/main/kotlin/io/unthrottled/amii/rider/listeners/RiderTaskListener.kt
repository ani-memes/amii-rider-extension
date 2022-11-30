package io.unthrottled.amii.rider.listeners

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.jetbrains.rd.platform.util.idea.ProtocolSubscribedProjectComponent
import com.jetbrains.rider.build.BuildHost
import com.jetbrains.rider.model.BuildResultKind
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEventListener
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.PluginMessageBundle

internal enum class TaskStatus {
  PASS, FAIL, UNKNOWN
}

class RiderTaskListener(
  project: Project
) : ProtocolSubscribedProjectComponent(project), UserEventListener, Disposable {

  private val messageBusConnection = project.messageBus.connect()

  private var previousTaskStatus = TaskStatus.UNKNOWN
  private val errors = setOf(
    BuildResultKind.HasErrors,
    BuildResultKind.Crashed,
  )

  init {
    ApplicationManager.getApplication().invokeLater {
      messageBusConnection.subscribe(EVENT_TOPIC, this)
    }
    BuildHost.getInstance(project).buildSession.advise(projectComponentLifetime) { buildSession ->
      buildSession?.result?.advise(projectComponentLifetime) { buildResult ->
        val hasErrors = errors.contains(buildResult.kind)
        when {
          hasErrors -> {
            project.messageBus
              .syncPublisher(EVENT_TOPIC)
              .onDispatch(
                UserEvent(
                  UserEvents.TASK,
                  UserEventCategory.NEGATIVE,
                  PluginMessageBundle.message("user.event.task.failure.name"),
                  project
                ),
              )
          }
          previousTaskStatus == TaskStatus.FAIL &&
            BuildResultKind.Canceled != buildResult.kind -> {
            project.messageBus
              .syncPublisher(EVENT_TOPIC)
              .onDispatch(
                UserEvent(
                  UserEvents.TASK,
                  UserEventCategory.POSITIVE,
                  PluginMessageBundle.message("user.event.task.success.name"),
                  project
                ),
              )
          }
        }
      }
    }
  }

  override fun onDispatch(userEvent: UserEvent) {
    previousTaskStatus = when (userEvent.type) {
      UserEvents.TASK -> {
        when (userEvent.category) {
          UserEventCategory.NEGATIVE -> TaskStatus.FAIL
          UserEventCategory.POSITIVE -> TaskStatus.PASS
          else -> TaskStatus.UNKNOWN
        }
      }
      else -> TaskStatus.UNKNOWN
    }
  }

  override fun dispose() {
    super.dispose()
    messageBusConnection.dispose()
  }
}
