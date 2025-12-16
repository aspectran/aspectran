/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.AbstractDiscriminator;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * A logback discriminator that separates logging events based on a value in the MDC
 * or the name of the current {@link com.aspectran.core.context.ActivityContext}.
 * <p>This is useful for separating logs per tenant in a multi-tenant application
 * or for different application contexts.</p>
 * <p>The discriminating value is determined as follows:
 * <ol>
 *   <li>Check for a value in the MDC using the configured 'key'.</li>
 *   <li>If not found, use the name of the current {@code ActivityContext}.</li>
 *   <li>If still not found, use the configured 'defaultValue'.</li>
 * </ol>
 * </p>
 * <p>Example logback configuration:
 * <pre>{@code
 *   <appender name="SIFT" class="ch.qos.logback.classic.sift.SiftingAppender">
 *     <discriminator class="com.aspectran.logging.LoggingGroupDiscriminator">
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
public class LoggingGroupDiscriminator extends AbstractDiscriminator<ILoggingEvent> {

    private static final String DEFAULT_KEY = "LOGGING_GROUP";

    private String key = DEFAULT_KEY;

    private String defaultValue;

    /**
     * Determines the discriminating value for the logging event.
     * @param event the logging event
     * @return the discriminating value
     */
    @Override
    public String getDiscriminatingValue(@NonNull ILoggingEvent event) {
        String groupName = getGroupName(event);
        if (groupName == null) {
            CoreService service = CoreServiceHolder.acquire();
            if (service != null) {
                ActivityContext context = service.getActivityContext();
                if (context != null && context.getName() != null) {
                    groupName = context.getName();
                }
            }
        }
        if (StringUtils.hasText(groupName)) {
            return groupName;
        } else {
            return defaultValue;
        }
    }

    @Nullable
    private String getGroupName(@NonNull ILoggingEvent event) {
        Map<String, String> mdcMap = event.getMDCPropertyMap();
        return (mdcMap != null ? mdcMap.get(key) : null);
    }

    /**
     * Gets the key that will be used to store the discriminating value in the MDC.
     * @return the key
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * Sets the key that will be used to store the discriminating value in the MDC.
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the default value to use when a discriminating value cannot be determined.
     * @return the default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value to use when a discriminating value cannot be determined.
     * @param defaultValue the default value to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Checks that the key and defaultValue properties have been set.
     */
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
