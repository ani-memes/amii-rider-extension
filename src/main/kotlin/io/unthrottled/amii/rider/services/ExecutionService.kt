package io.unthrottled.amii.rider.services

import com.intellij.openapi.application.ApplicationManager

object ExecutionService {

  fun executeAsynchronously(toExecute: () -> Unit) {
    ApplicationManager.getApplication().executeOnPooledThread(toExecute)
  }
}
