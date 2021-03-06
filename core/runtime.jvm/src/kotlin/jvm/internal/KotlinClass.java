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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface KotlinClass {
    @Deprecated
    int abiVersion();

    int[] version() default {};

    Kind kind() default Kind.CLASS;

    String[] data();

    enum Kind {
        CLASS,

        /**
         * A class has kind LOCAL_CLASS if and only if it's not an anonymous object and its first non-class container is not a package.
         */
        LOCAL_CLASS,

        ANONYMOUS_OBJECT,
    }
}
