package com.aspectran.core.component.bean.aware;

import com.aspectran.core.activity.Activity;

/**
 * Interface to be implemented by any object that wishes to be notified of the
 * current {@link Activity} that it runs in.
 *
 * <p>Created: 2017. 10. 22.</p>
 *
 * @since 5.0.0
 */
public interface CurrentActivityAware extends Aware {

    void setCurrentActivity(Activity activity);

}
