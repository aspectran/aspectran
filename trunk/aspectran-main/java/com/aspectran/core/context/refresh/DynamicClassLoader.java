package com.aspectran.core.context.refresh;

import com.aspectran.core.util.ClassUtils;

public class DynamicClassLoader extends ClassLoader {

	public DynamicClassLoader() {
		this(ClassUtils.getDefaultClassLoader());
	}
	
	public DynamicClassLoader(ClassLoader parent) {
		super(parent);
	}
	
}
