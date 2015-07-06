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

package org.jetbrains.kotlin.cli.common.messages

import com.google.common.base.Predicate

public class FilteringMessageCollector(private val messageCollector: MessageCollector, private val predicate: Predicate<CompilerMessageSeverity>) : MessageCollector {

    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation) {
        if (predicate.apply(severity)) {
            messageCollector.report(severity, message, location)
        }
    }
}