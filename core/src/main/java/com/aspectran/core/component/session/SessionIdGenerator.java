/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The Session ID Generator.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public class SessionIdGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SessionIdGenerator.class);

    private static final AtomicLong COUNTER = new AtomicLong();

    private final String workerName;

    private final Random random;

    private boolean weakRandom;

    public SessionIdGenerator() {
        this(null);
    }

    public SessionIdGenerator(String workerName) {
        if (workerName != null && workerName.contains(".")) {
            throw new IllegalArgumentException("Worker name cannot contain '.'");
        }
        this.workerName = workerName;
        this.random = initRandom();
    }

    /**
     * Returns a new unique session id.
     * @param seedTerm the seed for RNG
     * @return a new unique session id
     */
    public String createSessionId(long seedTerm) {
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
            id.append(Long.toString(r0, Character.MAX_RADIX));
            id.append(Long.toString(r1, Character.MAX_RADIX));
            id.append(COUNTER.getAndIncrement());
            if (workerName != null) {
                id.append(".").append(workerName);
            }
            return id.toString();
        }
    }

    /**
     * Set up a random number generator for the session ids.
     * By preference, use a SecureRandom but allow to be injected.
     */
    @NonNull
    private Random initRandom() {
        try {
            return new SecureRandom();
        } catch (Exception e) {
            logger.warn("Could not generate SecureRandom for session-id randomness", e);
            weakRandom = true;
            return new Random();
        }
    }

}
