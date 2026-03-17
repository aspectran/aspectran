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
package com.aspectran.test.web.mock;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

/**
 * Mock implementation of {@link RequestDispatcher}.
 *
 * <p>Created: 2026. 3. 17.</p>
 */
public class MockRequestDispatcher implements RequestDispatcher {

    private final String path;

    public MockRequestDispatcher(String path) {
        this.path = path;
    }

    @Override
    public void forward(ServletRequest request, @NonNull ServletResponse response) throws ServletException, IOException {
        if (response.isCommitted()) {
            throw new IllegalStateException("Cannot forward after response has been committed");
        }
        request.setAttribute("javax.servlet.forward.request_uri", path);
        request.setAttribute("jakarta.servlet.forward.request_uri", path);
    }

    @Override
    public void include(@NonNull ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute("javax.servlet.include.request_uri", path);
        request.setAttribute("jakarta.servlet.include.request_uri", path);
    }

    public String getPath() {
        return path;
    }

}
