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

package org.jetbrains.kotlin.idea.quickfix.replaceWith

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.idea.core.targetDescriptors
import org.jetbrains.kotlin.idea.quickfix.CleanupFix
import org.jetbrains.kotlin.idea.quickfix.JetSingleIntentionActionFactory
import org.jetbrains.kotlin.idea.quickfix.moveCaret
import org.jetbrains.kotlin.psi.JetImportDirective
import org.jetbrains.kotlin.psi.JetSimpleNameExpression
import org.jetbrains.kotlin.resolve.calls.callUtil.getCalleeExpressionIfAny

public class DeprecatedSymbolUsageFix(
        element: JetSimpleNameExpression/*TODO?*/,
        replaceWith: ReplaceWith
) : DeprecatedSymbolUsageFixBase(element, replaceWith), CleanupFix, HighPriorityAction {

    override fun getFamilyName() = "Replace deprecated symbol usage"

    override fun getText() = "Replace with '${replaceWith.pattern}'" //TODO: substitute?

    override fun invoke(replacementStrategy: UsageReplacementStrategy, project: Project, editor: Editor?) {
        val result = replacementStrategy.createReplacer(element)!!.invoke()
        val offset = (result.getCalleeExpressionIfAny() ?: result).textOffset
        editor?.moveCaret(offset)
    }

    companion object : JetSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            val (nameExpression, replacement) = DeprecatedSymbolUsageFixBase.extractDataFromDiagnostic(diagnostic) ?: return null
            return DeprecatedSymbolUsageFix(nameExpression, replacement)
        }

        public fun isImportToBeRemoved(import: JetImportDirective): Boolean {
            return !import.isAllUnder
                   && import.targetDescriptors().all { DeprecatedSymbolUsageFixBase.fetchReplaceWithPattern(it, import.project) != null }
        }
    }
}
