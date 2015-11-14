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
package com.aspectran.core.activity.response.transform.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.util.json.JsonSerializer;

/**
 * Converts a ProcessResult object to a JSON formatted string.
 * 
 * <p>Created: 2008. 06. 12 오후 8:20:54</p>
 */
public class ContentsJsonSerializer extends JsonSerializer {

	/**
	 * Instantiates a new ContentsJsonSerializer.
	 * 
	 * @param writer the writer
	 */
	public ContentsJsonSerializer(Writer writer) {
		this(writer, false);
	}

	/**
	 * Instantiates a new ContentsJsonSerializer.
	 * 
	 * @param writer the character-output stream
	 * @param prettyPrint enables or disables pretty-printing.
	 */
	public ContentsJsonSerializer(Writer writer, boolean prettyPrint) {
		super(writer, prettyPrint);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.util.json.JsonSerializer#write(java.lang.Object)
	 */
	public void write(Object object) throws IOException, InvocationTargetException {
		if(object instanceof ProcessResult) {
			write((ProcessResult)object);
		} else {
			super.write(object);
		}
	}

	/**
	 * Write a ProcessResult object to the character streams.
	 *
	 * @param processResult the ProcessResult object to write to a character-output stream.
	 * @throws IOException An I/O error occurs.
	 * @throws InvocationTargetException the invocation target exception
	 */
	private void write(ProcessResult processResult) throws IOException, InvocationTargetException {
		if(processResult.isEmpty()) {
			writeNull();
		} else if(processResult.size() == 1) {
			ContentResult contentResult = processResult.get(0);
			write(contentResult);
		} else {
			openSquareBracket();
	
			Iterator<ContentResult> iter = processResult.iterator();
	
			while(iter.hasNext()) {
				ContentResult contentResult = iter.next();
				write(contentResult);
	
				if(iter.hasNext()) {
					writeComma();
				}
			}
	
			closeSquareBracket();
		}
	}

	/**
	 * Write a ContentResult object to the character-output stream.
	 *
	 * @param contentResult the ContentResult object to write to a character-output stream.
	 * @throws IOException An I/O error occurs.
	 * @throws InvocationTargetException the invocation target exception
	 */
	private void write(ContentResult contentResult) throws IOException, InvocationTargetException {
		if(contentResult.isEmpty()) {
			writeNull();
			return;
		}
			
		if(contentResult.getContentId() != null) {
			openCurlyBracket();
			writeName(contentResult.getContentId());
		}

		if(contentResult.size() == 1) {
			ActionResult actionResult = contentResult.get(0);
			
			if(actionResult.getActionId() != null) {
				openCurlyBracket();
			
				writeName(actionResult.getActionId());
				write(actionResult.getResultValue());
				
				closeCurlyBracket();
			} else {
				write(actionResult.getResultValue());
			}
		} else {
			openCurlyBracket();
	
			Iterator<ActionResult> iter = contentResult.iterator();
			int cnt = 0;
	
			while(iter.hasNext()) {
				ActionResult actionResult = iter.next();
				
				if(actionResult.getActionId() != null) {
					if(cnt++ > 0) {
						writeComma();
					}
					writeName(actionResult.getActionId());
					write(actionResult.getResultValue());
				}
			}
			
			closeCurlyBracket();
		}
		
		if(contentResult.getContentId() != null) {
			closeCurlyBracket();
		}
	}

}
