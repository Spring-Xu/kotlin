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

package org.jetbrains.kotlin.idea.search.ideaExtensions

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.impl.search.AnnotatedElementsSearcher
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.kotlin.asJava.LightClassUtil
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.stubindex.JetAnnotationsIndex
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import java.util.ArrayList

public class KotlinAnnotatedElementsSearcher : AnnotatedElementsSearcher() {

    override fun execute(p: AnnotatedElementsSearch.Parameters, consumer: Processor<PsiModifierListOwner>): Boolean {
        val annClass = p.getAnnotationClass()
        assert(annClass.isAnnotationType(), "Annotation type should be passed to annotated members search")

        val annotationFQN = annClass.getQualifiedName()
        assert(annotationFQN != null)

        val useScope = p.getScope()

        for (elt in getJetAnnotationCandidates(annClass, useScope)) {
            if (notJetAnnotationEntry(elt)) continue

            ApplicationManager.getApplication().runReadAction(object : Runnable {
                override fun run() {
                    val parentOfType = PsiTreeUtil.getParentOfType<JetDeclaration>(elt, javaClass<JetDeclaration>()) ?: return

                    val annotationEntry = elt as JetAnnotationEntry

                    val context = annotationEntry.analyze(BodyResolveMode.PARTIAL)
                    val annotationDescriptor = context.get<JetAnnotationEntry, AnnotationDescriptor>(BindingContext.ANNOTATION, annotationEntry)
                    if (annotationDescriptor == null) return

                    val descriptor = annotationDescriptor.getType().getConstructor().getDeclarationDescriptor()
                    if (descriptor == null) return
                    if (!(DescriptorUtils.getFqName(descriptor).asString() == annotationFQN)) return

                    if (parentOfType is JetClass) {
                        val lightClass = LightClassUtil.getPsiClass(parentOfType as JetClass?)
                        consumer.process(lightClass)
                    }
                    else if (parentOfType is JetNamedFunction || parentOfType is JetSecondaryConstructor) {
                        val wrappedMethod = LightClassUtil.getLightClassMethod(parentOfType as JetFunction)
                        consumer.process(wrappedMethod)
                    }
                }
            })
        }

        return true
    }

    companion object {
        private val LOG = Logger.getInstance("#com.intellij.psi.impl.search.AnnotatedMembersSearcher")

        /* Return all elements annotated with given annotation name. Aliases don't work now. */
        private fun getJetAnnotationCandidates(annClass: PsiClass, useScope: SearchScope): Collection<PsiElement> {
            return ApplicationManager.getApplication().runReadAction(object : Computable<Collection<PsiElement>> {
                override fun compute(): Collection<PsiElement> {
                    if (useScope is GlobalSearchScope) {
                        val name = annClass.getName() ?: return emptyList()
                        val annotationEntries = JetAnnotationsIndex.getInstance().get(name, annClass.getProject(), useScope)

                        // Add annotations 'test' as often used alias when we search Test annotation
                        if (name == "Test") {
                            annotationEntries.addAll(JetAnnotationsIndex.getInstance().get(name.toLowerCase(), annClass.getProject(), useScope))
                        }

                        return annotationEntries
                    }

                    // TODO getJetAnnotationCandidates works only with global search scope
                    return ArrayList()
                }
            })
        }

        private fun notJetAnnotationEntry(found: PsiElement): Boolean {
            if (found is JetAnnotationEntry) return false

            val faultyContainer = PsiUtilCore.getVirtualFile(found)
            LOG.error("Non annotation in annotations list: $faultyContainer; element:$found")
            if (faultyContainer != null && faultyContainer.isValid()) {
                FileBasedIndex.getInstance().requestReindex(faultyContainer)
            }

            return true
        }
    }

}