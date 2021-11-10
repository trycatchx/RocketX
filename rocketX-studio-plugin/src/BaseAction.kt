import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import javax.swing.Icon

abstract class BaseAction : AnAction {

    lateinit var mProject: Project
    lateinit var mParentPath : String
    lateinit var mEvent: AnActionEvent

    constructor(icon: Icon) : super(icon)

    override fun actionPerformed(event: AnActionEvent) {
        this.mProject = event.getData(PlatformDataKeys.PROJECT)!!
        this.mParentPath = mProject?.baseDir?.path+"/.gradle/"
        this.mEvent = event
        doAction()
    }

    abstract fun doAction()
}