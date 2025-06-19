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
package com.aspectran.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Base class for JavaBean-style components that need to load properties
 * from one or more resources. Supports local properties as well, with
 * configurable overriding.
 *
 * <p>Created: 2025. 2. 18.</p>
 */
public abstract class PropertiesLoaderSupport {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesLoaderSupport.class);

    private Properties[] localProperties;

    private String[] locations;

    private boolean localOverride = false;

    private boolean ignoreInvalidResource = false;

    /**
     * Set local properties, for example, via the "props" tag in XML bean definitions.
     * These can be considered defaults, to be overridden by properties
     * loaded from files.
     */
    public void setProperties(Properties properties) {
        localProperties = new Properties[] { properties };
    }

    /**
     * Set local properties, for example, via the "props" tag in XML bean definitions,
     * allowing for merging multiple properties sets into one.
     */
    public void setPropertiesArray(Properties... propertiesArray) {
        localProperties = propertiesArray;
    }

    /**
     * Set a location of a properties file to be loaded.
     * <p>Can point to a classic properties file or to an XML file
     * that follows Java's properties XML format.
     */
    public void setLocation(String location) {
        locations = new String[] { location };
    }

    /**
     * Set locations of properties files to be loaded.
     * <p>Can point to classic properties files or to XML files
     * that follow Java's properties XML format.
     * <p>Note: Properties defined in later files will override
     * properties defined earlier files, in case of overlapping keys.
     * Hence, make sure that the most specific files are the last
     * ones in the given list of locations.
     */
    public void setLocations(String... locations) {
        this.locations = locations;
    }

    /**
     * Set whether local properties override properties from files.
     * <p>Default is "false": Properties from files override local defaults.
     * Can be switched to "true" to let local properties override defaults
     * from files.
     */
    public void setLocalOverride(boolean localOverride) {
        this.localOverride = localOverride;
    }

    /**
     * Set if failure to find the property resource should be ignored.
     * <p>"true" is appropriate if the properties file is completely optional.
     * Default is "false".
     */
    public void setIgnoreInvalidResource(boolean ignoreInvalidResource) {
        this.ignoreInvalidResource = ignoreInvalidResource;
    }

    /**
     * Return a merged Properties instance containing both the
     * loaded properties and properties set on this FactoryBean.
     */
    protected Properties mergeProperties() throws IOException {
        Properties result = new Properties();

        if (localOverride) {
            // Load properties from file upfront, to let local properties override.
            loadProperties(result);
        }

        if (localProperties != null) {
            for (Properties localProp : localProperties) {
                for (Enumeration<?> en = localProp.propertyNames(); en.hasMoreElements();) {
                    String name = (String)en.nextElement();
                    Object value = localProp.get(name);
                    if (value == null) {
                        // Allow for defaults fallback or potentially overridden accessor...
                        value = localProp.getProperty(name);
                    }
                    result.put(name, value);
                }
            }
        }

        if (!localOverride) {
            // Load properties from file afterwards, to let those properties override.
            loadProperties(result);
        }

        return result;
    }

    /**
     * Load properties into the given instance.
     * @param props the Properties instance to load into
     * @throws IOException in case of I/O errors
     * @see #setLocations
     */
    protected void loadProperties(Properties props) throws IOException {
        if (locations != null) {
            for (String location : locations) {
                try {
                    InputStream is = getResourceAsStream(location);
                    PropertiesLoaderUtils.loadIntoProperties(props, location, is);
                } catch (IOException e) {
                    if (ignoreInvalidResource) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("IGNORED - Invalid properties resource; {}", e.toString());
                        }
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    protected abstract InputStream getResourceAsStream(String location) throws IOException;

}
