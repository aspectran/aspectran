/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.utils.Assert;
import com.aspectran.utils.json.JsonWriter;

import java.io.IOException;
import java.io.Writer;

/**
 * A specialized {@link JsonWriter} that converts a {@link ProcessResult} object
 * into a JSON formatted string.
 *
 * <p>This class extends the basic JSON writing capabilities to specifically handle
 * the hierarchical structure of Aspectran's activity results ({@code ProcessResult},
 * {@link ContentResult}, and {@link ActionResult}). It maps them into a corresponding
 * JSON object or array structure, supporting pretty-printing and null value handling
 * through its {@link com.aspectran.utils.StringifyContext}.</p>
 *
 * <p>Created: 2008. 06. 12 PM 8:20:54</p>
 */
public class ContentsJsonWriter extends JsonWriter {

    /**
     * Instantiates a new ContentsJsonWriter.
     * @param writer a {@code Writer} object that can send character text
     */
    public ContentsJsonWriter(Writer writer) {
        super(writer);
    }

    @Override
    public void writeValue(Object object) throws IOException {
        if (object instanceof ProcessResult processResult) {
            writeValue(processResult);
        } else {
            super.writeValue(object);
        }
    }

    /**
     * Writes a {@code ProcessResult} to the output stream.
     * <p>If the result has a name, it is written as a JSON object with that name as the key.
     * If it contains a single {@link ContentResult}, that result is written as the value.
     * If it contains multiple results, they are written as a JSON array.</p>
     * @param processResult the {@code ProcessResult} to write
     * @throws IOException if an I/O error occurs
     */
    private void writeValue(ProcessResult processResult) throws IOException {
        Assert.notNull(processResult, "processResult must not be null");
        if (processResult.getName() != null) {
            beginObject();
            writeName(processResult.getName());
        }
        if (processResult.isEmpty()) {
            writeNull(processResult.getName() == null);
        } else if (processResult.size() == 1) {
            writeValue(processResult.getFirst());
        } else {
            beginArray();
            for (ContentResult contentResult : processResult) {
                writeValue(contentResult);
            }
            endArray();
        }
        if (processResult.getName() != null) {
            endObject();
        }
    }

    /**
     * Writes a {@code ContentResult} to the output stream.
     * <p>If the result has a name, it is written as a JSON object with that name as the key.
     * If it contains a single {@link ActionResult} with no action ID, the result value is
     * written directly. Otherwise, the action results are written as a JSON object, mapping
     * action IDs to their result values.</p>
     * @param contentResult the {@code ContentResult} to write
     * @throws IOException if an I/O error occurs
     */
    private void writeValue(ContentResult contentResult) throws IOException {
        Assert.notNull(contentResult, "contentResult must not be null");
        if (contentResult.getName() != null) {
            beginObject();
            writeName(contentResult.getName());
        }
        if (contentResult.isEmpty()) {
            writeNull();
        } else if (contentResult.size() == 1) {
            ActionResult actionResult = contentResult.getFirst();
            if (actionResult.getActionId() != null) {
                beginObject();
                writeName(actionResult.getActionId());
                writeValue(actionResult.getResultValue());
                endObject();
            } else {
                writeValue(actionResult.getResultValue());
            }
        } else {
            beginObject();
            for (String actionId : contentResult.getActionIds()) {
                ActionResult actionResult = contentResult.getActionResult(actionId);
                writeName(actionResult.getActionId());
                writeValue(actionResult.getResultValue());
            }
            endObject();
        }
        if (contentResult.getName() != null) {
            endObject();
        }
    }

}
