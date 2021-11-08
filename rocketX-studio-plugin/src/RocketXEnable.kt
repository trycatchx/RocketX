import icons.PluginIcons
import java.io.File

/**
 * @description: 表示开启RocketX 组件
 * @author: louis
 * @date: 2020/11/5 3:58 PM
 */
class RocketXEnable() : BaseAction(PluginIcons.ICON_ACTION_STOP) {


    override fun doAction() {
        val parentDir = File(mParentPath)
        if(!parentDir.exists()) {
            parentDir.mkdirs()
        }
        val targetFile =  File(parentDir,"rocketXEnable")
        targetFile.createNewFile()
    }

}