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

package org.jetbrains.kotlin.idea.decompiler.textBuilder

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.idea.decompiler.findMultifileClassParts
import org.jetbrains.kotlin.idea.decompiler.navigation.JsMetaFileUtils
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.load.kotlin.KotlinBinaryClassCache
import org.jetbrains.kotlin.load.kotlin.PackageClassUtils
import org.jetbrains.kotlin.load.kotlin.header.isCompatibleClassKind
import org.jetbrains.kotlin.load.kotlin.header.isCompatibleFileFacadeKind
import org.jetbrains.kotlin.load.kotlin.header.isCompatibleMultifileClassKind
import org.jetbrains.kotlin.load.kotlin.header.isCompatiblePackageFacadeKind
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.renderer.DescriptorRendererModifier
import org.jetbrains.kotlin.resolve.DescriptorUtils.isEnumEntry
import org.jetbrains.kotlin.resolve.dataClassUtils.isComponentLike
import org.jetbrains.kotlin.resolve.descriptorUtil.secondaryConstructors
import org.jetbrains.kotlin.types.error.MissingDependencyErrorClass
import org.jetbrains.kotlin.types.flexibility
import org.jetbrains.kotlin.types.isFlexible
import java.util.*

private val FILE_ABI_VERSION_MARKER: String = "FILE_ABI"
private val CURRENT_ABI_VERSION_MARKER: String = "CURRENT_ABI"

public val INCOMPATIBLE_ABI_VERSION_GENERAL_COMMENT: String = "// This class file was compiled with different version of Kotlin compiler and can't be decompiled."
public val INCOMPATIBLE_ABI_VERSION_COMMENT: String =
        "$INCOMPATIBLE_ABI_VERSION_GENERAL_COMMENT\n" +
        "//\n" +
        "// Current compiler ABI version is $CURRENT_ABI_VERSION_MARKER\n" +
        "// File ABI version is $FILE_ABI_VERSION_MARKER"

public fun buildDecompiledText(
        classFile: VirtualFile,
        resolver: ResolverForDecompiler = DeserializerForDecompiler(classFile)
): DecompiledText {
    val kotlinClass = KotlinBinaryClassCache.getKotlinBinaryClass(classFile)
    assert(kotlinClass != null) { "Decompiled data factory shouldn't be called on an unsupported file: " + classFile }
    val classId = kotlinClass!!.getClassId()
    val classHeader = kotlinClass.getClassHeader()
    val packageFqName = classId.getPackageFqName()

    return when {
        !classHeader.isCompatibleAbiVersion -> {
            DecompiledText(
                    INCOMPATIBLE_ABI_VERSION_COMMENT
                            .replace(CURRENT_ABI_VERSION_MARKER, JvmAbi.VERSION.toString())
                            .replace(FILE_ABI_VERSION_MARKER, classHeader.version.toString()),
                    mapOf())
        }
        classHeader.isCompatiblePackageFacadeKind(),
        classHeader.isCompatibleFileFacadeKind() ->
            buildDecompiledText(packageFqName, ArrayList(resolver.resolveDeclarationsInFacade(classId.asSingleFqName())))
        classHeader.isCompatibleClassKind() ->
            buildDecompiledText(packageFqName, listOf(resolver.resolveTopLevelClass(classId)).filterNotNull())
        classHeader.isCompatibleMultifileClassKind() -> {
            val partClasses = findMultifileClassParts(classFile, kotlinClass)
            val partMembers = partClasses.flatMap { partClass -> resolver.resolveDeclarationsInFacade(partClass.classId.asSingleFqName()) }
            buildDecompiledText(packageFqName, partMembers)
        }
        else ->
            throw UnsupportedOperationException("Unknown header kind: ${classHeader.kind} ${classHeader.isCompatibleAbiVersion}")
    }
}

public fun buildDecompiledTextFromJsMetadata(
        classFile: VirtualFile,
        resolver: ResolverForDecompiler = KotlinJavaScriptDeserializerForDecompiler(classFile)
): DecompiledText {
    val packageFqName = JsMetaFileUtils.getPackageFqName(classFile)
    val isPackageHeader = JsMetaFileUtils.isPackageHeader(classFile)

    if (isPackageHeader) {
        return buildDecompiledText(packageFqName,
                                   resolveDeclarationsInPackage(packageFqName, resolver),
                                   descriptorRendererForKotlinJavascriptDecompiler)
    }
    else {
        val classId = JsMetaFileUtils.getClassId(classFile)
        return buildDecompiledText(packageFqName, listOf(resolver.resolveTopLevelClass(classId)).filterNotNull(), descriptorRendererForKotlinJavascriptDecompiler)
    }
}

private fun resolveDeclarationsInPackage(packageFqName: FqName, resolver: ResolverForDecompiler) =
        ArrayList(resolver.resolveDeclarationsInFacade(PackageClassUtils.getPackageClassFqName(packageFqName)))

private val DECOMPILED_CODE_COMMENT = "/* compiled code */"
private val DECOMPILED_COMMENT_FOR_PARAMETER = "/* = compiled code */"
private val FLEXIBLE_TYPE_COMMENT = "/* platform type */"

