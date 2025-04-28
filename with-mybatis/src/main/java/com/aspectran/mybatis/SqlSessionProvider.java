package com.aspectran.mybatis;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.NoSuchBeanException;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.JoinpointRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * <p>Created: 2025-04-23</p>
 */
public abstract class SqlSessionProvider extends InstantActivitySupport implements InitializableBean {

    private final String relevantAspectId;

    private String sqlSessionFactoryBeanId;

    private ExecutorType executorType;

    private boolean autoCommit;

    public SqlSessionProvider(String relevantAspectId) {
        if (relevantAspectId == null) {
            throw new IllegalArgumentException("relevantAspectId must not be null");
        }
        this.relevantAspectId = relevantAspectId;
    }

    @AvoidAdvice
    public void setSqlSessionFactoryBeanId(String sqlSessionFactoryBeanId) {
        this.sqlSessionFactoryBeanId = sqlSessionFactoryBeanId;
    }

    @AvoidAdvice
    public void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }

    @AvoidAdvice
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    @AvoidAdvice
    protected SqlSession getSqlSession() {
        SqlSessionAdvice sqlSessionAdvice = getSqlSessionAdvice();
        SqlSession sqlSession = sqlSessionAdvice.getSqlSession();
        if (sqlSession == null) {
            if (sqlSessionAdvice.isArbitrarilyClosed()) {
                sqlSessionAdvice.open();
                sqlSession = sqlSessionAdvice.getSqlSession();
            } else {
                throw new IllegalStateException("SqlSession is not opened");
            }
        }
        return sqlSession;
    }

    @AvoidAdvice
    @NonNull
    protected SqlSessionAdvice getSqlSessionAdvice() {
        checkTransactional();
        SqlSessionAdvice sqlSessionAdvice = getAvailableActivity().getAspectAdviceBean(relevantAspectId);
        if (sqlSessionAdvice == null) {
            sqlSessionAdvice = getAvailableActivity().getBeforeAdviceResult(relevantAspectId);
        }
        if (sqlSessionAdvice == null) {
            if (getActivityContext().getAspectRuleRegistry().getAspectRule(relevantAspectId) == null) {
                throw new IllegalArgumentException("Aspect '" + relevantAspectId +
                        "' handling SqlSessionAdvice is not registered");
            }
            throw new IllegalStateException("SqlSessionAdvice not found handled by aspect '" + relevantAspectId + "'");
        }
        return sqlSessionAdvice;
    }

    @AvoidAdvice
    private void checkTransactional() {
        if (getAvailableActivity().getMode() == Activity.Mode.PROXY) {
            throw new IllegalStateException("Cannot be executed on a non-transactional activity;" +
                    " needs to be wrapped in an instant activity.");
        }
    }

    @AvoidAdvice
    @Override
    public void initialize() {
        if (!getActivityContext().getAspectRuleRegistry().contains(relevantAspectId)) {
            registerSqlSessionAdvice();
        }
    }

    @AvoidAdvice
    protected void registerSqlSessionAdvice() {
        if (getActivityContext().getAspectRuleRegistry().contains(relevantAspectId)) {
            throw new IllegalStateException("SqlSessionAdvice is already registered");
        }

        SqlSessionFactory sqlSessionFactory;
        try {
            sqlSessionFactory = getBeanRegistry().getBean(SqlSessionFactory.class, sqlSessionFactoryBeanId);
        } catch (NoSuchBeanException e) {
            if (sqlSessionFactoryBeanId != null) {
                throw new IllegalStateException("Cannot resolve SqlSessionFactory with id=" + sqlSessionFactoryBeanId, e);
            } else {
                throw new IllegalStateException("SqlSessionFactory is not defined", e);
            }
        }

        AspectRule aspectRule = new AspectRule();
        aspectRule.setId(relevantAspectId);
        aspectRule.setOrder(0);
        aspectRule.setIsolated(true);

        String pattern = "**@class:" + ClassUtils.getUserClass(getClass()).getName();
        PointcutPatternRule pointcutPatternRule = PointcutPatternRule.newInstance(pattern);

        PointcutRule pointcutRule = new PointcutRule(PointcutType.WILDCARD);
        pointcutRule.addPointcutPatternRule(pointcutPatternRule);

        JoinpointRule joinpointRule = new JoinpointRule();
        joinpointRule.setJoinpointTargetType(JoinpointTargetType.ACTIVITY);
        joinpointRule.setPointcutRule(pointcutRule);

        aspectRule.setJoinpointRule(joinpointRule);

        AspectAdviceRule beforeAspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.BEFORE);
        beforeAspectAdviceRule.setAdviceAction(activity -> {
            SqlSessionAdvice sqlSessionAdvice = new SqlSessionAdvice(sqlSessionFactory);
            if (executorType != null) {
                sqlSessionAdvice.setExecutorType(executorType);
            }
            sqlSessionAdvice.setAutoCommit(autoCommit);
            sqlSessionAdvice.open();
            return sqlSessionAdvice;
        });

        AspectAdviceRule afterAspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.AFTER);
        afterAspectAdviceRule.setAdviceAction(activity -> {
            SqlSessionAdvice sqlSessionAdvice = activity.getBeforeAdviceResult(relevantAspectId);
            sqlSessionAdvice.commit();
            return null;
        });

        AspectAdviceRule finallyAspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.FINALLY);
        finallyAspectAdviceRule.setAdviceAction(activity -> {
            SqlSessionAdvice sqlSessionAdvice = activity.getBeforeAdviceResult(relevantAspectId);
            sqlSessionAdvice.close();
            return null;
        });

        try {
            getActivityContext().getAspectRuleRegistry().addAspectRule(aspectRule);
        } catch (IllegalRuleException e) {
            ToStringBuilder tsb = new ToStringBuilder("Failed to register SqlSessionAdvice with");
            tsb.append("relevantAspectId", relevantAspectId);
            tsb.append("sqlSessionFactoryBeanId", sqlSessionFactoryBeanId);
            throw new RuntimeException(tsb.toString(), e);
        }
    }

}
