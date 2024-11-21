package com.aspectran.core.activity.response.dispatch;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

public abstract class AbstractViewDispatcher implements ViewDispatcher {

    private String contentType;

    private String prefix;

    private String suffix;

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    protected String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix for the template name.
     * @param prefix the new prefix for the template name
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    protected String getSuffix() {
        return suffix;
    }

    /**
     * Sets the suffix for the template name.
     * @param suffix the new suffix for the template name
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @NonNull
    protected String resolveViewName(@NonNull DispatchRule dispatchRule, Activity activity) {
        if (dispatchRule.getName() == null) {
            throw new IllegalArgumentException("Dispatch rule must have a name");
        }
        String viewName = dispatchRule.getName(activity);
        if (viewName == null) {
            throw new IllegalArgumentException("Cannot resolve view name");
        }
        if (prefix != null && suffix != null) {
            viewName = prefix + viewName + suffix;
        } else if (prefix != null) {
            viewName = prefix + viewName;
        } else if (suffix != null) {
            viewName = viewName + suffix;
        }
        return viewName;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", super.toString());
        tsb.append("defaultContentType", contentType);
        tsb.append("prefix", prefix);
        tsb.append("suffix", suffix);
        return tsb.toString();
    }

}
