package com.aspectran.jetty;

import com.aspectran.core.component.bean.annotation.After;
import com.aspectran.core.component.bean.annotation.Aspect;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Before;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Joinpoint;
import com.aspectran.core.component.bean.annotation.Scope;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Bean
@Scope(ScopeType.SESSION)
@Aspect("perSessionWebRequestCounter")
@Joinpoint(
        pointcut = {
                "+: /**"
        }
)
public class PerSessionWebRequestCounter implements Serializable {

    @Serial
    private static final long serialVersionUID = -1887943450985496469L;

    private static final Logger logger = LoggerFactory.getLogger(PerSessionWebRequestCounter.class);

    private final AtomicInteger requests = new AtomicInteger();

    private final AtomicLong startTime = new AtomicLong();

    private final AtomicLong stopTime = new AtomicLong();

    @Before
    public void before() {
        requests.incrementAndGet();
        startTime.set(System.currentTimeMillis());
    }

    @After
    public void after(PerSessionWebRequestCounter counter) {
        stopTime.set(System.currentTimeMillis());

        if (logger.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder(ObjectUtils.simpleIdentityToString(this));
            tsb.append("requests", counter.getRequests());
            tsb.append("start", counter.getStartTime());
            tsb.append("stop", counter.getStopTime());
            tsb.append("duration", counter.getStopTime() - counter.getStartTime());
            logger.debug(tsb.toString());
        }
    }

    public int getRequests() {
        return requests.get();
    }

    public long getStartTime() {
        return startTime.get();
    }

    public long getStopTime() {
        return stopTime.get();
    }

}
