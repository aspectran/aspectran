package com.aspectran.core.context.translet.scan;

import java.io.File;

public interface TemplateFileScanFilter {
	
	public String filter(String filePath, File templateFile);

}
