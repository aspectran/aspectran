/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
 * It contains the results of executing the command.
 *
 * <p>Created: 2019-01-19</p>
 */
public class CommandResult {

    private final boolean success;

    private final String result;

    /**
     * Creates a new instance of the command result.
     * @param success whether the command executed successfully
     * @param result the output of executed command
     */
    public CommandResult(boolean success, String result) {
        this.success = success;
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getResult() {
        return result;
    }

}
