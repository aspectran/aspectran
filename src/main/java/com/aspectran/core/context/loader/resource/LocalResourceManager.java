/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.loader.resource;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.aspectran.core.context.AspectranRuntimeException;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.util.ResourceUtils;

/**
 * <p>Created: 2014. 12. 18 오후 5:51:13</p>	
 */
public class LocalResourceManager extends ResourceManager {
	
	private final String resourceLocation;
	
	private final int resourceLocationSubLen;
	
	private final AspectranClassLoader owner;
	
	public LocalResourceManager(String resourceLocation, AspectranClassLoader owner) throws InvalidResourceException {
		super();
		
		this.owner = owner;
		
		if(resourceLocation != null) {
			File file = new File(resourceLocation);
			this.resourceLocation = file.getAbsolutePath();
			this.resourceLocationSubLen = this.resourceLocation.length() + 1;
			
			if(!file.isDirectory() &&
					(file.isFile() && !resourceLocation.endsWith(ResourceUtils.JAR_FILE_SUFFIX))) {
				throw new InvalidResourceException("invalid resource directory or jar file: " + file.getAbsolutePath());
			}

			findResource(file);
		} else {
			this.resourceLocation = null;
			this.resourceLocationSubLen = 0;
		}
	}

	public void reset() throws InvalidResourceException {
		super.reset();
		
		if(resourceLocation != null) {
			findResource(new File(resourceLocation));
		}
	}
	
	private void findResource(File file) throws InvalidResourceException {
		try {
			if(file.isDirectory()) {
				List<File> jarFileList = new ArrayList<File>();
				
				findResource(file, jarFileList);
				
				if(jarFileList.size() > 0) {
					for(File jarFile : jarFileList) {
						owner.wishBrother(jarFile.getAbsolutePath());
					}
				}
			} else {
				findResourceFromJAR(file);
			}
		} catch(Exception e) {
			throw new InvalidResourceException("faild to find resource from [" + resourceLocation + "]", e);
		}
	}
	
	private void findResource(File target, final List<File> jarFileList) {
		target.listFiles(new FileFilter() {
			public boolean accept(File file) {
				String filePath = file.getAbsolutePath();
				
				String resourceName = filePath.substring(resourceLocationSubLen);

				try {
					resourceEntries.putResource(resourceName, file);
				} catch(InvalidResourceException e) {
					throw new AspectranRuntimeException(e);
				}

				if(file.isDirectory()) {
					findResource(file, jarFileList);
				} else if(file.isFile()) {
					if(filePath.endsWith(ResourceUtils.JAR_FILE_SUFFIX)) {
						jarFileList.add(file);
					}
				}
				
				return false;
			}
		});
	}
	
	private void findResourceFromJAR(File target) throws InvalidResourceException, IOException {
		JarFile jarFile = null;
		
		try {
			jarFile = new JarFile(target);
			
			for(Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				resourceEntries.putResource(target, entry);
			}
		} finally {
			if(jarFile != null)
				jarFile.close();
		}
	}

}
