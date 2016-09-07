/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.adapter;

/**
 * The Class BasicResponseAdapter.
 * 
 * @since 2016. 2. 13.
 */
public abstract class BasicResponseAdapter extends AbstractResponseAdapter implements ResponseAdapter {

	private String characterEncoding;

	private String contentType;

	/**
	 * Instantiates a new Basic response adapter.
	 *
	 * @param adaptee the adaptee object
	 */
	public BasicResponseAdapter(Object adaptee) {
		super(adaptee);
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
