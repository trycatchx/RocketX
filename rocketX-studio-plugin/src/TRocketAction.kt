import icons.PluginIcons.ICON_ACTION_ENABLE
import icons.PluginIcons.ICON_ACTION_UNABLE
import java.io.File

/**
 * @description: 表示开启RocketX 组件
 * @author: louis
 * @date: 2020/11/5 3:58 PM
 */
class TRocketAction : BaseAction(ICON_ACTION_UNABLE) {

    override fun doAction() {
        val parentDir = File("$mParentPath/.gradle/")
        if(!parentDir.exists()) {
            parentDir.mkdirs()
        }
        val targetFile =  File(parentDir,"rocketXEnable")
        if(targetFile.exists()) {
            // 开启状态
            changeIcon(false)
            targetFile.delete()
        } else {
            //关闭状态
            changeIcon(true)
            targetFile.createNewFile()
        }
    }

    private fun changeIcon(enable:Boolean) {
        if(enable) {
            mEvent.presentation.icon = ICON_ACTION_UNABLE
        } else {
            mEvent.presentation.icon = ICON_ACTION_ENABLE
        }
    }

}