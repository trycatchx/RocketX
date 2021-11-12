import icons.PluginIcons.ICON_ACTION_CLEAN
import java.io.File

/**
 * @description: 表示 清理RocketX 缓存
 * @author: louis
 * @date: 2020/11/10 3:50 PM
 */
class TCleanAction : BaseAction(ICON_ACTION_CLEAN) {

    override fun doAction() {
        val parentDir = File("${mParentPath}/.rocketxcache/")
        if(parentDir.exists()) { parentDir.deleteRecursively()
        }
    }

}