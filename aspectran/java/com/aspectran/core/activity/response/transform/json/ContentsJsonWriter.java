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
package com.aspectran.core.activity.response.transform.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.util.json.JsonWriter;

/**
 * <p>Created: 2008. 06. 12 오후 8:20:54</p>
 */
public class ContentsJsonWriter extends JsonWriter {

	/**
	 * Instantiates a new contents json writer.
	 * 
	 * @param writer the writer
	 */
	public ContentsJsonWriter(Writer writer) {
		this(writer, false);
	}

	/**
	 * Instantiates a new contents json writer.
	 * 
	 * @param writer the writer
	 * @param prettyWrite the pretty write
	 */
	public ContentsJsonWriter(Writer writer, boolean prettyWrite) {
		super(writer, prettyWrite);
	}

	/**
	 * Write.
	 * 
	 * @param processResult the process result
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InvocationTargetException the invocation target exception
	 */
	public void write(ProcessResult processResult) throws IOException, InvocationTargetException {
		openCurlyBracket();
		write(processResult, null);
		nextLine();
		closeCurlyBracket();
	}

	/**
	 * Write.
	 * 
	 * @param processResult the process result
	 * @param parentContentId the parent action path
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InvocationTargetException the invocation target exception
	 */
	private void write(ProcessResult processResult, String parentContentId) throws IOException,
			InvocationTargetException {
		if(processResult == null || processResult.size() == 0)
			return;

		Iterator<ContentResult> iter = processResult.iterator();

		while(iter.hasNext()) {
			ContentResult contentResult = iter.next();

			if(contentResult != null && contentResult.size() > 0) {
				Iterator<ActionResult> iter2 = contentResult.iterator();

				while(iter2.hasNext()) {
					ActionResult actionResult = iter2.next();

					Object resultValue = actionResult.getResultValue();

					if(resultValue == null) {
						writeNull();
					} else if(resultValue instanceof ProcessResult) {
						write((ProcessResult)resultValue, actionResult.getQuialifiedActionId());
					} else {
						writeName(actionResult.getQuialifiedActionId(parentContentId));
						write(resultValue);
					}

					if(iter2.hasNext()) {
						writeComma();
						nextLine();
					}
				}

				if(iter.hasNext()) {
					writeComma();
					nextLine();
				}
			}
		}
	}

}
