/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.template.engine.freemaker;

import com.aspectran.core.context.bean.ablility.FactoryBean;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Factory that configures a FreeMarker Configuration.
 *
 * <p>Created: 2016. 01. 09</p>
 */
public class FreeMarkerConfigurationFactory {

    protected final Log logger = LogFactory.getLog(getClass());

    private Properties freemarkerSettings;

    private Map<String, Object> freemarkerVariables;

    private String defaultEncoding;

    /**
     * Set properties that contain well-known FreeMarker keys which will be
     * passed to FreeMarker's {@code Configuration.setSettings} method.
     * @see freemarker.template.Configuration#setSettings
     */
    public void setFreemarkerSettings(Properties settings) {
        this.freemarkerSettings = settings;
    }

    /**
     * Set a Map that contains well-known FreeMarker objects which will be passed
     * to FreeMarker's {@code Configuration.setAllSharedVariables()} method.
     * @see freemarker.template.Configuration#setAllSharedVariables
     */
    public void setFreemarkerVariables(Map<String, Object> variables) {
        this.freemarkerVariables = variables;
    }

    /**
     * Set the default encoding for the FreeMarker configuration.
     * If not specified, FreeMarker will use the platform file encoding.
     * <p>Used for template rendering unless there is an explicit encoding specified
     * for the rendering process (for example, on Spring's FreeMarkerView).
     * @see freemarker.template.Configuration#setDefaultEncoding
     */
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Prepare the FreeMarker Configuration and return it.
     * @return the FreeMarker Configuration object
     * @throws IOException if the config file wasn't found
     * @throws TemplateException on FreeMarker initialization failure
     */
    public Configuration createConfiguration() throws IOException, TemplateException {
        Configuration config = newConfiguration();
        Properties props = new Properties();

        // Merge local properties if specified.
        if (this.freemarkerSettings != null) {
            props.putAll(this.freemarkerSettings);
        }

        // FreeMarker will only accept known keys in its setSettings and
        // setAllSharedVariables methods.
        if (!props.isEmpty()) {
            config.setSettings(props);
        }

        if (this.freemarkerVariables != null && freemarkerVariables.size() > 0) {
            config.setAllSharedVariables(new SimpleHash(this.freemarkerVariables, config.getObjectWrapper()));
        }

        if (this.defaultEncoding != null) {
            config.setDefaultEncoding(this.defaultEncoding);
        }

        return config;
    }

    /**
     * Return a new Configuration object. Subclasses can override this for custom
     * initialization (e.g. specifying a FreeMarker compatibility level which is a
     * new feature in FreeMarker 2.3.21), or for using a mock object for testing.
     * <p>Called by {@code createConfiguration()}.
     * @return the Configuration object
     * @throws IOException if a config file wasn't found
     * @throws TemplateException on FreeMarker initialization failure
     * @see #createConfiguration()
     */
    protected Configuration newConfiguration() throws IOException, TemplateException {
        return new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    }

}
