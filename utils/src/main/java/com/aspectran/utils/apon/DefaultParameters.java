package com.aspectran.utils.apon;

import com.aspectran.utils.BooleanUtils;

import java.util.List;

/**
 * The default concrete implementation of {@link AbstractParameters}.
 * <p>This class provides implementations for all the type-safe accessor methods
 * (e.g., {@code getString}, {@code getInt}) for retrieving parameter values.
 * It serves as the standard base class for most {@code Parameters} implementations.</p>
 *
 * <p>This class can be used in two ways:
 * <ul>
 * <li><b>With a fixed schema:</b> By providing an array of {@link ParameterKey}s to the
 * constructor, the structure is fixed, and only the defined parameters can be used.
 * <li><b>With a dynamic schema:</b> By using the default constructor {@code new DefaultParameters()},
 * the class behaves like {@link VariableParameters}, allowing parameters to be added
 * dynamically at runtime.
 * </ul></p>
 *
 * <p>Note: This class is not {@link java.io.Serializable}. For use cases that require
 * serialization (such as persisting to disk or transferring over a network),
 * use {@link VariableParameters} instead, especially when a dynamic schema is needed.</p>
 *
 * <p>Created: 2025-11-12</p>
 *
 * @see AbstractParameters
 * @see VariableParameters
 */
public class DefaultParameters extends AbstractParameters {

    /**
     * Creates a new {@code DefaultParameters} instance with a dynamic schema.
     * <p>This constructor initializes the parameters container without a predefined schema,
     * allowing parameters to be added dynamically at runtime, similar to {@link VariableParameters}.</p>
     */
    public DefaultParameters() {
        super(null);
    }

    /**
     * Creates a new {@code DefaultParameters} instance with a fixed schema defined by the given parameter keys.
     * <p>Only parameters defined in the {@code parameterKeys} array will be allowed in this container.</p>
     * @param parameterKeys an array of {@link ParameterKey}s defining the fixed schema
     */
    public DefaultParameters(ParameterKey[] parameterKeys) {
        super(parameterKeys);
    }

    /**
     * Creates a new {@code DefaultParameters} instance with a fixed schema by merging two arrays of parameter keys.
     * <p>The schema will be formed by combining the {@code topParameterKeys} and {@code bottomParameterKeys}.</p>
     * @param topParameterKeys an array of {@link ParameterKey}s for the top part of the schema
     * @param bottomParameterKeys an array of {@link ParameterKey}s for the bottom part of the schema
     */
    public DefaultParameters(ParameterKey[] topParameterKeys, ParameterKey[] bottomParameterKeys) {
        super(topParameterKeys, bottomParameterKeys);
    }

