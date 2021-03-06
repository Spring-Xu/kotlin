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

package org.jetbrains.kotlin.codegen;

import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor;
import org.jetbrains.kotlin.psi.JetParameter;

import static org.jetbrains.kotlin.resolve.DescriptorToSourceUtils.descriptorToDeclaration;

public interface DefaultParameterValueLoader {

    StackValue genValue(ValueParameterDescriptor descriptor, ExpressionCodegen codegen);

    DefaultParameterValueLoader DEFAULT = new DefaultParameterValueLoader() {
        @Override
        public StackValue genValue(
                ValueParameterDescriptor descriptor,
                ExpressionCodegen codegen
        ) {
            JetParameter jetParameter = (JetParameter) descriptorToDeclaration(descriptor);
            assert jetParameter != null;
            return codegen.gen(jetParameter.getDefaultValue());
        }
    };
}

