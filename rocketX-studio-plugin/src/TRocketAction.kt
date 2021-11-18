import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.Messages
import icons.PluginIcons
import java.io.File

/**
 * @description: 表示开启RocketX 组件
 * @author: louis
 * @date: 2020/11/5 3:58 PM
 */
open class TRocketAction : BaseAction(icons.PluginIcons.ICON_ACTION_ENABLE) {

    private var init = false


    //进入 project 需要更新 icon
    override fun update(e: AnActionEvent) {
        if (!init) {
            val project = e.getData(PlatformDataKeys.PROJECT)
            project?.let {
                init = true
                val parentDir = project?.baseDir?.path + "/.gradle/"
                val targetFile = File(parentDir, "rocketXEnable")
                if (targetFile.exists()) {
                    templatePresentation.icon = PluginIcons.ICON_ACTION_UNABLE
                } else {
                    templatePresentation.icon = PluginIcons.ICON_ACTION_ENABLE
                }
            }
        }
    }


    override fun doAction() {

        val parentDir = File(mParentPath)
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }
        val targetFile = File(parentDir, "rocketXEnable")
        if (targetFile.exists()) {
            // 开启状态
            changeIcon(false)
            targetFile.delete()
            targetFile.canWrite()
        } else {
            //关闭状态
            changeIcon(true)
            targetFile.createNewFile()
        }
    }

    fun changeIcon(enable:Boolean) {
        if(enable) {
            mEvent.presentation.icon = PluginIcons.ICON_ACTION_UNABLE
        } else {
            mEvent.presentation.icon = PluginIcons.ICON_ACTION_ENABLE
        }
    }


}