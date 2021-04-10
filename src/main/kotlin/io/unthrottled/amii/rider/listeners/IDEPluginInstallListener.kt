package io.unthrottled.amii.rider.listeners

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.rider.PluginMaster
import io.unthrottled.amii.rider.config.Constants.PLUGIN_ID
import io.unthrottled.amii.rider.tools.Logging

class IDEPluginInstallListener : DynamicPluginListener, Logging {

  override fun beforePluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
  }

  override fun beforePluginUnload(
    pluginDescriptor: IdeaPluginDescriptor,
    isUpdate: Boolean
  ) {
  }

  override fun checkUnloadPlugin(pluginDescriptor: IdeaPluginDescriptor) {
  }

  override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
    if (pluginDescriptor.pluginId.idString == PLUGIN_ID) {
      ApplicationManager.getApplication().invokeLater {
        PluginMaster.instance.onUpdate()
      }
    }
  }

  override fun pluginUnloaded(
    pluginDescriptor: IdeaPluginDescriptor,
    isUpdate: Boolean
  ) {
  }
}
