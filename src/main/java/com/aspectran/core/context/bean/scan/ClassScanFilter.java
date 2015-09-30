package com.aspectran.core.context.bean.scan;

public interface ClassScanFilter {
	
	public boolean filter(String beanId, String className, Class<?> scannedClass);

}
