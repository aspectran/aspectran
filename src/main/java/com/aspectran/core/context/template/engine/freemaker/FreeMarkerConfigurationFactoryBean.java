package com.aspectran.core.context.template.engine.freemaker;

import com.aspectran.core.context.bean.ablility.FactoryBean;
import com.aspectran.core.context.bean.ablility.InitializableBean;
import freemarker.template.Configuration;

/**
 * JavaBean to configure FreeMarker.
 *
 * Note: Aspectran's FreeMarker support requires FreeMarker 2.3 or higher.
 *
 * Created by gulendol on 2016. 1. 9..
 */
public class FreeMarkerConfigurationFactoryBean extends FreeMarkerConfigurationFactory implements InitializableBean, FactoryBean<Configuration> {

    private Configuration configuration;

    /**
     * Initialize FreeMarkerConfigurationFactory's Configuration
     * if not overridden by a preconfigured FreeMarker Configuation.
     *
     * @throws Exception
     */
    @Override
    public void initialize() throws Exception {
        if(this.configuration == null) {
            this.configuration = createConfiguration();
        }
    }

    @Override
    public Configuration getObject() {
        return this.configuration;
    }

}
