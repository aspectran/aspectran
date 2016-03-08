/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.core.context.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.aspectran.core.context.bean.aware.ClassLoaderAware;
import com.aspectran.core.context.loader.AspectranClassLoader;

/**
 * <p>Created: 2016. 2. 8.</p>
 */
public class ResourceBundleMessageSource extends AbstractMessageSource implements ClassLoaderAware {

    private String defaultEncoding = "ISO-8859-1";

    private ClassLoader classLoader;

    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    public ClassLoader getClassLoader() {
        if(classLoader == null) {
            return AspectranClassLoader.getDefaultClassLoader();
        }
        return classLoader;
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Obtain the resource bundle for the given basename and Locale.
     *
     * @param basename the basename to look for
     * @param locale the Locale to look for
     * @return the corresponding ResourceBundle
     * @throws MissingResourceException if no matching bundle could be found
     * @see java.util.ResourceBundle#getBundle(String, Locale, ClassLoader)
     */
    protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
        return ResourceBundle.getBundle(basename, locale, getClassLoader(), new MessageSourceControl());
    }

    /**
     * Load a property-based resource bundle from the given reader.
     * <p>The default implementation returns a {@link PropertyResourceBundle}.
     *
     * @param reader the reader for the target resource
     * @return the fully loaded bundle
     * @throws IOException in case of I/O failure
     * @see PropertyResourceBundle#PropertyResourceBundle(Reader)
     */
    protected ResourceBundle loadBundle(Reader reader) throws IOException {
        return new PropertyResourceBundle(reader);
    }

    /**
     * Custom implementation of Java 6's {@code ResourceBundle.Control},
     * adding support for custom file encodings, deactivating the fallback to the
     * system locale and activating ResourceBundle's native cache, if desired.
     */
    private class MessageSourceControl extends ResourceBundle.Control {

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            // Special handling of default encoding
            if(format.equals("java.properties")) {
                String bundleName = toBundleName(baseName, locale);
                final String resourceName = toResourceName(bundleName, "properties");
                final ClassLoader classLoader = loader;
                final boolean reloadFlag = reload;
                InputStream stream;
                try {
                    stream = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                        @Override
                        public InputStream run() throws IOException {
                            InputStream is = null;
                            if(reloadFlag) {
                                URL url = classLoader.getResource(resourceName);
                                if(url != null) {
                                    URLConnection connection = url.openConnection();
                                    if(connection != null) {
                                        connection.setUseCaches(false);
                                        is = connection.getInputStream();
                                    }
                                }
                            } else {
                                is = classLoader.getResourceAsStream(resourceName);
                            }
                            return is;
                        }
                    });
                } catch(PrivilegedActionException ex) {
                    throw (IOException) ex.getException();
                }
                if(stream != null) {
                    try {
                        return loadBundle(new InputStreamReader(stream, defaultEncoding));
                    } finally {
                        stream.close();
                    }
                } else {
                    return null;
                }
            } else {
                // Delegate handling of "java.class" format to standard Control
                return super.newBundle(baseName, locale, format, loader, reload);
            }
        }
    }

}
