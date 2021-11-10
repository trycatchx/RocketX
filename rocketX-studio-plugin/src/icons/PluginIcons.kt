package icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * Created by louis on 21/11/4.
 */
object PluginIcons {

    /* Run action icon */
    val ICON_ACTION_ENABLE: Icon = load("/res/unable.png")

    /* Stop action icon */
    val ICON_ACTION_UNABLE: Icon =  load("/res/enable.png")

    private fun load(path:String): Icon {
        return IconLoader.getIcon(path, PluginIcons::class.java)
    }

}