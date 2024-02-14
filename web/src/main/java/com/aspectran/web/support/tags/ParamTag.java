package com.aspectran.web.support.tags;

import com.aspectran.utils.annotation.jsr305.Nullable;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import java.io.Serial;

/**
 * The {@code <param>} tag collects name-value parameters and passes them to a
 * {@link ParamAware} ancestor in the tag hierarchy.
 *
 * <p>This tag must be nested under a param aware tag.
 *
 * <table>
 * <caption>Attribute Summary</caption>
 * <thead>
 * <tr>
 * <th>Attribute</th>
 * <th>Required?</th>
 * <th>Runtime Expression?</th>
 * <th>Description</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>name</td>
 * <td>true</td>
 * <td>true</td>
 * <td>The name of the parameter.</td>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>false</td>
 * <td>true</td>
 * <td>The value of the parameter.</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * @see Param
 * @see UrlTag
 */
public class ParamTag extends BodyTagSupport {

    @Serial
    private static final long serialVersionUID = 2318398257284641244L;

    private String name = "";

    @Nullable
    private String value;

    private boolean valueSet;

    /**
     * Set the name of the parameter (required).
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the value of the parameter (optional).
     */
    public void setValue(String value) {
        this.value = value;
        this.valueSet = true;
    }

    @Override
    public int doEndTag() throws JspException {
        Param param = new Param();
        param.setName(this.name);
        if (this.valueSet) {
            param.setValue(this.value);
        } else if (getBodyContent() != null) {
            // Get the value from the tag body
            param.setValue(getBodyContent().getString().trim());
        }

        // Find a param aware ancestor
        ParamAware paramAwareTag = (ParamAware)findAncestorWithClass(this, ParamAware.class);
        if (paramAwareTag == null) {
            throw new JspException("The param tag must be a descendant of a tag that supports parameters");
        }

        paramAwareTag.addParam(param);

        return EVAL_PAGE;
    }

    @Override
    public void release() {
        super.release();
        this.name = "";
        this.value = null;
        this.valueSet = false;
    }

}
