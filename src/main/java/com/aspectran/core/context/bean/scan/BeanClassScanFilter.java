package com.aspectran.core.context.bean.scan;

public interface BeanClassScanFilter {
	
	public String filter(String beanId, String resourceName, Class<?> scannedClass);

}
