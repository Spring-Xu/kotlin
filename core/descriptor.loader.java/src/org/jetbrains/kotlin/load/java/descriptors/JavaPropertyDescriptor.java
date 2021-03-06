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

package org.jetbrains.kotlin.load.java.descriptors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.*;
import org.jetbrains.kotlin.descriptors.annotations.Annotations;
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.types.JetType;

import java.util.List;

public class JavaPropertyDescriptor extends PropertyDescriptorImpl implements JavaCallableMemberDescriptor {
    private final boolean isStaticFinal;
    public JavaPropertyDescriptor(
            @NotNull DeclarationDescriptor containingDeclaration,
            @NotNull Annotations annotations,
            @NotNull Visibility visibility,
            boolean isVar,
            @NotNull Name name,
            @NotNull SourceElement source,
            @Nullable PropertyDescriptor original,
            boolean isStaticFinal
    ) {
        super(containingDeclaration, original, annotations, Modality.FINAL, visibility, isVar, name, Kind.DECLARATION, source,
              /* lateInit = */ false, /* isConst = */ false);

        this.isStaticFinal = isStaticFinal;
    }

    @Override
    public boolean hasSynthesizedParameterNames() {
        return false;
    }

    @NotNull
    @Override
    public JavaCallableMemberDescriptor enhance(
            @Nullable JetType enhancedReceiverType,
            @NotNull List<JetType> enhancedValueParametersTypes,
            @NotNull JetType enhancedReturnType
    ) {
        JavaPropertyDescriptor enhanced = new JavaPropertyDescriptor(
                getContainingDeclaration(),
                getAnnotations(),
                getVisibility(),
                isVar(),
                getName(),
                getSource(),
                getOriginal(),
                isStaticFinal
        );
        assert getGetter() == null : "Field must not have a getter: " + this;
        assert getSetter() == null : "Field must not have a setter: " + this;
        enhanced.initialize(null, null);
        enhanced.setSetterProjectedOut(isSetterProjectedOut());
        if (compileTimeInitializer != null) {
            enhanced.setCompileTimeInitializer(compileTimeInitializer);
        }
        enhanced.setType(
                enhancedReturnType,
                getTypeParameters(), // TODO
                getDispatchReceiverParameter(),
                enhancedReceiverType
        );
        return enhanced;
    }

    @Override
    public boolean isConst() {
        return isStaticFinal && ConstUtil.canBeUsedForConstVal(getType());
    }
}
