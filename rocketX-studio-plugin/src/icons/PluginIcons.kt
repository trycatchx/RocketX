package icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * Created by louis on 21/11/4.
 */
object PluginIcons {

    private val RocketXIcon = load("/icons/icon.png")
    private val RocketXRun = load("/icons/run.png")

    /* Run action icon */
    val ICON_ACTION_RUN: Icon = RocketXIcon

    /* Stop action icon */
    val ICON_ACTION_STOP: Icon = RocketXRun

    private fun load(path:String): Icon {
        return IconLoader.getIcon(path, PluginIcons::class.java)
    }

}