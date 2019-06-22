/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.json.JsonWriter;

import java.io.IOException;
import java.io.Writer;
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
    public ContentsJsonWriter write(Object object) throws IOException {
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
     */
    public void write(ProcessResult processResult) throws IOException {
        Assert.notNull(processResult, "'processResult' must not be null");
        if (processResult.getName() != null) {
            openCurlyBracket();
            writeName(processResult.getName());
        }
        if (processResult.isEmpty()) {
            writeNull();
        } else if (processResult.size() == 1) {
            ContentResult contentResult = processResult.get(0);
            write(contentResult);
        } else {
            openSquareBracket();
            Iterator<ContentResult> it = processResult.iterator();
            while (it.hasNext()) {
                ContentResult contentResult = it.next();
                write(contentResult);
                if (it.hasNext()) {
                    writeComma();
                }
            }
            closeSquareBracket();
        }
        if (processResult.getName() != null) {
            closeCurlyBracket();
        }
    }

    /**
     * Write a {@code ContentResult} object to the character-output stream.
     *
     * @param contentResult the {@code ContentResult} object to write to a character-output stream.
     * @throws IOException if an I/O error has occurred
     */
    private void write(ContentResult contentResult) throws IOException {
        if (contentResult.getName() != null) {
            openCurlyBracket();
            writeName(contentResult.getName());
        }
        if (contentResult.isEmpty()) {
            writeNull();
        } else if (contentResult.size() == 1) {
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
            int cnt = 0;
            for (String actionId : contentResult.getActionIds()) {
                if (cnt++ > 0) {
                    writeComma();
                }
                ActionResult actionResult = contentResult.getActionResult(actionId);
                writeName(actionResult.getActionId());
                write(actionResult.getResultValue());
            }
            closeCurlyBracket();
        }
        if (contentResult.getName() != null) {
            closeCurlyBracket();
        }
    }

}
