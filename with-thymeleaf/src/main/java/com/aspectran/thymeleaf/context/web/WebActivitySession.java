package com.aspectran.thymeleaf.context.web;

import com.aspectran.core.adapter.SessionAdapter;
import org.thymeleaf.web.IWebSession;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class WebActivitySession implements IWebSession {

    private final SessionAdapter sessionAdapter;

    WebActivitySession(SessionAdapter sessionAdapter) {
        this.sessionAdapter = sessionAdapter;
    }

    @Override
    public boolean exists() {
        return sessionAdapter.isValid();
    }

    @Override
    public boolean containsAttribute(String name) {
        return (sessionAdapter.getAttribute(name) != null);
    }

    @Override
    public int getAttributeCount() {
        Enumeration<String> enumer = sessionAdapter.getAttributeNames();
        if (enumer == null) {
            return 0;
        }
        int count = 0;
        while (enumer.hasMoreElements()) {
            enumer.nextElement();
            count++;
        }
        return count;
    }

    @Override
    public Set<String> getAllAttributeNames() {
        Enumeration<String> enumer = sessionAdapter.getAttributeNames();
        if (enumer == null) {
            return Collections.emptySet();
        }
        Set<String> attributeNames = new LinkedHashSet<String>(10);
        while (enumer.hasMoreElements()) {
            attributeNames.add(enumer.nextElement());
        }
        return Collections.unmodifiableSet(attributeNames);
    }

    @Override
    public Map<String, Object> getAttributeMap() {
        Enumeration<String> enumer = sessionAdapter.getAttributeNames();
        if (enumer == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> attributeMap = new LinkedHashMap<String, Object>(10);
        String attributeName;
        while (enumer.hasMoreElements()) {
            attributeName = enumer.nextElement();
            attributeMap.put(attributeName, getAttributeValue(attributeName));
        }
        return Collections.unmodifiableMap(attributeMap);
    }

    @Override
    public Object getAttributeValue(String name) {
        return sessionAdapter.getAttribute(name);
    }

    @Override
    public void setAttributeValue(String name, Object value) {
        sessionAdapter.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        sessionAdapter.removeAttribute(name);
    }

}
