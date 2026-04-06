/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.core.activity;

import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.apon.DefaultParameters;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

/**
 * A specialized container for conveying metadata "hints" from business logic
 * (such as Services or DAOs) to underlying framework modules or external systems.
 *
 * <p>This class extends {@link DefaultParameters} to provide a flexible,
 * APON-based structure for passing intent without creating tight coupling
 * between the core and sub-modules. Hints are typically propagated through
 * the activity stack and consumed by intelligent agents (e.g., MyBatis or
 * JPA agents) to determine execution behavior like transaction settings or
 * caching strategies.</p>
 *
 * <p>Created: 2026. 4. 3.</p>
 */
public class HintParameters extends DefaultParameters implements Serializable {

    @Serial
    private static final long serialVersionUID = -8375106503003850170L;

    private final String type;

    private final boolean propagated;

    /**
     * Constructs a new HintParameters with the specified type.
     * @param type the category or type of the hint (e.g., "transactional", "cache")
     */
    public HintParameters(String type) {
        this(type, true);
    }

    /**
     * Constructs a new HintParameters with the specified type and propagation control.
     * @param type the category or type of the hint
     * @param propagated whether the hint should be propagated to child calls
     */
    public HintParameters(String type, boolean propagated) {
        super();
        this.type = type;
        this.propagated = propagated;
    }

    /**
     * Constructs a new HintParameters with the specified type and initial metadata.
     * @param type the category or type of the hint
     * @param apon the APON-formatted string containing the hint metadata
     * @throws IOException if the APON string is malformed or cannot be parsed
     */
    public HintParameters(String type, String apon) throws IOException {
        this(type, apon, true);
    }

    /**
     * Constructs a new HintParameters with the specified type, initial metadata,
     * and propagation control.
     * @param type the category or type of the hint
     * @param apon the APON-formatted string containing the hint metadata
     * @param propagated whether the hint should be propagated to child calls
     * @throws IOException if the APON string is malformed or cannot be parsed
     */
    public HintParameters(String type, String apon, boolean propagated) throws IOException {
        this(type, propagated);
        readFrom(apon);
    }

    /**
     * Returns the type of this hint, which identifies its intended consumer or category.
     * @return the hint type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns whether the hint should be propagated to child method calls.
     * @return {@code true} if the hint should be propagated; {@code false} otherwise
     */
    public boolean isPropagated() {
        return propagated;
    }

    @Override
    public String toString() {
        return ToStringBuilder.toString("Hint [" + type + "]", this);
    }

}
