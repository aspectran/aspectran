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
package com.aspectran.core.context;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.AbstractDiscriminator;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.utils.StringUtils;

/**
 * Discriminates logging events based on the name given to the ActivityContext of the current CoreService.
 * <p>ex)
 * <pre>{@code
 *   <appender name="SIFT" class="ch.qos.logback.classic.sift.SiftingAppender">
 *     <discriminator class="com.aspectran.core.context.ContextBasedLoggingGroupDiscriminator">
 *       <key>LOGGING_GROUP</key>
 *       <defaultValue>app</defaultValue>
 *     </discriminator>
 *     <sift>
 *       <appender name="FILE-${LOGGING_GROUP}" class="ch.qos.logback.core.rolling.RollingFileAppender">
 *         <file>${aspectran.basePath:-app}/logs/${LOGGING_GROUP}.log</file>
 *         ...
 *       </appender>
 *     </sift>
 *   </appender>
 * }</pre></p>
 * @see <a href="https://logback.qos.ch/manual/loggingSeparation.html">Logging separation</a>
 */
public class ContextBasedLoggingGroupDiscriminator extends AbstractDiscriminator<ILoggingEvent> {

    private String key;

    private String defaultValue;

    /**
     * Returns the name of the current CoreService's ActivityContext.
     * If that value is null, then return the value assigned to the defaultValue
     * property.
     */
    @Override
    public String getDiscriminatingValue(ILoggingEvent event) {
        CoreService service = CoreServiceHolder.acquire();
        if (service != null) {
            ActivityContext context = service.getActivityContext();
            if (context != null && context.getName() != null) {
                return context.getName();
            }
        }
        return defaultValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void start() {
        int errors = 0;
        if (!StringUtils.hasText(key)) {
            errors++;
            addError("The 'key' property must be set");
        }
        if (!StringUtils.hasText(defaultValue)) {
            errors++;
            addError("The 'defaultValue' property must be set");
        }
        if (errors == 0) {
            super.start();
        }
    }

}