private val descriptorRendererForDecompiler = DescriptorRenderer.withOptions {
    withDefinedIn = false
    classWithPrimaryConstructor = true
    typeNormalizer = { type -> if (type.isFlexible()) type.flexibility().lowerBound else type }
    secondaryConstructorsAsPrimary = false
    modifiers = DescriptorRendererModifier.ALL
}

private val descriptorRendererForKotlinJavascriptDecompiler = DescriptorRenderer.withOptions {
    withDefinedIn = false
    classWithPrimaryConstructor = true
    secondaryConstructorsAsPrimary = false
    modifiers = DescriptorRendererModifier.ALL
}

private val descriptorRendererForKeys = DescriptorRenderer.COMPACT_WITH_MODIFIERS.withOptions {
    modifiers = DescriptorRendererModifier.ALL
}

public fun descriptorToKey(descriptor: DeclarationDescriptor): String {
    return descriptorRendererForKeys.render(descriptor)
}

public data class DecompiledText(public val text: String, public val renderedDescriptorsToRange: Map<String, TextRange>)

public fun buildDecompiledText(
        packageFqName: FqName,
        descriptors: List<DeclarationDescriptor>,
        descriptorRenderer: DescriptorRenderer = descriptorRendererForDecompiler
): DecompiledText {
    val builder = StringBuilder()
    val renderedDescriptorsToRange = HashMap<String, TextRange>()

    fun appendDecompiledTextAndPackageName() {
        builder.append("// IntelliJ API Decompiler stub source generated from a class file\n" + "// Implementation of methods is not available")
        builder.append("\n\n")
        if (!packageFqName.isRoot()) {
            builder.append("package ").append(packageFqName).append("\n\n")
        }
    }

    fun saveDescriptorToRange(descriptor: DeclarationDescriptor, startOffset: Int, endOffset: Int) {
        renderedDescriptorsToRange[descriptorToKey(descriptor)] = TextRange(startOffset, endOffset)
    }

    fun appendDescriptor(descriptor: DeclarationDescriptor, indent: String, lastEnumEntry: Boolean? = null) {
        if (descriptor is MissingDependencyErrorClass) {
            throw IllegalStateException("${descriptor.javaClass.getSimpleName()} cannot be rendered. FqName: ${descriptor.fullFqName}")
        }
        val startOffset = builder.length()
        val header = if (isEnumEntry(descriptor))
            descriptor.name.asString() + if (lastEnumEntry!!) ";" else ","
        else
            descriptorRenderer.render(descriptor).replace("= ...", DECOMPILED_COMMENT_FOR_PARAMETER)
        builder.append(header)
        var endOffset = builder.length()

        if (descriptor is CallableDescriptor) {
            //NOTE: assuming that only return types can be flexible
            if (descriptor.getReturnType()!!.isFlexible()) {
                builder.append(" ").append(FLEXIBLE_TYPE_COMMENT)
            }
        }

        if (descriptor is FunctionDescriptor || descriptor is PropertyDescriptor) {
            if ((descriptor as MemberDescriptor).getModality() != Modality.ABSTRACT) {
                if (descriptor is FunctionDescriptor) {
                    builder.append(" { ").append(DECOMPILED_CODE_COMMENT).append(" }")
                }
                else {
                    // descriptor instanceof PropertyDescriptor
                    builder.append(" ").append(DECOMPILED_CODE_COMMENT)
                }
                endOffset = builder.length()
            }
        }
        else if (descriptor is ClassDescriptor && !isEnumEntry(descriptor)) {
            builder.append(" {\n")

            val subindent = indent + "    "

            var firstPassed = false
            fun newlineExceptFirst() {
                if (firstPassed) {
                    builder.append("\n")
                }
                else {
                    firstPassed = true
                }
            }

            val allDescriptors = descriptor.secondaryConstructors + descriptor.defaultType.memberScope.getDescriptors()
            val (enumEntries, members) = allDescriptors.partition(::isEnumEntry)

            for ((index, enumEntry) in enumEntries.withIndex()) {
                newlineExceptFirst()
                builder.append(subindent)
                appendDescriptor(enumEntry, subindent, index == enumEntries.lastIndex)
            }

            val companionObject = descriptor.companionObjectDescriptor
            if (companionObject != null) {
                newlineExceptFirst()
                builder.append(subindent)
                appendDescriptor(companionObject, subindent)
            }

            for (member in members) {
                if (member.containingDeclaration != descriptor) {
                    continue
                }
                if (member == companionObject) {
                    continue
                }
                if (member is CallableMemberDescriptor
                    && member.kind != CallableMemberDescriptor.Kind.DECLARATION
                    //TODO: not synthesized and component like
                    && !isComponentLike(member.name)) {
                    continue
                }
                newlineExceptFirst()
                builder.append(subindent)
                appendDescriptor(member, subindent)
            }

            builder.append(indent).append("}")
            endOffset = builder.length()
        }

        builder.append("\n")
        saveDescriptorToRange(descriptor, startOffset, endOffset)

        if (descriptor is ClassDescriptor) {
            val primaryConstructor = descriptor.unsubstitutedPrimaryConstructor
            if (primaryConstructor != null) {
                saveDescriptorToRange(primaryConstructor, startOffset, endOffset)
            }
        }
    }

    appendDecompiledTextAndPackageName()
    for (member in descriptors) {
        appendDescriptor(member, "")
        builder.append("\n")
    }

    return DecompiledText(builder.toString(), renderedDescriptorsToRange)
}
