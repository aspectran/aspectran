/*
 *  Copyright (c) 2009 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.context.loader.resource;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.util.ResourceUtils;


/**
 * <p>Created: 2014. 12. 18 오후 5:51:13</p>	
 */
public class LocalResourceManager {
	
	private final String resourceLocation;
	
	private final AspectranClassLoader owner;
	
	private boolean archived;
	
	private final ResourceEntries resourceEntries = new ResourceEntries();
	
	public LocalResourceManager(String resourceLocation, AspectranClassLoader owner) {
		this.resourceLocation = resourceLocation;
		this.owner = owner;
		
		if(resourceLocation != null && resourceLocation.endsWith(ResourceUtils.JAR_FILE_SUFFIX)) {
			this.archived = true;
		} else {
			this.archived = false;
		}
		
		if(resourceLocation != null)
			findResource();
	}

	public void findResource() {
		if(resourceLocation != null && resourceLocation.endsWith(ResourceUtils.JAR_FILE_SUFFIX)) {
			this.archived = true;
		} else {
			this.archived = false;
		}
		
		try {
			File file = new File(resourceLocation);
			
			if(this.archived) {
				if(!file.isFile())
					throw new FileNotFoundException("invalid resource jar file: " + resourceLocation);

				findResourceFromJAR(file);
			} else {
				if(!file.isDirectory())
					throw new FileNotFoundException("invalid resource directory: " + resourceLocation);
				
				List<File> jarFileList = new LinkedList<File>();
				
				findResource(file, jarFileList);
				
				if(jarFileList.size() > 0) {
					for(File jarFile : jarFileList) {
						owner.wishBrother(jarFile.getAbsolutePath());
					}
				}
			}
		} catch(Exception e) {
			throw new InvalidResourceException("Faild to find resource from " + resourceLocation, e);
		}
	}
	
	private void findResource(File target, final List<File> jarFileList) {
		target.listFiles(new FileFilter() {
			public boolean accept(File file) {
				String filePath = file.getAbsolutePath();
				String resourceName = filePath.substring(resourceLocation.length() + 1);
				
				resourceEntries.putResource(resourceName, file);

				if(file.isDirectory()) {
					//System.out.println("AbsolutePath: " + filePath);
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
	
	private void findResourceFromJAR(File target) throws IOException {
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
