package com.aspectran.core.context.message;

/**
 * <p>Created: 2016. 2. 8.</p>
 */
public class AbstractMessageSource {

    private final ClassLoader classLoader;

    private final String defaultEncoding;

    /**
     * Instantiates a new Resource bundle message source.
     *
     * @param classLoader the <code>ClassLoader</code> to use to load the bundle
     * @param defaultEncoding the default charset
     */
    public AbstractMessageSource(ClassLoader classLoader, String defaultEncoding) {
        this.classLoader = classLoader;
        this.defaultEncoding = defaultEncoding;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public String getDefaultEncoding() {
        return defaultEncoding;
    }

}
