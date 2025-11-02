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
package com.aspectran.core.component.bean;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Default implementation of the Aspectran {@link BeanRegistry}.
 * <p>
 * Provides standard lookup and containment operations by id and type,
 * delegating to a parent registry when configured. Actual instantiation
 * and scope handling are performed by the base {@link AbstractBeanRegistry}.
 * </p>
 */
public class DefaultBeanRegistry extends AbstractBeanRegistry {

    public DefaultBeanRegistry(ActivityContext context, BeanRuleRegistry beanRuleRegistry) {
        super(context, beanRuleRegistry);
    }

    /**
     * {@inheritDoc}
     * <p>If a bean with the given ID is not found in this registry,
     * it will be looked up in the parent registry.</p>
     */
    @Override
    public <V> V getBean(String id) {
        BeanRule beanRule = getBeanRuleRegistry().getBeanRule(id);
        if (beanRule == null) {
            BeanRegistry parent = getParentBeanRegistry();
            if (parent != null) {
                return parent.getBean(id);
            }
            throw new NoSuchBeanException(id);
        }
        return getBean(beanRule);
    }

    /**
     * {@inheritDoc}
     * <p>This is a convenience method that delegates to {@link #getBean(Class, String)} with a null ID.</p>
     */
    @Override
    public <V> V getBean(Class<V> type) {
        return getBean(type, null);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation first attempts to find a matching bean in the current registry.
     * If no definitive match is found (e.g., no beans of the type, or the ID does not match),
     * it delegates the request to the parent registry.</p>
     */
    @Override
    public <V> V getBean(@NonNull Class<V> type, @Nullable String id) {
        BeanRule[] beanRules = getBeanRuleRegistry().getBeanRules(type);
        if (beanRules == null) {
            BeanRule beanRule = getBeanRuleRegistry().getBeanRuleForConfig(type);
            if (beanRule != null) {
                return getBean(beanRule);
            } else {
                BeanRegistry parent = getParentBeanRegistry();
                if (parent != null) {
                    return parent.getBean(type, id);
                }
                if (id != null) {
                    throw new NoSuchBeanException(type, id);
                } else {
                    throw new NoSuchBeanException(type);
                }
            }
        }
        if (beanRules.length == 1) {
            if (id != null) {
                if (id.equals(beanRules[0].getId())) {
                    return getBean(beanRules[0]);
                } else {
                    BeanRegistry parent = getParentBeanRegistry();
                    if (parent != null) {
                        return parent.getBean(type, id);
                    }
                    throw new NoSuchBeanException(type, id);
                }
            } else {
                return getBean(beanRules[0]);
            }
        } else {
            if (id != null) {
                for (BeanRule beanRule : beanRules) {
                    if (id.equals(beanRule.getId())) {
                        return getBean(beanRule);
                    }
                }
                BeanRegistry parent = getParentBeanRegistry();
                if (parent != null) {
                    return parent.getBean(type, id);
                }
                throw new NoSuchBeanException(type, id);
            } else {
                BeanRegistry parent = getParentBeanRegistry();
                if (parent != null) {
                    return parent.getBean(type, null);
                }
                throw new NoUniqueBeanException(type, beanRules);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>Returns all beans of the given type from this registry. If no beans are found,
     * it queries the parent registry. Returns {@code null} if no beans are found in either.</p>
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> V[] getBeansOfType(Class<V> type) {
        BeanRule[] beanRules = getBeanRuleRegistry().getBeanRules(type);
        if (beanRules != null) {
            Object arr = Array.newInstance(type, beanRules.length);
            for (int i = 0; i < beanRules.length; i++) {
                Object bean = getBean(beanRules[i]);
                Array.set(arr, i, bean);
            }
            return (V[])arr;
        } else {
            BeanRegistry parent = getParentBeanRegistry();
            if (parent != null) {
                return parent.getBeansOfType(type);
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>Checks both this registry and its parent registry for a bean with the given ID.</p>
     */
    @Override
    public boolean containsBean(String id) {
        if (getBeanRuleRegistry().containsBeanRule(id)) {
            return true;
        }
        BeanRegistry parent = getParentBeanRegistry();
        if (parent != null) {
            return parent.containsBean(id);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>Checks both this registry and its parent registry for any bean of the given type.</p>
     */
    @Override
    public boolean containsBean(Class<?> type) {
        if (getBeanRuleRegistry().containsBeanRule(type)) {
            return true;
        }
        BeanRegistry parent = getParentBeanRegistry();
        if (parent != null) {
            return parent.containsBean(type);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>Checks both this registry and its parent registry for a bean of the given type and ID.</p>
     */
    @Override
    public boolean containsBean(@NonNull Class<?> type, @Nullable String id) {
        BeanRule[] beanRules = getBeanRuleRegistry().getBeanRules(type);
        if (beanRules == null) {
            BeanRegistry parent = getParentBeanRegistry();
            if (parent != null) {
                return parent.containsBean(type, id);
            }
            return false;
        }
        if (beanRules.length == 1) {
            if (id != null) {
                if (id.equals(beanRules[0].getId())) {
                    return true;
                }
                BeanRegistry parent = getParentBeanRegistry();
                if (parent != null) {
                    return parent.containsBean(type, id);
                }
                return false;
            } else {
                return true;
            }
        } else {
            if (id != null) {
                for (BeanRule beanRule : beanRules) {
                    if (id.equals(beanRule.getId())) {
                        return true;
                    }
                }
                BeanRegistry parent = getParentBeanRegistry();
                if (parent != null) {
                    return parent.containsBean(type, id);
                }
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation checks if exactly one bean of the given type is present in this registry.
     * If not, it delegates the check to the parent registry.</p>
     */
    @Override
    public boolean containsSingleBean(Class<?> type) {
        BeanRule[] beanRules = getBeanRuleRegistry().getBeanRules(type);
        if (beanRules == null) {
            BeanRegistry parent = getParentBeanRegistry();
            if (parent != null) {
                return parent.containsSingleBean(type);
            }
            return false;
        }
        return (beanRules.length == 1);
    }

    /**
     * {@inheritDoc}
     * <p>This search is confined to the current bean registry and does not consult the parent registry.</p>
     */
    @Override
    public Collection<Class<?>> findConfigBeanClassesWithAnnotation(Class<? extends Annotation> annotationType) {
        return getBeanRuleRegistry().findConfigBeanClassesWithAnnotation(annotationType);
    }

    @Nullable
    private BeanRegistry getParentBeanRegistry() {
        if (getActivityContext().getMasterService() != null) {
            CoreService parentService = getActivityContext().getMasterService().getParentService();
            if (parentService != null) {
                return parentService.getActivityContext().getBeanRegistry();
            }
        }
        return null;
    }

}
