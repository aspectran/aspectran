/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.support.i18n.message;

import com.aspectran.core.component.bean.aware.ClassLoaderAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>This class is a clone of org.springframework.context.support.ResourceBundleMessageSource</p>
 *
 * {@link MessageSource} implementation that
 * accesses resource bundles using specified basenames. This class relies
 * on the underlying JDK's {@link java.util.ResourceBundle} implementation,
 * in combination with the JDK's standard message parsing provided by
 * {@link java.text.MessageFormat}.
 *
 * <p>This MessageSource caches both the accessed ResourceBundle instances and
 * the generated MessageFormats for each message. It also implements rendering of
 * no-arg messages without MessageFormat, as supported by the AbstractMessageSource
 * base class. The caching provided by this MessageSource is significantly faster
 * than the built-in caching of the {@code java.util.ResourceBundle} class.</p>
 *
 * <p>Unfortunately, {@code java.util.ResourceBundle} caches loaded bundles
 * forever: Reloading a bundle during VM execution is <i>not</i> possible.
 * As this MessageSource relies on ResourceBundle, it faces the same limitation.</p>
 *
 * <p>Created: 2016. 2. 8.</p>
 */
public class ResourceBundleMessageSource extends AbstractMessageSource implements ClassLoaderAware {

    private String defaultEncoding = ActivityContext.DEFAULT_ENCODING;

    private boolean fallbackToSystemLocale = true;

    private long cacheMillis = -1;

    private ClassLoader classLoader;

    private String[] basenames = new String[0];

    /**
     * Cache to hold loaded ResourceBundles.
     * This Map is keyed with the bundle basename, which holds a Map that is
     * keyed with the Locale and in turn holds the ResourceBundle instances.
     * This allows for very efficient hash lookups, significantly faster
     * than the ResourceBundle class's own cache.
     */
    private final Map<String, Map<Locale, ResourceBundle>> cachedResourceBundles = new ConcurrentHashMap<>();

    /**
     * Cache to hold already generated MessageFormats.
     * This Map is keyed with the ResourceBundle, which holds a Map that is
     * keyed with the message code, which in turn holds a Map that is keyed
     * with the Locale and holds the MessageFormat values. This allows for
     * very efficient hash lookups without concatenated keys.
     * @see #getMessageFormat
     */
    private final Map<ResourceBundle, Map<String, Map<Locale, MessageFormat>>> cachedBundleMessageFormats =
            new ConcurrentHashMap<>();

    /**
     * Set the default charset to use for parsing resource bundle files.
     * <p>Default is the {@code java.util.ResourceBundle} default encoding: ISO-8859-1.
     * @param defaultEncoding the default encoding
     */
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Set whether to fall back to the system Locale if no files for a specific
     * Locale have been found. Default is "true"; if this is turned off, the only
     * fallback will be the default file (e.g. "messages.properties" for
     * basename "messages").
     * <p>Falling back to the system Locale is the default behavior of
     * {@code java.util.ResourceBundle}. However, this is often not desirable
     * in an application server environment, where the system Locale is not relevant
     * to the application at all: Set this flag to "false" in such a scenario.
     * @param fallbackToSystemLocale whether fallback to system locale
     */
    public void setFallbackToSystemLocale(boolean fallbackToSystemLocale) {
        this.fallbackToSystemLocale = fallbackToSystemLocale;
    }

