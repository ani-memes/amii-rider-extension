package io.unthrottled.amii.rider.listeners

import com.intellij.openapi.project.Project
import com.jetbrains.rd.platform.util.idea.ProtocolSubscribedProjectComponent
import com.jetbrains.rider.model.RdUnitTestStatus
import com.jetbrains.rider.model.rdUnitTestHost
import com.jetbrains.rider.projectView.solution
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.PluginMessageBundle
import io.unthrottled.amii.tools.toOptional
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class RiderTestListener(
  project: Project
) : ProtocolSubscribedProjectComponent(project) {

  companion object {
    val nonCompletionEvents = setOf(
      "building",
      "none",
      "exploring tests"
    )
  }

  private val model = project.solution.rdUnitTestHost

  private val runningTests = ConcurrentHashMap<String, Boolean>()

  init {
    model.sessions.advise(projectComponentLifetime) {
      val sessionKey = it.key.toString()
      runningTests[sessionKey] = false
      val state = it.newValueOpt?.state
      state?.advise(projectComponentLifetime) { sessionState ->
        val sessionStateMessage = sessionState.message
        if ((
          "Running".equals(sessionStateMessage, ignoreCase = true) ||
            // when the test UI window is first open
            // running is not seen, but finishing is.
            "Finishing".equals(sessionStateMessage, ignoreCase = true)
          ) && runningTests[sessionKey] != true
        ) {
          runningTests[sessionKey] = true
        } else if (runningTests[sessionKey] == true &&
          // Lots of events get emitted before the tests run that
          // hang on to the previous results, causing a lot of noise
          // when running the tests.
          !nonCompletionEvents.contains(sessionStateMessage.lowercase(Locale.getDefault()))
        ) {
          if (sessionState.completedCount == sessionState.totalCount &&
            sessionState.completedCount > 0
          ) {
            when (sessionState.status) {
              RdUnitTestStatus.Success -> PluginMessageBundle.message("user.event.test.pass.name") to
                UserEventCategory.POSITIVE
              RdUnitTestStatus.Failed -> PluginMessageBundle.message("user.event.test.fail.name") to
                UserEventCategory.NEGATIVE
              else -> null
            }.toOptional()
              .ifPresent { messageToCategory ->
                runningTests.remove(sessionKey)
                val (type, category) = messageToCategory
                project.messageBus
                  .syncPublisher(EVENT_TOPIC)
                  .onDispatch(
                    UserEvent(
                      UserEvents.TEST,
                      category,
                      type,
                      project
                    )
                  )
              }
          }
        }
      }
    }
  }
}
