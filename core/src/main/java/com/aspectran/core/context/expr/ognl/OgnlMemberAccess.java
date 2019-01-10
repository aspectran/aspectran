package com.aspectran.core.context.expr.ognl;

import ognl.MemberAccess;

import java.lang.reflect.Member;
import java.util.Map;

public class OgnlMemberAccess implements MemberAccess {

    @Override
    public Object setup(Map context, Object target, Member member, String propertyName) {
        return null;
    }

    @Override
    public void restore(Map context, Object target, Member member, String propertyName, Object state) {
    }

    @Override
    public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
        return false;
    }

}