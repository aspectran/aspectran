package com.aspectran.jpa.common.eclipselink;

import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Profile;
import com.aspectran.core.component.bean.annotation.Qualifier;

import javax.sql.DataSource;

@Component
@Profile("eclipselink")
@Bean(id = "readOnlyEntityManagerFactory", lazyDestroy = true)
public class ReadOnlyEclipseLinkEntityManagerFactory extends EclipseLinkEntityManagerFactory {

    @Autowired
    public ReadOnlyEclipseLinkEntityManagerFactory(@Qualifier("readOnlyDataSource") DataSource dataSource) {
        super(dataSource);
    }

}
