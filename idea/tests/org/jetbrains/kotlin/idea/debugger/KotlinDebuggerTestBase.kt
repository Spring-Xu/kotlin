/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.debugger

import com.intellij.debugger.DebuggerInvocationUtil
import com.intellij.debugger.DebuggerManagerEx
import com.intellij.debugger.SourcePosition
import com.intellij.debugger.actions.MethodSmartStepTarget
import com.intellij.debugger.actions.SmartStepTarget
import com.intellij.debugger.engine.*
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.impl.DebuggerContextImpl
import com.intellij.debugger.impl.PositionUtil
import com.intellij.debugger.settings.DebuggerSettings
import com.intellij.debugger.ui.breakpoints.BreakpointManager
import com.intellij.debugger.ui.breakpoints.LineBreakpoint
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.roots.JdkOrderEntry
import com.intellij.openapi.roots.libraries.LibraryUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XDebuggerUtil
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import com.sun.jdi.request.StepRequest
import org.jetbrains.kotlin.idea.debugger.breakpoints.KotlinFieldBreakpoint
import org.jetbrains.kotlin.idea.debugger.breakpoints.KotlinFieldBreakpointType
import org.jetbrains.kotlin.idea.debugger.stepping.*
import org.jetbrains.kotlin.idea.test.JetJdkAndLibraryProjectDescriptor
import org.jetbrains.kotlin.idea.util.application.runReadAction
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.jetbrains.kotlin.test.InTextDirectivesUtils
import org.jetbrains.kotlin.test.InTextDirectivesUtils.findStringWithPrefixes
import java.io.File
import javax.swing.SwingUtilities

abstract class KotlinDebuggerTestBase : KotlinDebuggerTestCase() {
    private var oldSettings: DebuggerSettings? = null
    private var oldIsFilterForStdlibAlreadyAdded = false
    private var oldDisableKotlinInternalClasses = false
    private var oldRenderDelegatedProperties = false

    protected var evaluationContext: EvaluationContextImpl? = null
    protected var debuggerContext: DebuggerContextImpl? = null

    override fun initApplication() {
        super.initApplication()
        saveDefaultSettings()
    }

    override fun tearDown() {
        super.tearDown()
        restoreDefaultSettings()

        evaluationContext = null
        debuggerContext = null
    }

    protected fun configureSettings(fileText: String) {
        val kotlinSettings = KotlinDebuggerSettings.getInstance()
        kotlinSettings.DEBUG_IS_FILTER_FOR_STDLIB_ALREADY_ADDED = false
        kotlinSettings.DEBUG_DISABLE_KOTLIN_INTERNAL_CLASSES = fileText.getValueForSetting("DISABLE_KOTLIN_INTERNAL_CLASSES", oldDisableKotlinInternalClasses)
        kotlinSettings.DEBUG_RENDER_DELEGATED_PROPERTIES = fileText.getValueForSetting("RENDER_DELEGATED_PROPERTIES", oldRenderDelegatedProperties)

        val debuggerSettings = DebuggerSettings.getInstance()!!
        debuggerSettings.SKIP_SYNTHETIC_METHODS = fileText.getValueForSetting("SKIP_SYNTHETIC_METHODS", oldSettings!!.SKIP_SYNTHETIC_METHODS)
        debuggerSettings.SKIP_CONSTRUCTORS = fileText.getValueForSetting("SKIP_CONSTRUCTORS", oldSettings!!.SKIP_CONSTRUCTORS)
        debuggerSettings.SKIP_CLASSLOADERS = fileText.getValueForSetting("SKIP_CLASSLOADERS", oldSettings!!.SKIP_CLASSLOADERS)
        debuggerSettings.TRACING_FILTERS_ENABLED = fileText.getValueForSetting("TRACING_FILTERS_ENABLED", oldSettings!!.TRACING_FILTERS_ENABLED)
        debuggerSettings.SKIP_GETTERS = fileText.getValueForSetting("SKIP_GETTERS", oldSettings!!.SKIP_GETTERS)
    }

    private fun String.getValueForSetting(name: String, defaultValue: Boolean): Boolean {
        return findStringWithPrefixes(this, "// $name: ")?.toBoolean() ?: defaultValue
    }

