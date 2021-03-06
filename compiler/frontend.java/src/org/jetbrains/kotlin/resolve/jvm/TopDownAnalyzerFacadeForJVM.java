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

package org.jetbrains.kotlin.resolve.jvm;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.builtins.KotlinBuiltIns;
import org.jetbrains.kotlin.context.ModuleContext;
import org.jetbrains.kotlin.context.MutableModuleContext;
import org.jetbrains.kotlin.descriptors.*;
import org.jetbrains.kotlin.frontend.java.di.ContainerForTopDownAnalyzerForJvm;
import org.jetbrains.kotlin.frontend.java.di.DiPackage;
import org.jetbrains.kotlin.incremental.components.LookupTracker;
import org.jetbrains.kotlin.load.kotlin.incremental.IncrementalPackageFragmentProvider;
import org.jetbrains.kotlin.load.kotlin.incremental.IncrementalPackagePartProvider;
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCache;
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCompilationComponents;
import org.jetbrains.kotlin.modules.Module;
import org.jetbrains.kotlin.modules.ModulesPackage;
import org.jetbrains.kotlin.modules.TargetId;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.platform.JavaToKotlinClassMap;
import org.jetbrains.kotlin.platform.PlatformToKotlinClassMap;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.kotlin.resolve.*;
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisCompletedHandlerExtension;
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory;
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter;
import org.jetbrains.kotlin.resolve.scopes.JetScope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.jetbrains.kotlin.context.ContextPackage.ContextForNewModule;

public enum TopDownAnalyzerFacadeForJVM {

    INSTANCE;

    public static final List<ImportPath> DEFAULT_IMPORTS = buildDefaultImports();

    private static void addAllClassifiersToImportPathList(List<ImportPath> list, JetScope scope) {
        for (DeclarationDescriptor descriptor : scope.getDescriptors(DescriptorKindFilter.CLASSIFIERS, JetScope.ALL_NAME_FILTER)) {
            list.add(new ImportPath(DescriptorUtils.getFqNameSafe(descriptor), false));
        }
    }

    private static List<ImportPath> buildDefaultImports() {
        List<ImportPath> list = new ArrayList<ImportPath>();
        list.add(new ImportPath("java.lang.*"));
        list.add(new ImportPath("kotlin.*"));
        list.add(new ImportPath("kotlin.annotation.*"));
        list.add(new ImportPath("kotlin.jvm.*"));
        list.add(new ImportPath("kotlin.io.*"));

        addAllClassifiersToImportPathList(list, KotlinBuiltIns.getInstance().getBuiltInsPackageScope());
        addAllClassifiersToImportPathList(list, KotlinBuiltIns.getInstance().getAnnotationPackageScope());

        return list;
    }

    public static ModuleParameters JVM_MODULE_PARAMETERS = new ModuleParameters() {
        @NotNull
        @Override
        public List<ImportPath> getDefaultImports() {
            return DEFAULT_IMPORTS;
        }

        @NotNull
        @Override
        public PlatformToKotlinClassMap getPlatformToKotlinClassMap() {
            return JavaToKotlinClassMap.INSTANCE;
        }
    };

    @NotNull
    public static AnalysisResult analyzeFilesWithJavaIntegrationNoIncremental(
            @NotNull ModuleContext moduleContext,
            @NotNull Collection<JetFile> files,
            @NotNull BindingTrace trace,
            @NotNull TopDownAnalysisMode topDownAnalysisMode,
            PackagePartProvider packagePartProvider
    ) {
        return analyzeFilesWithJavaIntegration(moduleContext, files, trace, topDownAnalysisMode, null, null, packagePartProvider);
    }

