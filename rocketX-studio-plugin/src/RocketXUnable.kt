import java.io.File

/**
 * @description: 表示关闭RocketX 组件
 * @author: louis
 * @date: 2020/11/8 2:15 PM
 */
class RocketXUnable : BaseAction(icons.PluginIcons.ICON_ACTION_STOP){

    override fun doAction() {
        val parentDir = File(mParentPath)
        if(!parentDir.exists()) {
            parentDir.mkdirs()
        }
        val targetFile =  File(parentDir,"rocketXEnable")
        targetFile.delete()
    }
}