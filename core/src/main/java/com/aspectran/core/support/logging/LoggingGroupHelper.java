/*
 * Copyright (c) 2008-2024 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.support.logging;

import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.MDC;

/*
 * Since logs are output asynchronously, the key to identify
 * the logging group from MDC should not be removed immediately.
 * In this case, the key may remain in the existing thread and
 * must be initialized or removed in the next request.
 */
public abstract class LoggingGroupHelper {

    public static final String LOGGING_GROUP = "LOGGING_GROUP";

    public static void set(@NonNull String groupName) {
        MDC.put(LOGGING_GROUP, groupName);
    }

    public static void clear() {
        MDC.remove(LOGGING_GROUP);
    }

}