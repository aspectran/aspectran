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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.loader.DelegatingLoader;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Factory that configures a FreeMarker Configuration.
 *
 * <p>Created: 2016. 1. 9.</p>
 */
public class PebbleEngineFactory implements ApplicationAdapterAware {

    private final Log log = LogFactory.getLog(PebbleEngineFactory.class);

    private ApplicationAdapter applicationAdapter;

    private Locale defaultLocale;

    private boolean strictVariables;

    private String[] templateLoaderPaths;

    private Loader<?>[] templateLoaders;

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = StringUtils.deduceLocale(defaultLocale);
    }

    public void setStrictVariables(boolean strictVariables) {
        this.strictVariables = strictVariables;
    }

    public void setTemplateLoaderPath(String templateLoaderPath) throws IOException {
        this.templateLoaderPaths = new String[] { templateLoaderPath };
    }

    public void setTemplateLoaderPath(String[] templateLoaderPaths) {
        this.templateLoaderPaths = templateLoaderPaths;
    }

    public void setTemplateLoaderPath(List<String> templateLoaderPathList) {
        this.templateLoaderPaths = templateLoaderPathList.toArray(new String[templateLoaderPathList.size()]);
    }

    public void setTemplateLoader(Loader<?> templateLoaders) {
        this.templateLoaders = new Loader<?>[] {templateLoaders};
    }

    public void setTemplateLoader(Loader<?>[] templateLoaders) {
        this.templateLoaders = templateLoaders;
    }

    public void setTemplateLoader(List<Loader<?>> templateLoaderList) {
        this.templateLoaders = templateLoaderList.toArray(new Loader<?>[templateLoaderList.size()]);
    }

    public PebbleEngine createPebbleEngine() throws IOException {

        PebbleEngine.Builder builder = new PebbleEngine.Builder();
        builder.strictVariables(strictVariables);

        if(defaultLocale != null) {
            builder.defaultLocale(defaultLocale);
        }

        if(templateLoaders == null) {
            if(templateLoaderPaths != null && templateLoaderPaths.length > 0) {
                List<Loader<?>> templateLoaderList = new ArrayList<Loader<?>>();
                for(String path : templateLoaderPaths) {
                    templateLoaderList.add(getTemplateLoaderForPath(path));
                }
                setTemplateLoader(templateLoaderList);
            }
        }

        if(templateLoaders != null) {
            Loader<?> templateLoader = getAggregateTemplateLoader(templateLoaders);
            if(templateLoader != null) {
                builder.loader(templateLoader);
            }
        }

        return builder.build();
    }

    /**
     * Return a Template Loader based on the given Template Loader list.
     * If more than one TemplateLoader has been registered, a FreeMarker
     * MultiTemplateLoader needs to be created.
     * @param templateLoaders the final List of TemplateLoader instances
     * @return the aggregate TemplateLoader
     */
    protected Loader<?> getAggregateTemplateLoader(Loader<?>[] templateLoaders) {
        int loaderCount = templateLoaders.length;
        switch(loaderCount) {
            case 0:
                //log.info("No FreeMarker TemplateLoaders specified.");
                return null;
            case 1:
                return templateLoaders[0];
            default:
                List<Loader<?>> defaultLoadingStrategies = new ArrayList<Loader<?>>();
                for(Loader<?> loader : templateLoaders) {
                    defaultLoadingStrategies.add(loader);
                }
                return new DelegatingLoader(defaultLoadingStrategies);
        }
    }

    /**
     * Determine a Pebble Engine Template Loader for the given path.
     * @param templateLoaderPath the path to load templates from
     * @return an appropriate Template Loader
     */
    protected Loader<?> getTemplateLoaderForPath(String templateLoaderPath) throws IOException {
        if(templateLoaderPath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            String basePackagePath = templateLoaderPath.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
            if(log.isDebugEnabled()) {
                log.debug("Template loader path [" + templateLoaderPath + "] resolved to class path [" + basePackagePath + "]");
            }
            return new ClasspathLoader(applicationAdapter.getClassLoader());
        } else if(templateLoaderPath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            File file = new File(templateLoaderPath.substring(ResourceUtils.FILE_URL_PREFIX.length()));
            String prefix = file.getAbsolutePath();
            if(log.isDebugEnabled()) {
                log.debug("Template loader path [" + templateLoaderPath + "] resolved to file path [" + prefix + "]");
            }
            FileLoader fileLoader = new FileLoader();
            fileLoader.setPrefix(prefix);
            return fileLoader;
        } else {
            File file = new File(applicationAdapter.getApplicationBasePath(), templateLoaderPath);
            String prefix = file.getAbsolutePath();
            if(log.isDebugEnabled()) {
                log.debug("Template loader path [" + templateLoaderPath + "] resolved to file path [" + prefix + "]");
            }
            FileLoader fileLoader = new FileLoader();
            fileLoader.setPrefix(prefix);
            return fileLoader;
        }
    }

}
