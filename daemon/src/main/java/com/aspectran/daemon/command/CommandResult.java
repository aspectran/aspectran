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
package com.aspectran.daemon.command;

/**
 * Represents the outcome of a daemon command execution.
 * <p>
 * A {@code CommandResult} is immutable and contains a success flag and an
 * optional textual message produced by the command. When a command finishes
 * without a textual response, the {@code result} may be {@code null}.
 * </p>
 *
 * <p>Created: 2019-01-19</p>
 */
public class CommandResult {

    private final boolean success;

    private final String result;

    /**
     * Creates a new instance of the command result.
     * @param success whether the command executed successfully
     * @param result the output produced by the executed command; may be {@code null}
     */
    public CommandResult(boolean success, String result) {
        this.success = success;
        this.result = result;
    }

    /**
     * Indicates whether the associated command completed successfully.
     * @return {@code true} if the command succeeded; {@code false} otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the textual output produced by the command, if any.
     * @return the command output, or {@code null} if the command did not produce text
     */
    public String getResult() {
        return result;
    }

}
