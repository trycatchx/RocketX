import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import javax.swing.Icon
/**
 * @description: 基类
 * @author: louis
 * @date: 2020/11/5 3:50 PM
 */
abstract class BaseAction(val icon: Icon) : AnAction(icon){

    lateinit var mProject: Project
    lateinit var mParentPath : String
    lateinit var mEvent: AnActionEvent

    override fun actionPerformed(event: AnActionEvent) {
        this.mProject = event.getData(PlatformDataKeys.PROJECT)!!
        this.mParentPath = mProject.baseDir?.path+"/.gradle/"
        this.mEvent = event
        doAction()
    }

    abstract fun doAction()

}