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
package com.aspectran.mybatis.routing;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.mybatis.AbstractSqlSessionProvider;
import com.aspectran.mybatis.SqlSessionAdvice;
import com.aspectran.mybatis.SqlSessionAdviceRegister;
import com.aspectran.utils.Assert;
import org.apache.ibatis.session.SqlSession;

/**
 * Advanced {@link SqlSession} agent that routes operations between read-write
 * and read-only aspects based on the method name patterns.
 *
 * <p>This implementation allows for optimized database access by directing
 * read-only operations to a separate transactional context, which can be
 * configured differently (e.g., directed to a read-replica database).</p>
 *
 * <p>Created: 2026. 4. 5.</p>
 */
public class RoutingSqlSessionAgent extends AbstractSqlSessionProvider implements InitializableBean {

    /** Method name patterns that are treated as read-only by default. */
    private static final String[] DEFAULT_READONLY_METHOD_PATTERNS = { "select*" };

    /** Method name patterns for management that do not require transactional advice. */
    private static final String[] MANAGEMENT_METHOD_PATTERNS = {
            "getMapper",
            "getConfiguration",
            "getConnection",
            "commit",
            "rollback",
            "close",
            "flushStatements",
            "clearCache"
    };

    private final String primaryAspectId;

    private final String replicaAspectId;

    /**
     * Instantiates a new RoutingSqlSessionAgent.
     * @param primaryAspectId the ID for the primary aspect rule
     * @param replicaAspectId the ID for the replica aspect rule
     */
    public RoutingSqlSessionAgent(String primaryAspectId, String replicaAspectId) {
        Assert.notNull(primaryAspectId, "primaryAspectId must not be null");
        Assert.notNull(replicaAspectId, "replicaAspectId must not be null");
        this.primaryAspectId = primaryAspectId;
        this.replicaAspectId = replicaAspectId;
    }

    /**
     * Retrieves the {@link SqlSessionAdvice} from the current activity context
     * by checking for a primary advice first, then falling back to a replica advice.
     * @return the SqlSessionAdvice found in the current activity
     */
    @Override
    public SqlSessionAdvice getSqlSessionAdvice() {
        Activity currentActivity = getAvailableActivity();
        checkTransactional(currentActivity);

        SqlSessionAdvice primaryAdvice = currentActivity.getAvailableAdvice(primaryAspectId);
        if (primaryAdvice != null && primaryAdvice.isOpen()) {
            return primaryAdvice;
        }

        SqlSessionAdvice replicaAdvice = currentActivity.getAvailableAdvice(replicaAspectId);
        if (replicaAdvice != null && replicaAdvice.isOpen()) {
            return replicaAdvice;
        }

        SqlSessionAdvice sqlSessionAdvice = (primaryAdvice != null ? primaryAdvice : replicaAdvice);
        if (sqlSessionAdvice == null) {
            throw new IllegalStateException("No transactional context found for the current activity; " +
                    "ensure the activity is advised by aspect '" + primaryAspectId + "' or '" + replicaAspectId + "'");
        }
        return sqlSessionAdvice;
    }

    /**
     * Initializes the provider. If the aspect specified by {@code primaryAspectId}
     * is not already registered in the aspect rule registry, this method
     * automatically creates and registers a new {@link SqlSessionAdvice} aspect
     * using the current configuration.
     */
    @Override
    public void initialize() {
        if (!getAspectRuleRegistry().contains(primaryAspectId)) {
            SqlSessionAdviceRegister register = new SqlSessionAdviceRegister(getActivityContext());
            register.setTxAspectId(primaryAspectId);
            register.setSqlSessionFactoryBeanId(getSqlSessionFactoryBeanId());
            register.setTargetBeanId(getTargetBeanId());
            register.setTargetBeanClass(getTargetBeanClass());

            String[] excludeMethodNamePatterns = new String[DEFAULT_READONLY_METHOD_PATTERNS.length + MANAGEMENT_METHOD_PATTERNS.length];
            System.arraycopy(DEFAULT_READONLY_METHOD_PATTERNS, 0, excludeMethodNamePatterns, 0, DEFAULT_READONLY_METHOD_PATTERNS.length);
            System.arraycopy(MANAGEMENT_METHOD_PATTERNS, 0, excludeMethodNamePatterns, DEFAULT_READONLY_METHOD_PATTERNS.length, MANAGEMENT_METHOD_PATTERNS.length);
            register.setExcludeMethodNamePatterns(excludeMethodNamePatterns);

            register.setExecutorType(getExecutorType());
            register.setIsolationLevel(getIsolationLevel());
            register.setAutoCommit(isAutoCommit());
            register.setReadOnly(isReadOnly());
            register.setReadOnlyRollbackOnClose(isReadOnlyRollbackOnClose());
            register.register();
        }
        if (!getAspectRuleRegistry().contains(replicaAspectId)) {
            SqlSessionAdviceRegister register = new SqlSessionAdviceRegister(getActivityContext());
            register.setTxAspectId(replicaAspectId);
            register.setSqlSessionFactoryBeanId(getSqlSessionFactoryBeanId());
            register.setTargetBeanId(getTargetBeanId());
            register.setTargetBeanClass(getTargetBeanClass());

            register.setIncludeMethodNamePatterns(DEFAULT_READONLY_METHOD_PATTERNS);

            register.setExecutorType(getExecutorType());
            register.setIsolationLevel(getIsolationLevel());
            register.setAutoCommit(isAutoCommit());
            register.setReadOnly(true);
            register.setReadOnlyRollbackOnClose(isReadOnlyRollbackOnClose());
            register.register();
        }
        setInitialized(true);
    }

}