    @NotNull
    public static AnalysisResult analyzeFilesWithJavaIntegrationWithCustomContext(
            @NotNull ModuleContext moduleContext,
            @NotNull Collection<JetFile> files,
            @NotNull BindingTrace trace,
            @Nullable List<Module> modules,
            @Nullable IncrementalCompilationComponents incrementalCompilationComponents,
            @NotNull PackagePartProvider packagePartProvider
    ) {
        return analyzeFilesWithJavaIntegration(
                moduleContext, files, trace, TopDownAnalysisMode.TopLevelDeclarations, modules, incrementalCompilationComponents,
                packagePartProvider);
    }

    @NotNull
    private static AnalysisResult analyzeFilesWithJavaIntegration(
            @NotNull ModuleContext moduleContext,
            @NotNull Collection<JetFile> files,
            @NotNull BindingTrace trace,
            @NotNull TopDownAnalysisMode topDownAnalysisMode,
            @Nullable List<Module> modules,
            @Nullable IncrementalCompilationComponents incrementalCompilationComponents,
            @NotNull PackagePartProvider packagePartProvider
    ) {
        Project project = moduleContext.getProject();
        List<JetFile> allFiles = JvmAnalyzerFacade.getAllFilesToAnalyze(project, null, files);

        FileBasedDeclarationProviderFactory providerFactory =
                new FileBasedDeclarationProviderFactory(moduleContext.getStorageManager(), allFiles);

        LookupTracker lookupTracker =
                incrementalCompilationComponents != null ? incrementalCompilationComponents.getLookupTracker() : LookupTracker.DO_NOTHING;

        List<TargetId> targetIds = null;
        if (modules != null) {
            targetIds = new ArrayList<TargetId>(modules.size());

            for (Module module : modules) {
                targetIds.add(ModulesPackage.TargetId(module));
            }
        }

        packagePartProvider = IncrementalPackagePartProvider.create(packagePartProvider, files, targetIds, incrementalCompilationComponents, moduleContext.getStorageManager());

        ContainerForTopDownAnalyzerForJvm container = DiPackage.createContainerForTopDownAnalyzerForJvm(
                moduleContext,
                trace,
                providerFactory,
                GlobalSearchScope.allScope(project),
                lookupTracker,
                packagePartProvider
        );

        List<PackageFragmentProvider> additionalProviders = new ArrayList<PackageFragmentProvider>();

        if (targetIds != null && incrementalCompilationComponents != null) {
            for (TargetId targetId : targetIds) {
                IncrementalCache incrementalCache = incrementalCompilationComponents.getIncrementalCache(targetId);

                additionalProviders.add(
                        new IncrementalPackageFragmentProvider(
                                files, moduleContext.getModule(), moduleContext.getStorageManager(),
                                container.getDeserializationComponentsForJava().getComponents(),
                                incrementalCache, targetId
                        )
                );
            }
        }
        additionalProviders.add(container.getJavaDescriptorResolver().getPackageFragmentProvider());

        container.getLazyTopDownAnalyzerForTopLevel().analyzeFiles(topDownAnalysisMode, allFiles, additionalProviders);

        BindingContext bindingContext = trace.getBindingContext();
        ModuleDescriptor module = moduleContext.getModule();

        Collection<AnalysisCompletedHandlerExtension> analysisCompletedHandlerExtensions =
                AnalysisCompletedHandlerExtension.Companion.getInstances(moduleContext.getProject());

        for (AnalysisCompletedHandlerExtension extension : analysisCompletedHandlerExtensions) {
            AnalysisResult result = extension.analysisCompleted(project, module, bindingContext, files);
            if (result != null) return result;
        }

        return AnalysisResult.success(bindingContext, module);
    }

    @NotNull
    public static MutableModuleContext createContextWithSealedModule(@NotNull Project project, @NotNull String moduleName) {
        MutableModuleContext context = ContextForNewModule(
                project, Name.special("<" + moduleName + ">"), JVM_MODULE_PARAMETERS
        );
        context.setDependencies(context.getModule(), KotlinBuiltIns.getInstance().getBuiltInsModule());
        return context;
    }
}
