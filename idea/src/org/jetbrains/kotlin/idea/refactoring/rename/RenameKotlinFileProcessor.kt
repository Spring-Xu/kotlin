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

package org.jetbrains.kotlin.idea.refactoring.rename

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.refactoring.rename.RenamePsiFileProcessor
import org.jetbrains.kotlin.fileClasses.JvmFileClassUtil
import org.jetbrains.kotlin.idea.JetFileType
import org.jetbrains.kotlin.idea.search.allScope
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.psi.JetFile

class RenameKotlinFileProcessor() : RenamePsiFileProcessor() {
    override fun canProcessElement(element: PsiElement) = element is JetFile

    override fun prepareRenaming(element: PsiElement?,
                                 newName: String,
                                 allRenames: MutableMap<PsiElement, String>,
                                 scope: SearchScope) {
        val jetFile = element as? JetFile ?: return
        if (FileTypeManager.getInstance().getFileTypeByFileName(newName) != JetFileType.INSTANCE) {
            return
        }

        val fileInfo = JvmFileClassUtil.getFileClassInfoNoResolve(jetFile)
        if (!fileInfo.isWithJvmName) {
            val facadeFqName = fileInfo.facadeClassFqName
            val project = jetFile.project
            val facadeClass = JavaPsiFacade.getInstance(project).findClass(facadeFqName.asString(), project.allScope())
            if (facadeClass != null) {
                allRenames[facadeClass] = PackagePartClassUtils.getFilePartShortName(newName)
            }
        }
    }
}
