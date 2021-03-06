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

package org.jetbrains.kotlin.idea;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathMacros;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.caches.JarUserDataManager;
import org.jetbrains.kotlin.idea.debugger.filter.DebuggerFiltersUtilKt;
import org.jetbrains.kotlin.idea.decompiler.HasCompiledKotlinInJar;
import org.jetbrains.kotlin.idea.framework.KotlinJavaScriptLibraryDetectionUtil;
import org.jetbrains.kotlin.idea.references.BuiltInsReferenceResolver;
import org.jetbrains.kotlin.utils.PathUtil;

import java.io.File;
import java.io.IOException;

public class PluginStartupComponent implements ApplicationComponent {
    private static final String KOTLIN_BUNDLED = "KOTLIN_BUNDLED";

    public static PluginStartupComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(PluginStartupComponent.class);
    }

    @Override
    @NotNull
    public String getComponentName() {
        return PluginStartupComponent.class.getName();
    }

    @Override
    public void initComponent() {
        registerPathVariable();

        JarUserDataManager.INSTANCE$.register(KotlinJavaScriptLibraryDetectionUtil.HasKotlinJSMetadataInJar.INSTANCE$);
        JarUserDataManager.INSTANCE$.register(HasCompiledKotlinInJar.INSTANCE$);

        BuiltInsReferenceResolver.Companion.refreshBuiltIns();

        DebuggerFiltersUtilKt.addKotlinStdlibDebugFilterIfNeeded();
    }

    private static void registerPathVariable() {
        PathMacros macros = PathMacros.getInstance();
        macros.setMacro(KOTLIN_BUNDLED, PathUtil.getKotlinPathsForIdeaPlugin().getHomePath().getPath());
    }

    private String aliveFlagPath;

    public synchronized String getAliveFlagPath() {
        if (this.aliveFlagPath == null) {
            try {
                File flagFile = File.createTempFile("kotlin-idea-", "-is-running");
                flagFile.deleteOnExit();
                this.aliveFlagPath = flagFile.getAbsolutePath();
            }
            catch (IOException e) {
                this.aliveFlagPath = "";
            }
        }
        return this.aliveFlagPath;
    }

    public synchronized void resetAliveFlag() {
        if (this.aliveFlagPath != null) {
            File flagFile = new File(this.aliveFlagPath);
            if (flagFile.exists()) {
                if (flagFile.delete()) {
                    this.aliveFlagPath = null;
                }
            }
        }
    }

    @Override
    public void disposeComponent() {}
}
