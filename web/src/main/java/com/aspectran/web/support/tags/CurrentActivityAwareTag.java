/*
 * Copyright (c) 2008-2021 The Aspectran Project
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
package com.aspectran.web.support.tags;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.web.service.WebServiceHolder;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.TagSupport;
import jakarta.servlet.jsp.tagext.TryCatchFinally;

/**
 * Superclass for all tags that require a {@link Activity}.
 *
 * <p>Created: 2020/05/31</p>
 */
public abstract class CurrentActivityAwareTag extends TagSupport implements TryCatchFinally {

    /** Logger available to subclasses. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Nullable
    private Activity currentActivity;

    /**
     * Create and expose the current RequestContext.
     * Delegates to {@link #doStartTagInternal()} for actual work.
     */
    @Override
    public final int doStartTag() throws JspException {
        try {
            ActivityContext context = WebServiceHolder.getCurrentActivityContext();
            if (context == null) {
                throw new IllegalStateException("Failed to find WebService");
            }
            this.currentActivity = context.getCurrentActivity();
            return doStartTagInternal();
        } catch (JspException | RuntimeException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new JspTagException(ex.getMessage());
        }
    }

    /**
     * Return the current Activity.
     */
    protected final Activity getCurrentActivity() {
        Assert.state(this.currentActivity != null, "No current activity injected");
        return this.currentActivity;
    }

    /**
     * Called by doStartTag to perform the actual work.
     * @return same as TagSupport.doStartTag
     * @throws Exception any exception, any checked one other than
     *      a JspException gets wrapped in a JspException by doStartTag
     * @see jakarta.servlet.jsp.tagext.TagSupport#doStartTag
     */
    protected abstract int doStartTagInternal() throws Exception;

    @Override
    public void doCatch(Throwable throwable) throws Throwable {
        throw throwable;
    }

    @Override
    public void doFinally() {
        this.currentActivity = null;
    }

}
