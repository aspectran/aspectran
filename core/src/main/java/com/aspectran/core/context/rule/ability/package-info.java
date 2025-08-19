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
 * Contains a collection of interfaces that define common capabilities or "abilities"
 * for rule classes.
 *
 * <p>This package provides a set of contracts that various rule classes can implement
 * to indicate that they support certain features. This allows the framework to treat
 * different types of rules polymorphically based on the abilities they possess.
 * Key abilities include:
 * <ul>
 *   <li>Applying action rules ({@link com.aspectran.core.context.rule.ability.ActionRuleApplicable})</li>
 *   <li>Applying response rules ({@link com.aspectran.core.context.rule.ability.ResponseRuleApplicable})</li>
 *   <li>Referencing beans ({@link com.aspectran.core.context.rule.ability.BeanReferenceable})</li>
 *   <li>Cloning or replicating themselves ({@link com.aspectran.core.context.rule.ability.Replicable})</li>
 * </ul>
 * By implementing these interfaces, rule classes can participate in different parts of the
 * application context lifecycle, from parsing and validation to runtime execution.
 */
package com.aspectran.core.context.rule.ability;