    @Override
    public Object getValue(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValue() : null);
    }

    @Override
    public Object getValue(ParameterKey key) {
        checkKey(key);
        return getValue(key.getName());
    }

    @Override
    public List<?> getValueList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueList() : null);
    }

    @Override
    public List<?> getValueList(ParameterKey key) {
        checkKey(key);
        return getValueList(key.getName());
    }

    @Override
    public String getString(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsString() : null);
    }

    @Override
    public String getString(String name, String defaultValue) {
        String s = getString(name);
        return (s != null ? s : defaultValue);
    }

    @Override
    public String getString(ParameterKey key) {
        checkKey(key);
        return getString(key.getName());
    }

    @Override
    public String getString(ParameterKey key, String defaultValue) {
        checkKey(key);
        return getString(key.getName(), defaultValue);
    }

    @Override
    public String[] getStringArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsStringArray() : null);
    }

    @Override
    public String[] getStringArray(ParameterKey key) {
        checkKey(key);
        return getStringArray(key.getName());
    }

    @Override
    public List<String> getStringList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsStringList() : null);
    }

    @Override
    public List<String> getStringList(ParameterKey key) {
        checkKey(key);
        return getStringList(key.getName());
    }

    @Override
    public Integer getInt(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsInt() : null);
    }

    @Override
    public int getInt(String name, int defaultValue) {
        Parameter p = getParameter(name);
        if (p != null) {
            Integer val = p.getValueAsInt();
            return (val != null ? val : defaultValue);
        }
        return defaultValue;
    }

    @Override
    public Integer getInt(ParameterKey key) {
        checkKey(key);
        return getInt(key.getName());
    }

    @Override
    public int getInt(ParameterKey key, int defaultValue) {
        checkKey(key);
        return getInt(key.getName(), defaultValue);
    }

    @Override
    public Integer[] getIntArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsIntArray() : null);
    }

    @Override
    public Integer[] getIntArray(ParameterKey key) {
        checkKey(key);
        return getIntArray(key.getName());
    }

    @Override
    public List<Integer> getIntList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsIntList() : null);
    }

    @Override
    public List<Integer> getIntList(ParameterKey key) {
        checkKey(key);
        return getIntList(key.getName());
    }

    @Override
    public Long getLong(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsLong() : null);
    }

    @Override
    public long getLong(String name, long defaultValue) {
        Parameter p = getParameter(name);
        if (p != null) {
            Long val = p.getValueAsLong();
            return (val != null ? val : defaultValue);
        }
        return defaultValue;
    }

    @Override
    public Long getLong(ParameterKey key) {
        checkKey(key);
        return getLong(key.getName());
    }

    @Override
    public long getLong(ParameterKey key, long defaultValue) {
        checkKey(key);
        return getLong(key.getName(), defaultValue);
    }

    @Override
    public Long[] getLongArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsLongArray() : null);
    }

    @Override
    public Long[] getLongArray(ParameterKey key) {
        checkKey(key);
        return getLongArray(key.getName());
    }

    @Override
    public List<Long> getLongList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsLongList() : null);
    }

    @Override
    public List<Long> getLongList(ParameterKey key) {
        checkKey(key);
        return getLongList(key.getName());
    }

    @Override
    public Float getFloat(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsFloat() : null);
    }

    @Override
    public float getFloat(String name, float defaultValue) {
        Parameter p = getParameter(name);
        if (p != null) {
            Float val = p.getValueAsFloat();
            return (val != null ? val : defaultValue);
        }
        return defaultValue;
    }

    @Override
    public Float getFloat(ParameterKey key) {
        checkKey(key);
        return getFloat(key.getName());
    }

    @Override
    public float getFloat(ParameterKey key, float defaultValue) {
        checkKey(key);
        return getFloat(key.getName(), defaultValue);
    }

    @Override
    public Float[] getFloatArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsFloatArray() : null);
    }

    @Override
    public Float[] getFloatArray(ParameterKey key) {
        checkKey(key);
        return getFloatArray(key.getName());
    }

    @Override
    public List<Float> getFloatList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsFloatList() : null);
    }

    @Override
    public List<Float> getFloatList(ParameterKey key) {
        checkKey(key);
        return getFloatList(key.getName());
    }

    @Override
    public Double getDouble(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsDouble() : null);
    }

    @Override
    public double getDouble(String name, double defaultValue) {
        Parameter p = getParameter(name);
        if (p != null) {
            Double val = p.getValueAsDouble();
            return (val != null ? val : defaultValue);
        }
        return defaultValue;
    }

    @Override
    public Double getDouble(ParameterKey key) {
        checkKey(key);
        return getDouble(key.getName());
    }

    @Override
    public double getDouble(ParameterKey key, double defaultValue) {
        checkKey(key);
        return getDouble(key.getName(), defaultValue);
    }

    @Override
    public Double[] getDoubleArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsDoubleArray() : null);
    }

    @Override
    public Double[] getDoubleArray(ParameterKey key) {
        checkKey(key);
        return getDoubleArray(key.getName());
    }

    @Override
    public List<Double> getDoubleList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsDoubleList() : null);
    }

    @Override
    public List<Double> getDoubleList(ParameterKey key) {
        checkKey(key);
        return getDoubleList(key.getName());
    }

    @Override
    public Boolean getBoolean(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsBoolean() : null);
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        Parameter p = getParameter(name);
        return (p != null ? BooleanUtils.toBoolean(p.getValueAsBoolean(), defaultValue) : defaultValue);
    }

    @Override
    public Boolean getBoolean(ParameterKey key) {
        checkKey(key);
        return getBoolean(key.getName());
    }

    @Override
    public boolean getBoolean(ParameterKey key, boolean defaultValue) {
        checkKey(key);
        return getBoolean(key.getName(), defaultValue);
    }

    @Override
    public Boolean[] getBooleanArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsBooleanArray() : null);
    }

    @Override
    public Boolean[] getBooleanArray(ParameterKey key) {
        checkKey(key);
        return getBooleanArray(key.getName());
    }

    @Override
    public List<Boolean> getBooleanList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? p.getValueAsBooleanList() : null);
    }

    @Override
    public List<Boolean> getBooleanList(ParameterKey key) {
        checkKey(key);
        return getBooleanList(key.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T getParameters(String name) {
        Parameter p = getParameter(name);
        return (p != null ? (T)p.getValueAsParameters() : null);
    }

    @Override
    public <T extends Parameters> T getParameters(ParameterKey key) {
        checkKey(key);
        return getParameters(key.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T[] getParametersArray(String name) {
        Parameter p = getParameter(name);
        return (p != null ? (T[])p.getValueAsParametersArray() : null);
    }

    @Override
    public <T extends Parameters> T[] getParametersArray(ParameterKey key) {
        checkKey(key);
        return getParametersArray(key.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> List<T> getParametersList(String name) {
        Parameter p = getParameter(name);
        return (p != null ? (List<T>)p.getValueAsParametersList() : null);
    }

    @Override
    public <T extends Parameters> List<T> getParametersList(ParameterKey key) {
        checkKey(key);
        return getParametersList(key.getName());
    }

}
