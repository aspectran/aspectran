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
/**
 * Redis session store support based on Lettuce for Master-Replica (primary/replica) topologies.
 * <p>
 * Provides connection pooling and a session store implementation that connects to an
 * upstream node for writes while allowing replica reads via Lettuce.
 * </p>
 */
package com.aspectran.core.component.session.redis.lettuce.primaryreplica;
