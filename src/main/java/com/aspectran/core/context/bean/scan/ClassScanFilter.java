package com.aspectran.core.context.bean.scan;

public interface ClassScanFilter {
	
	public String filter(String beanId, String className, Class<?> scannedClass);

}
