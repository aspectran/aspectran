package com.aspectran.core.context;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.AbstractDiscriminator;
import ch.qos.logback.core.util.OptionHelper;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.utils.StringUtils;

/**
 * Discriminates logging events based on the name given to the ActivityContext of the current CoreService.
 * <p>ex)
 * <pre>
 *    &lt;appender name="SIFT" class="ch.qos.logback.classic.sift.SiftingAppender"&gt;
 *       &lt;discriminator class="com.aspectran.core.context.ContextBasedLoggerDiscriminator"&gt;
 *         &lt;key>LOGGER_NAME&lt;/key&gt;
 *         &lt;defaultValue&gt;app&lt;/defaultValue&gt;
 *       &lt;/discriminator&gt;
 *       &lt;sift&gt;
 *         &lt;appender name="FILE-${LOGGER_NAME}" class="ch.qos.logback.core.rolling.RollingFileAppender"&gt;
 *           &lt;file&gt;${aspectran.basePath:-app}/logs/${LOGGER_NAME}.log&lt;/file&gt;
 *           ...
 *         &lt;/appender&gt;
 *       &lt;/sift&gt;
 *   &lt;/appender&gt;
 * </pre></p>
 * @see <a href="https://logback.qos.ch/manual/loggingSeparation.html">Logging separation</a>
 */
public class ContextBasedLoggerDiscriminator extends AbstractDiscriminator<ILoggingEvent> {

    private String key;

    private String defaultValue;

    /**
     * Returns the name of the current CoreService's ActivityContext.
     * If that value is null, then return the value assigned to the DefaultValue
     * property.
     */
    @Override
    public String getDiscriminatingValue(ILoggingEvent event) {
        CoreService service = CoreServiceHolder.acquire();
        if (service == null) {
            return defaultValue;
        }
        String value = (service.getActivityContext() != null ? service.getActivityContext().getName() : null);
        return (value != null ? value : defaultValue);
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void start() {
        int errors = 0;
        if (!StringUtils.hasText(key)) {
            errors++;
            addError("The 'key' property must be set");
        }
        if (!StringUtils.hasText(defaultValue)) {
            errors++;
            addError("The 'defaultValue' property must be set");
        }
        if (errors == 0) {
            super.start();
        }
    }

}
