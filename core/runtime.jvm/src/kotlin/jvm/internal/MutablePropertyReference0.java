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

package kotlin.jvm.internal;

import kotlin.reflect.KMutableProperty0;
import kotlin.reflect.KProperty0;

public class MutablePropertyReference0 extends MutablePropertyReference implements KMutableProperty0 {
    @Override
    public Object get() {
        throw error();
    }

    @Override
    public void set(Object value) {
        throw error();
    }

    @Override
    public KProperty0.Getter getGetter() {
        throw error();
    }

    @Override
    public KMutableProperty0.Setter getSetter() {
        throw error();
    }
}
