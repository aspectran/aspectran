/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.activity.response.transform.json;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.util.json.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

/**
 * Converts a ProcessResult object to a JSON formatted string.
 * 
 * <p>Created: 2008. 06. 12 PM 8:20:54</p>
 */
public class ContentsJsonWriter extends JsonWriter {

    /**
     * Instantiates a new ContentsJsonWriter.
     *
     * @param writer a {@code Writer} object that can send character text
     */
    public ContentsJsonWriter(Writer writer) {
        this(writer, false);
    }

    /**
     * Instantiates a new ContentsJsonWriter.
     *
     * @param writer a {@code Writer} object that can send character text
     * @param prettyPrint enables or disables pretty-printing
     */
    public ContentsJsonWriter(Writer writer, boolean prettyPrint) {
        super(writer, prettyPrint);
    }

    /**
     * Instantiates a new ContentsJsonWriter.
     *
     * @param writer a {@code Writer} object that can send character text
     * @param indentString the string that should be used for indentation when pretty-printing is enabled
     */
    public ContentsJsonWriter(Writer writer, String indentString) {
        super(writer, indentString);
    }

    @Override
    public ContentsJsonWriter write(Object object) throws IOException, InvocationTargetException {
        if (object instanceof ProcessResult) {
            write((ProcessResult)object);
        } else {
            super.write(object);
        }
        return this;
    }

    /**
     * Write a {@code ProcessResult} object to the character-output stream.
     *
     * @param processResult the {@code ProcessResult} object to write to a character-output stream
     * @throws IOException if an I/O error has occurred
     * @throws InvocationTargetException the invocation target exception
     */
    private void write(ProcessResult processResult) throws IOException, InvocationTargetException {
        if (processResult.isEmpty()) {
            writeNull();
        } else if (processResult.size() == 1) {
            ContentResult contentResult = processResult.get(0);
            write(contentResult);
        } else {
            openSquareBracket();
            Iterator<ContentResult> iter = processResult.iterator();
            while (iter.hasNext()) {
                ContentResult contentResult = iter.next();
                write(contentResult);
                if (iter.hasNext()) {
                    writeComma();
                }
            }
            closeSquareBracket();
        }
    }

    /**
     * Write a {@code ContentResult} object to the character-output stream.
     *
     * @param contentResult the {@code ContentResult} object to write to a character-output stream.
     * @throws IOException if an I/O error has occurred
     * @throws InvocationTargetException the invocation target exception
     */
    private void write(ContentResult contentResult) throws IOException, InvocationTargetException {
        if (contentResult.isEmpty()) {
            writeNull();
            return;
        }
        if (contentResult.getName() != null) {
            openCurlyBracket();
            writeName(contentResult.getName());
        }
        if (contentResult.size() == 1) {
            ActionResult actionResult = contentResult.get(0);
            if (actionResult.getActionId() != null) {
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
            while (iter.hasNext()) {
                ActionResult actionResult = iter.next();
                if (actionResult.getActionId() != null) {
                    if (cnt++ > 0) {
                        writeComma();
                    }
                    writeName(actionResult.getActionId());
                    write(actionResult.getResultValue());
                }
            }
            closeCurlyBracket();
        }
        if (contentResult.getName() != null) {
            closeCurlyBracket();
        }
    }

}
