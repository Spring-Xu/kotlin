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

package org.jetbrains.kotlin.serialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor;
import org.jetbrains.kotlin.descriptors.ClassDescriptor;
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor;
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor;
import org.jetbrains.kotlin.types.JetType;

import java.util.Collection;

public abstract class SerializerExtension {
    @NotNull
    public abstract StringTable getStringTable();

    public void serializeClass(@NotNull ClassDescriptor descriptor, @NotNull ProtoBuf.Class.Builder proto) {
    }

    public void serializePackage(@NotNull Collection<PackageFragmentDescriptor> packageFragments, @NotNull ProtoBuf.Package.Builder proto) {
    }

    public void serializeCallable(@NotNull CallableMemberDescriptor callable, @NotNull ProtoBuf.Callable.Builder proto) {
    }

    public void serializeValueParameter(
            @NotNull ValueParameterDescriptor descriptor,
            @NotNull ProtoBuf.Callable.ValueParameter.Builder proto
    ) {
    }

    public void serializeType(@NotNull JetType type, @NotNull ProtoBuf.Type.Builder proto) {
    }

    @NotNull
    public String getLocalClassName(@NotNull ClassDescriptor descriptor) {
        throw new UnsupportedOperationException("Local classes are unsupported: " + descriptor);
    }
}
