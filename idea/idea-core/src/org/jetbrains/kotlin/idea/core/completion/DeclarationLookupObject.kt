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

package org.jetbrains.kotlin.idea.core.completion

import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiElement
import com.intellij.util.PlatformIcons
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

public interface DeclarationLookupObject : Iconable {
    public val psiElement: PsiElement?
    public val descriptor: DeclarationDescriptor?
    public val name: Name?
    public val importableFqName: FqName?
    public val isDeprecated: Boolean
}

public data class PackageLookupObject(val fqName: FqName) : DeclarationLookupObject {
    override val psiElement: PsiElement? get() = null

    override val descriptor: DeclarationDescriptor? get() = null

    override val name: Name get() = fqName.shortName()

    override val importableFqName: FqName get() = fqName

    override val isDeprecated: Boolean get() = false

    override fun getIcon(flags: Int) = PlatformIcons.PACKAGE_ICON
}