    private fun saveDefaultSettings() {
        oldIsFilterForStdlibAlreadyAdded = KotlinDebuggerSettings.getInstance().DEBUG_IS_FILTER_FOR_STDLIB_ALREADY_ADDED
        oldDisableKotlinInternalClasses = KotlinDebuggerSettings.getInstance().DEBUG_DISABLE_KOTLIN_INTERNAL_CLASSES
        oldRenderDelegatedProperties = KotlinDebuggerSettings.getInstance().DEBUG_RENDER_DELEGATED_PROPERTIES
        oldSettings = DebuggerSettings.getInstance()!!.clone()
    }

    private fun restoreDefaultSettings() {
        KotlinDebuggerSettings.getInstance().DEBUG_IS_FILTER_FOR_STDLIB_ALREADY_ADDED = oldIsFilterForStdlibAlreadyAdded
        KotlinDebuggerSettings.getInstance().DEBUG_DISABLE_KOTLIN_INTERNAL_CLASSES = oldDisableKotlinInternalClasses
        KotlinDebuggerSettings.getInstance().DEBUG_RENDER_DELEGATED_PROPERTIES = oldRenderDelegatedProperties

        val debuggerSettings = DebuggerSettings.getInstance()!!
        debuggerSettings.SKIP_SYNTHETIC_METHODS = oldSettings!!.SKIP_SYNTHETIC_METHODS
        debuggerSettings.SKIP_CONSTRUCTORS = oldSettings!!.SKIP_CONSTRUCTORS
        debuggerSettings.SKIP_CLASSLOADERS = oldSettings!!.SKIP_CLASSLOADERS
        debuggerSettings.TRACING_FILTERS_ENABLED = oldSettings!!.TRACING_FILTERS_ENABLED
        debuggerSettings.SKIP_GETTERS = oldSettings!!.SKIP_GETTERS
    }

    protected val dp: DebugProcessImpl
        get() = getDebugProcess() ?: throw AssertionError("createLocalProcess() should be called before getDebugProcess()")

    public fun doOnBreakpoint(action: SuspendContextImpl.() -> Unit) {
        super.onBreakpoint(SuspendContextRunnable {
            initContexts(it)
            it.printContext()
            it.action()
        })
    }

    protected fun initContexts(suspendContext: SuspendContextImpl) {
        evaluationContext = createEvaluationContext(suspendContext)
        debuggerContext = createDebuggerContext(suspendContext)
    }

    protected fun SuspendContextImpl.doStepInto(ignoreFilters: Boolean, smartStepFilter: MethodFilter?) {
        dp.getManagerThread()!!.schedule(dp.createStepIntoCommand(this, ignoreFilters, smartStepFilter))
    }

    protected fun SuspendContextImpl.doStepOut() {
        val stepOutCommand = runReadAction { KotlinSteppingCommandProvider().getStepOutCommand(this, StepRequest.STEP_LINE) }
                              ?: dp.createStepOutCommand(this)
        dp.getManagerThread()!!.schedule(stepOutCommand)
    }

    protected fun SuspendContextImpl.doStepOver() {
        val stepOverCommand = runReadAction { KotlinSteppingCommandProvider().getStepOverCommand(this, false, StepRequest.STEP_LINE) }
                              ?: dp.createStepOverCommand(this, false)
        dp.getManagerThread()!!.schedule(stepOverCommand)
    }

    protected fun doStepping(path: String) {
        val file = File(path)
        file.readLines().forEach {
            val line = it.trim()
            processSteppingInstruction(line)
        }
    }

    protected fun processSteppingInstruction(line: String) {
        fun repeat(indexPrefix: String, f: SuspendContextImpl.() -> Unit) {
            for (i in 1..(InTextDirectivesUtils.getPrefixedInt(line, indexPrefix) ?: 1)) {
                doOnBreakpoint(f)
            }
        }

        when {
            line.startsWith("// STEP_INTO: ") -> repeat("// STEP_INTO: ") { stepInto(this) }
            line.startsWith("// STEP_OUT: ") -> repeat("// STEP_OUT: ") { doStepOut() }
            line.startsWith("// STEP_OVER: ") -> repeat("// STEP_OVER: ") { doStepOver() }
            line.startsWith("// SMART_STEP_INTO_BY_INDEX: ") -> doOnBreakpoint { doSmartStepInto(InTextDirectivesUtils.getPrefixedInt(line, "// SMART_STEP_INTO_BY_INDEX: ")!!) }
            line.startsWith("// SMART_STEP_INTO: ") -> repeat("// SMART_STEP_INTO: ") { doSmartStepInto() }
            line.startsWith("// RESUME: ") -> repeat("// RESUME: ") { resume(this) }
        }
    }

