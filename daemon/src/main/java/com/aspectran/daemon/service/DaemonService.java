/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.daemon.service;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.aspectran.core.context.ActivityContext.BASE_DIR_PROPERTY_NAME;
import static com.aspectran.core.context.config.AspectranConfig.DEFAULT_ASPECTRAN_CONFIG_FILE;

/**
 * The Interface DaemonService.
 *
 * <p>Created: 2017. 10. 28.</p>
 */
public interface DaemonService extends CoreService {

    String DEFAULT_ROOT_CONTEXT = "classpath:root-config.xml";

    SessionAdapter newSessionAdapter();

    /**
     * Execute the translet.
     *
     * @param name the translet name
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translet(String name, ParameterMap parameterMap, Map<String, Object> attributeMap);

    /**
     * Execute the translet.
     *
     * @param name the translet name
     * @param method the request method
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translet(String name, MethodType method, ParameterMap parameterMap, Map<String, Object> attributeMap);


    /**
     * Evaluate the template with a set of parameters and a set of attributes.
     *
     * @param templateId the template id
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the output string of the template
     */
    String template(String templateId, ParameterMap parameterMap, Map<String, Object> attributeMap);

    /**
     * Returns a new instance of DaemonService.
     *
     * @param rootConfigLocation the root configuration location
     * @return the instance of DaemonService
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    static DaemonService create(String rootConfigLocation)
            throws AspectranServiceException, IOException {
        return AspectranDaemonService.create(rootConfigLocation);
    }

    /**
     * Returns a new instance of DaemonService.
     *
     * @param aspectranConfig the parameters for aspectran configuration
     * @return the instance of DaemonService
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    static DaemonService create(AspectranConfig aspectranConfig)
            throws AspectranServiceException, IOException {
        return AspectranDaemonService.create(aspectranConfig);
    }

    static File determineAspectranConfigFile(String arg) {
        File file;
        if (!StringUtils.isEmpty(arg)) {
            file = new File(arg);
        } else {
            String baseDir = SystemUtils.getProperty(BASE_DIR_PROPERTY_NAME);
            if (baseDir != null) {
                file = new File(baseDir, DEFAULT_ASPECTRAN_CONFIG_FILE);
            } else {
                file = new File(DEFAULT_ASPECTRAN_CONFIG_FILE);
            }
        }
        return file;
    }

}
