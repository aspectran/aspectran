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
package com.aspectran.mybatis;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.bean.ablility.InitializableBean;

public class DefaultSqlSessionAgent extends AbstractSqlSessionProvider implements InitializableBean {

    private final String txAspectId;

    /**
     * Instantiates a new SqlSessionAgent.
     * @param txAspectId the ID of the aspect that provides the SqlSessionAdvice
     */
    public DefaultSqlSessionAgent(String txAspectId) {
        if (txAspectId == null) {
            throw new IllegalArgumentException("txAspectId must not be null");
        }
        this.txAspectId = txAspectId;
    }

    @Override
    public SqlSessionAdvice getSqlSessionAdvice() {
        Activity currentActivity = getAvailableActivity();
        checkTransactional(currentActivity.getMode());
        SqlSessionAdvice sqlSessionAdvice = currentActivity.getAvailableAdvice(txAspectId);
        if (sqlSessionAdvice == null) {
            if (getAspectRuleRegistry().getAspectRule(txAspectId) == null) {
                throw new IllegalArgumentException("Aspect '" + txAspectId +
                        "' handling SqlSessionAdvice is not registered");
            }
            throw new IllegalStateException("No transactional context found for the current activity; " +
                    "ensure the activity is advised by aspect '" + txAspectId + "'");
        }
        return sqlSessionAdvice;
    }

    /**
     * Initializes the provider. If the aspect specified by {@code txAspectId}
     * is not already registered in the aspect rule registry, this method
     * automatically creates and registers a new {@link SqlSessionAdvice} aspect
     * using the current configuration.
     */
    @Override
    public void initialize() {
        if (!getAspectRuleRegistry().contains(txAspectId)) {
            SqlSessionAdviceRegister register = new SqlSessionAdviceRegister(getActivityContext());
            register.setTxAspectId(txAspectId);
            register.setSqlSessionFactoryBeanId(getSqlSessionFactoryBeanId());
            register.setTargetBeanId(getTargetBeanId());
            register.setTargetBeanClass(getTargetBeanClass());
            register.setExecutorType(getExecutorType());
            register.setIsolationLevel(getIsolationLevel());
            register.setAutoCommit(isAutoCommit());
            register.setReadOnly(isReadOnly());
            register.setReadOnlyRollbackOnClose(isReadOnlyRollbackOnClose());
            register.register();
        }
    }

}
