/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.context.builder.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.aspectran.core.util.Resources;

/**
 * Offline entity resolver for the Aspectran DTDs
 * 
 * <p>Created: 2008. 06. 14 오전 4:48:34</p>
 */
public class AspectranDtdResolver implements EntityResolver {

	private static final String ASPECTRAN_DTD = "com/aspectran/core/context/builder/xml/dtd/aspectran-1.0.dtd";

	private static final Map<String, String> doctypeMap = new HashMap<String, String>();

	static {
		doctypeMap.put("aspectran-1.0.dtd".toUpperCase(), ASPECTRAN_DTD);
		doctypeMap.put("-//aspectran.com//DTD Aspectran 1.0//EN".toUpperCase(), ASPECTRAN_DTD);
	}

	/**
	 * Converts a public DTD into a local one.
	 * 
	 * @param publicId Unused but required by EntityResolver interface
	 * @param systemId The DTD that is being requested
	 * 
	 * @return The InputSource for the DTD
	 * 
	 * @throws SAXException If anything goes wrong
	 */
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
		if(publicId != null)
			publicId = publicId.toUpperCase();
		
		if(systemId != null)
			systemId = systemId.toUpperCase();

		InputSource source = null;
		
		try {
			String path = doctypeMap.get(publicId);
			source = getInputSource(path, source);
			
			if(source == null) {
				path = doctypeMap.get(systemId);
				source = getInputSource(path, source);
			}
		} catch(Exception e) {
			throw new SAXException(e.toString());
		}
		
		return source;
	}

	/**
	 * Gets the input source.
	 * 
	 * @param path the path
	 * @param source the source
	 * 
	 * @return the input source
	 */
	private InputSource getInputSource(String path, InputSource source) {
		if(path != null) {
			InputStream in = null;
			
			try {
				in = Resources.getResourceAsStream(path);
				source = new InputSource(in);
			} catch(IOException e) {
				// ignore, null is ok
			}
		}
		
		return source;
	}
}
