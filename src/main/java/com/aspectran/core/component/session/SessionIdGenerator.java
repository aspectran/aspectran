/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.component.session;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * <p>Created: 2017. 6. 12.</p>
 */
class SessionIdGenerator {

    private static final Log log = LogFactory.getLog(SessionIdGenerator.class);

    private static final AtomicLong counter = new AtomicLong();

    private String serviceName;

    private Random random;

    private boolean weakRandom;

    SessionIdGenerator(String serviceName) {
        this.serviceName = serviceName;
        initRandom();
    }

    /**
     * Returns a new unique session id.
     *
     * @param seedTerm the seed for RNG
     * @return a new unique session id
     */
    String newSessionId(long seedTerm) {
        synchronized (random) {
            long r0;
            if (weakRandom) {
                r0 = hashCode() ^ Runtime.getRuntime().freeMemory() ^ random.nextInt() ^ (seedTerm << 32);
            } else {
                r0 = random.nextLong();
            }
            if (r0 < 0) {
                r0 = -r0;
            }

            long r1;
            if (weakRandom) {
                r1 = hashCode() ^ Runtime.getRuntime().freeMemory() ^ random.nextInt() ^ (seedTerm << 32);
            } else {
                r1 = random.nextLong();
            }
            if (r1 < 0) {
                r1 = -r1;
            }

            StringBuilder id = new StringBuilder();
            if (!StringUtils.isEmpty(serviceName)) {
                id.append(serviceName);
            }
            id.append(Long.toString(r0,36));
            id.append(Long.toString(r1,36));
            id.append(Long.toString(counter.getAndIncrement()));
            return id.toString();
        }
    }

    /**
     * Set up a random number generator for the sessionids.
     *
     * By preference, use a SecureRandom but allow to be injected.
     */
    private void initRandom() {
        try {
            random = new SecureRandom();
        } catch (Exception e) {
            log.warn("Could not generate SecureRandom for session-id randomness", e);
            random = new Random();
            weakRandom = true;
        }
    }

}