    protected fun SuspendContextImpl.doSmartStepInto(chooseFromList: Int = 0) {
        this.doSmartStepInto(chooseFromList, false)
    }

    private fun SuspendContextImpl.doSmartStepInto(chooseFromList: Int, ignoreFilters: Boolean) {
        val filters = createSmartStepIntoFilters()
        if (chooseFromList == 0) {
            filters.forEach {
                dp.getManagerThread()!!.schedule(dp.createStepIntoCommand(this, ignoreFilters, it))
            }
        }
        else {
            dp.getManagerThread()!!.schedule(dp.createStepIntoCommand(this, ignoreFilters, filters.get(chooseFromList)))
        }
    }

    private fun createSmartStepIntoFilters(): List<MethodFilter> {
        val breakpointManager = DebuggerManagerEx.getInstanceEx(getProject())?.getBreakpointManager()
        val breakpoint = breakpointManager?.getBreakpoints()?.first { it is LineBreakpoint }

        val line = (breakpoint as LineBreakpoint).getLineIndex()

        return runReadAction {
            val containingFile = breakpoint.getPsiFile()
            if (containingFile == null) throw AssertionError("Couldn't find file for breakpoint at the line $line")

            val position = MockSourcePosition(_file = containingFile, _line = line)

            val stepTargets = KotlinSmartStepIntoHandler().findSmartStepTargets(position)

            stepTargets.filterIsInstance<SmartStepTarget>().map {
                stepTarget ->
                when (stepTarget) {
                    is KotlinLambdaSmartStepTarget -> KotlinLambdaMethodFilter(stepTarget.getLambda(), stepTarget.getCallingExpressionLines()!!)
                    is KotlinMethodSmartStepTarget -> KotlinBasicStepMethodFilter(stepTarget.resolvedElement, stepTarget.getCallingExpressionLines()!!)
                    is MethodSmartStepTarget -> BasicStepMethodFilter(stepTarget.getMethod(), stepTarget.getCallingExpressionLines())
                    else -> null
                }
            }
        }.filterNotNull()
    }

    protected fun SuspendContextImpl.printContext() {
        runReadAction {
            if (this.getFrameProxy() == null) {
                return@runReadAction println("Context thread is null", ProcessOutputTypes.SYSTEM)
            }

            val sourcePosition = PositionUtil.getSourcePosition(this)
            println(renderSourcePosition(sourcePosition), ProcessOutputTypes.SYSTEM)
        }
    }

    protected fun renderSourcePosition(sourcePosition: SourcePosition?): String {
        if (sourcePosition == null) {
            return "null"
        }

        val virtualFile = sourcePosition.getFile().getVirtualFile()
        if (virtualFile == null) {
            return "VirtualFile for position is null"
        }

        val libraryEntry = LibraryUtil.findLibraryEntry(virtualFile, getProject())
        if (libraryEntry != null && (libraryEntry is JdkOrderEntry ||
                                     libraryEntry.getPresentableName() == JetJdkAndLibraryProjectDescriptor.LIBRARY_NAME)) {
            return FileUtil.getNameWithoutExtension(virtualFile.getName()) + ".!EXT!"
        }

        return virtualFile.getName() + ":" + (sourcePosition.getLine() + 1)
    }

    protected fun finish() {
        doOnBreakpoint {
            resume(this)
        }
    }

