package com.aspectran.core.support;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.aware.CurrentActivityAware;

/**
 * {@code CurrentTransletFactoryBean} that returns the translet for the current request.
 * It should be declared as a {@code request} or {@code prototype} bean because it is intended
 * to use the value that the current Translet has.
 *
 * <p>Created: 2017. 10. 22.</p>
 */
public class CurrentTransletFactoryBean implements CurrentActivityAware, FactoryBean<Translet> {

    private Translet translet;

    @Override
    public void setCurrentActivity(Activity activity) {
        translet = activity.getTranslet();
    }

    @Override
    public Translet getObject() throws Exception {
        return translet;
    }

}
