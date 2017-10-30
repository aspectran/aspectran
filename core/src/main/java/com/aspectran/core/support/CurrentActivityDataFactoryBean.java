package com.aspectran.core.support;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityDataMap;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.aware.CurrentActivityAware;

/**
 * {@code CurrentActivityDataFactoryBean} that returns the {@code ActivityDataMap} for the current request.
 * It should be declared as a {@code request} or {@code prototype} bean because it is intended
 * to use the value that the current Translet has.
 *
 * <p>Created: 2017. 10. 24.</p>
 */
public class CurrentActivityDataFactoryBean implements CurrentActivityAware, FactoryBean<ActivityDataMap> {

    private String attributeName;

    private Translet translet;

    /**
     * Returns whether the current {@code ActivityDataMap} is registered as an attribute
     * in the request scope.
     *
     * @return true if the current {@code ActivityDataMap} is registered as an attribute
     *      in the request scope; otherwise false
     */
    public boolean isAttributable() {
        return (attributeName != null);
    }

    /**
     * Returns the attribute name of the current {@code ActivityDataMap} specified to register
     * in the request scope.
     *
     * @return the attribute name
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Specifies the attribute name for registering the current {@code ActivityDataMap} as an attribute
     * in the request scope.
     *
     * @param attributeName the attribute name of the current {@code ActivityDataMap} to be registered
     *                      in the request scope.
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public void setCurrentActivity(Activity activity) {
        translet = activity.getTranslet();

        if (attributeName != null) {
            translet.setAttribute(attributeName, translet.getActivityDataMap());
        }
    }

    @Override
    public ActivityDataMap getObject() throws Exception {
        return translet.getActivityDataMap();
    }

}