    override fun createBreakpoints(file: PsiFile?) {
        super.createBreakpoints(file)

        if (file == null) return

        val document = PsiDocumentManager.getInstance(myProject).getDocument(file) ?: return
        val breakpointManager = XDebuggerManager.getInstance(myProject).getBreakpointManager()
        val breakpointType = javaClass<KotlinFieldBreakpointType>() as Class<out XBreakpointType<XBreakpoint<XBreakpointProperties<*>>, XBreakpointProperties<*>>>
        val type = XDebuggerUtil.getInstance().findBreakpointType<XBreakpoint<XBreakpointProperties<*>>>(breakpointType) as KotlinFieldBreakpointType
        val virtualFile = file.getVirtualFile()

        val runnable = {
            var offset = -1;
            while (true) {
                val fileText = document.getText()
                offset = fileText.indexOf("FieldWatchpoint!", offset + 1)
                if (offset == -1) break

                val commentLine = document.getLineNumber(offset)

                val comment = fileText.substring(document.getLineStartOffset(commentLine), document.getLineEndOffset(commentLine))

                val lineIndex = commentLine + 1
                val fieldName = comment.substringAfter("//FieldWatchpoint! (").substringBefore(")")

                if (!type.canPutAt(virtualFile, lineIndex, myProject)) continue

                val xBreakpoint = runWriteAction {
                    breakpointManager.addLineBreakpoint(
                            type as XLineBreakpointType<XBreakpointProperties<*>>,
                            virtualFile.getUrl(),
                            lineIndex,
                            type.createBreakpointProperties(virtualFile, lineIndex)
                    )
                }

                val javaBreakpoint = BreakpointManager.getJavaBreakpoint(xBreakpoint)
                if (javaBreakpoint is KotlinFieldBreakpoint) {
                    javaBreakpoint.setFieldName(fieldName)
                    javaBreakpoint.setWatchAccess(fileText.getValueForSetting("WATCH_FIELD_ACCESS", true))
                    javaBreakpoint.setWatchModification(fileText.getValueForSetting("WATCH_FIELD_MODIFICATION", true))
                    javaBreakpoint.setWatchInitialization(fileText.getValueForSetting("WATCH_FIELD_INITIALISATION", false))
                    BreakpointManager.addBreakpoint(javaBreakpoint)
                    println("KotlinFieldBreakpoint created at ${file.getVirtualFile().getName()}:$lineIndex", ProcessOutputTypes.SYSTEM)
                }
            }
        }

        if (!SwingUtilities.isEventDispatchThread()) {
            DebuggerInvocationUtil.invokeAndWait(myProject, runnable, ModalityState.defaultModalityState())
        }
        else {
            runnable.invoke()
        }
    }

    protected fun createAdditionalBreakpoints(fileText: String) {
        val breakpoints = InTextDirectivesUtils.findLinesWithPrefixesRemoved(fileText, "// ADDITIONAL_BREAKPOINT: ")
        for (breakpoint in breakpoints) {
            val position = breakpoint.split(".kt:")
            assert(position.size() == 2) { "Couldn't parse position from test directive: directive = $breakpoint" }
            createBreakpoint(position[0], position[1])
        }
    }

    private fun createBreakpoint(fileName: String, lineMarker: String) {
        val project = getProject()!!
        val sourceFiles = runReadAction {
            FilenameIndex.getAllFilesByExt(project, "kt").filter {
                it.getName().contains(fileName) &&
                it.contentsToByteArray().toString("UTF-8").contains(lineMarker)
            }
        }

        assert(sourceFiles.size() == 1) { "One source file should be found: name = $fileName, sourceFiles = $sourceFiles" }

        val runnable = Runnable() {
            val psiSourceFile = PsiManager.getInstance(project).findFile(sourceFiles.first())!!

            val breakpointManager = DebuggerManagerEx.getInstanceEx(project)?.getBreakpointManager()!!
            val document = PsiDocumentManager.getInstance(project).getDocument(psiSourceFile)!!

            val index = psiSourceFile.getText()!!.indexOf(lineMarker)
            val lineNumber = document.getLineNumber(index) + 1

            val breakpoint = breakpointManager.addLineBreakpoint(document, lineNumber)
            if (breakpoint != null) {
                println("LineBreakpoint created at " + psiSourceFile.getName() + ":" + lineNumber, ProcessOutputTypes.SYSTEM);
            }
        }

        DebuggerInvocationUtil.invokeAndWait(project, runnable, ModalityState.defaultModalityState())
    }
}
