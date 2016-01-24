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
package com.aspectran.core.context.template.engine.pebble;

import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.loader.DelegatingLoader;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Factory that configures a FreeMarker Configuration.
 *
 * <p>Created: 2016. 1. 9.</p>
 */
public class PebbleEngineFactory {

    protected final Log logger = LogFactory.getLog(getClass());

    private Locale defaultLocale;

    private boolean strictVariables;

    private String templateNamePrefix;

    private String templateNameSuffix;

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = StringUtils.deduceLocale(defaultLocale);
    }

    public void setStrictVariables(boolean strictVariables) {
        this.strictVariables = strictVariables;
    }

    public void setTemplateNamePrefix(String templateNamePrefix) {
        this.templateNamePrefix = templateNamePrefix;
    }

    public void setTemplateNameSuffix(String templateNameSuffix) {
        this.templateNameSuffix = templateNameSuffix;
    }

    public PebbleEngine createPebbleEngine() {
        return createPebbleEngine(null);
    }

    public PebbleEngine createPebbleEngine(ClassLoader classLoader) {
        List<Loader<?>> defaultLoadingStrategies = new ArrayList<Loader<?>>();
        ClasspathLoader classpathLoader;
        if(classLoader == null)
            classpathLoader = new ClasspathLoader();
        else
            classpathLoader = new ClasspathLoader(classLoader);
        defaultLoadingStrategies.add(classpathLoader);
        defaultLoadingStrategies.add(new FileLoader());

        Loader<?> loader = new DelegatingLoader(defaultLoadingStrategies);
        loader.setPrefix(templateNamePrefix);
        loader.setSuffix(templateNameSuffix);

        PebbleEngine.Builder builder = new PebbleEngine.Builder();
        builder.loader(loader);
        builder.strictVariables(strictVariables);

        if(defaultLocale != null)
            builder.defaultLocale(defaultLocale);

        return builder.build();
    }

}
