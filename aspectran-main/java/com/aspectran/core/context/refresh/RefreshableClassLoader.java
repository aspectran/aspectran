package com.aspectran.core.context.refresh;

import com.aspectran.core.util.ClassUtils;

public class RefreshableClassLoader extends ClassLoader {

	public RefreshableClassLoader() {
		this(ClassUtils.getDefaultClassLoader());
	}
	
	public RefreshableClassLoader(ClassLoader parent) {
		super(parent);
	}
	
}