    /**
     * Set the number of seconds to cache loaded resource bundle files.
     * <ul>
     * <li>Default is "-1", indicating to cache forever.
     * <li>A positive number will expire resource bundles after the given
     * number of seconds. This is essentially the interval between refresh checks.
     * Note that a refresh attempt will first check the last-modified timestamp
     * of the file before actually reloading it; so if files don't change, this
     * interval can be set rather low, as refresh attempts will not actually reload.
     * <li>A value of "0" will check the last-modified timestamp of the file on
     * every message access. <b>Do not use this in a production environment!</b>
     * <li><b>Note that depending on your ClassLoader, expiration might not work reliably
     * since the ClassLoader may hold on to a cached version of the bundle file.</b>
     * </ul>
     * @param cacheSeconds the cache seconds
     */
    public void setCacheSeconds(int cacheSeconds) {
        this.cacheMillis = (cacheSeconds * 1000L);
    }

    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            return ClassUtils.getDefaultClassLoader();
        } else {
            return classLoader;
        }
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Set a single basename, following {@link java.util.ResourceBundle} conventions:
     * essentially, a fully-qualified classpath location. If it doesn't contain a
     * package qualifier (such as {@code org.mypackage}), it will be resolved
     * from the classpath root.
     * <p>Messages will normally be held in the "/lib" or "/classes" directory of
     * a web application's WAR structure. They can also be held in jar files on
     * the class path.
     * <p>Note that ResourceBundle names are effectively classpath locations: As a
     * consequence, the JDK's standard ResourceBundle treats dots as package separators.
     * This means that "test.theme" is effectively equivalent to "test/theme",
     * just like it is for programmatic {@code java.util.ResourceBundle} usage.
     * @param basename the basename
     * @see #setBasenames #setBasenames
     * @see java.util.ResourceBundle#getBundle(String) java.util.ResourceBundle#getBundle(String)
     */
    public void setBasename(String basename) {
        setBasenames(basename);
    }

    /**
     * Set an array of basenames, each following {@link java.util.ResourceBundle}
     * conventions: essentially, a fully-qualified classpath location. If it
     * doesn't contain a package qualifier (such as {@code org.mypackage}),
     * it will be resolved from the classpath root.
     * <p>The associated resource bundles will be checked sequentially
     * when resolving a message code. Note that message definitions in a
     * <i>previous</i> resource bundle will override ones in a later bundle,
     * due to the sequential lookup.
     * <p>Note that ResourceBundle names are effectively classpath locations: As a
     * consequence, the JDK's standard ResourceBundle treats dots as package separators.
     * This means that "test.theme" is effectively equivalent to "test/theme",
     * just like it is for programmatic {@code java.util.ResourceBundle} usage.
     * @param basenames the basenames
     * @see #setBasename #setBasename
     * @see java.util.ResourceBundle#getBundle(String) java.util.ResourceBundle#getBundle(String)
     */
    public void setBasenames(String... basenames) {
        if (basenames != null) {
            this.basenames = new String[basenames.length];
            for (int i = 0; i < basenames.length; i++) {
                String basename = basenames[i];
                if (!StringUtils.hasText(basename)) {
                    throw new IllegalArgumentException("Basename must not be empty");
                }
                this.basenames[i] = basename.trim();
            }
        } else {
            this.basenames = new String[0];
        }
    }

    /**
     * Resolves the given message code as key in the registered resource bundles,
     * returning the value found in the bundle as-is (without MessageFormat parsing).
     */
    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        String result = null;
        for (int i = 0; result == null && i < this.basenames.length; i++) {
            ResourceBundle bundle = getResourceBundle(this.basenames[i], locale);
            if (bundle != null) {
                result = getStringOrNull(bundle, code);
            }
        }
        return result;
    }

    /**
     * Resolves the given message code as key in the registered resource bundles,
     * using a cached MessageFormat instance per message code.
     */
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        MessageFormat messageFormat = null;
        for (int i = 0; messageFormat == null && i < this.basenames.length; i++) {
            ResourceBundle bundle = getResourceBundle(this.basenames[i], locale);
            if (bundle != null) {
                messageFormat = getMessageFormat(bundle, code, locale);
            }
        }
        return messageFormat;
    }

    /**
     * Return a ResourceBundle for the given basename and code,
     * fetching already generated MessageFormats from the cache.
     * @param basename the basename of the ResourceBundle
     * @param locale the Locale to find the ResourceBundle for
     * @return the resulting ResourceBundle, or {@code null} if none found for the given basename and Locale
     */
    protected ResourceBundle getResourceBundle(String basename, Locale locale) {
        if (this.cacheMillis >= 0) {
            // Fresh ResourceBundle.getBundle call in order to let ResourceBundle
            // do its native caching, at the expense of more extensive lookup steps.
            return doGetBundle(basename, locale);
        } else {
            // Cache forever: prefer locale cache over repeated getBundle calls.
            Map<Locale, ResourceBundle> localeMap = this.cachedResourceBundles.get(basename);
            if (localeMap != null) {
                ResourceBundle bundle = localeMap.get(locale);
                if (bundle != null) {
                    return bundle;
                }
            }
            try {
                ResourceBundle bundle = doGetBundle(basename, locale);
                if (localeMap == null) {
                    localeMap = new ConcurrentHashMap<>();
                    Map<Locale, ResourceBundle> existing = this.cachedResourceBundles.putIfAbsent(basename, localeMap);
                    if (existing != null) {
                        localeMap = existing;
                    }
                }
                localeMap.put(locale, bundle);
                return bundle;
            } catch (MissingResourceException ex) {
                logger.warn("ResourceBundle [" + basename + "] not found for MessageSource: " + ex.getMessage());

                // Assume bundle not found
                // -> do NOT throw the exception to allow for checking parent message source.
                return null;
            }
        }
    }

    /**
     * Obtain the resource bundle for the given basename and Locale.
     * @param basename the basename to look for
     * @param locale the Locale to look for
     * @return the corresponding ResourceBundle
     * @throws MissingResourceException if no matching bundle could be found
     * @see java.util.ResourceBundle#getBundle(String, Locale, ClassLoader) java.util.ResourceBundle#getBundle(String, Locale, ClassLoader)
     */
    protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
        return ResourceBundle.getBundle(basename, locale, getClassLoader(), new MessageSourceControl());
    }

    /**
     * Load a property-based resource bundle from the given reader.
     * <p>The default implementation returns a {@link PropertyResourceBundle}.
     * @param reader the reader for the target resource
     * @return the fully loaded bundle
     * @throws IOException in case of I/O failure
     * @see PropertyResourceBundle#PropertyResourceBundle(Reader) PropertyResourceBundle#PropertyResourceBundle(Reader)
     */
    protected ResourceBundle loadBundle(Reader reader) throws IOException {
        return new PropertyResourceBundle(reader);
    }

    /**
     * Load a property-based resource bundle from the given input stream,
     * picking up the default properties encoding on JDK 9+.
     * <p>This will only be called with {@link #setDefaultEncoding "defaultEncoding"}
     * set to {@code null}, explicitly enforcing the platform default encoding
     * (which is UTF-8 with a ISO-8859-1 fallback on JDK 9+ but configurable
     * through the "java.util.PropertyResourceBundle.encoding" system property).
     * Note that this method can only be called with a {@code ResourceBundle.Control}:
     * When running on the JDK 9+ module path where such control handles are not
     * supported, any overrides in custom subclasses will effectively get ignored.
     * <p>The default implementation returns a {@link PropertyResourceBundle}.
     * @param inputStream the input stream for the target resource
     * @return the fully loaded bundle
     * @throws IOException in case of I/O failure
     * @see #loadBundle(Reader)
     * @see PropertyResourceBundle#PropertyResourceBundle(InputStream)
     */
    protected ResourceBundle loadBundle(InputStream inputStream) throws IOException {
        return new PropertyResourceBundle(inputStream);
    }

    /**
     * Return a MessageFormat for the given bundle and code,
     * fetching already generated MessageFormats from the cache.
     * @param bundle the ResourceBundle to work on
     * @param code the message code to retrieve
     * @param locale the Locale to use to build the MessageFormat
     * @return the resulting MessageFormat, or {@code null} if no message defined for the given code
     * @throws MissingResourceException if thrown by the ResourceBundle
     */
    protected MessageFormat getMessageFormat(ResourceBundle bundle, String code, Locale locale)
            throws MissingResourceException {
        Map<String, Map<Locale, MessageFormat>> codeMap = this.cachedBundleMessageFormats.get(bundle);
        Map<Locale, MessageFormat> localeMap = null;
        if (codeMap != null) {
            localeMap = codeMap.get(code);
            if (localeMap != null) {
                MessageFormat result = localeMap.get(locale);
                if (result != null) {
                    return result;
                }
            }
        }

        String msg = getStringOrNull(bundle, code);
        if (msg != null) {
            if (codeMap == null) {
                codeMap = new ConcurrentHashMap<>();
                Map<String, Map<Locale, MessageFormat>> existing =
                        this.cachedBundleMessageFormats.putIfAbsent(bundle, codeMap);
                if (existing != null) {
                    codeMap = existing;
                }
            }
            if (localeMap == null) {
                localeMap = new ConcurrentHashMap<>();
                Map<Locale, MessageFormat> existing = codeMap.putIfAbsent(code, localeMap);
                if (existing != null) {
                    localeMap = existing;
                }
            }
            MessageFormat result = createMessageFormat(msg, locale);
            localeMap.put(locale, result);
            return result;
        }
        return null;
    }

    /**
     * Efficiently retrieve the String value for the specified key,
     * or return {@code null} if not found.
     * @param bundle the ResourceBundle to perform the lookup in
     * @param key the key to look up
     * @return the associated value, or {@code null} if none
     * @see ResourceBundle#getString(String) ResourceBundle#getString(String)
     * @see ResourceBundle#containsKey(String) ResourceBundle#containsKey(String)
     */
    protected String getStringOrNull(ResourceBundle bundle, String key) {
        if (bundle.containsKey(key)) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException ex) {
                // Assume key not found for some other reason
                // -> do NOT throw the exception to allow for checking parent message source.
            }
        }
        return null;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(getClass().getName());
        tsb.append("basenames", this.basenames);
        return tsb.toString();
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
            if (format.equals("java.properties")) {
                String bundleName = toBundleName(baseName, locale);
                final String resourceName = toResourceName(bundleName, "properties");
                InputStream inputStream = null;
                if (reload) {
                    URL url = loader.getResource(resourceName);
                    if (url != null) {
                        URLConnection connection = url.openConnection();
                        if (connection != null) {
                            connection.setUseCaches(false);
                            inputStream = connection.getInputStream();
                        }
                    }
                } else {
                    inputStream = loader.getResourceAsStream(resourceName);
                }
                if (inputStream != null) {
                    if (defaultEncoding != null) {
                        try (InputStreamReader bundleReader = new InputStreamReader(inputStream, defaultEncoding)) {
                            return loadBundle(bundleReader);
                        }
                    } else {
                        try (InputStream bundleStream = inputStream) {
                            return loadBundle(bundleStream);
                        }
                    }
                } else {
                    return null;
                }
            } else {
                // Delegate handling of "java.class" format to standard Control
                return super.newBundle(baseName, locale, format, loader, reload);
            }
        }

        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            return (fallbackToSystemLocale ? super.getFallbackLocale(baseName, locale) : null);
        }

        @Override
        public long getTimeToLive(String baseName, Locale locale) {
            return (cacheMillis >= 0 ? cacheMillis : super.getTimeToLive(baseName, locale));
        }

        @Override
        public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime) {
            if (super.needsReload(baseName, locale, format, loader, bundle, loadTime)) {
                cachedBundleMessageFormats.remove(bundle);
                return true;
            } else {
                return false;
            }
        }
    }

}
