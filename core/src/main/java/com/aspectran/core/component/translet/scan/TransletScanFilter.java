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
package com.aspectran.core.component.translet.scan;

import java.io.File;

/**
 * A callback interface for filtering files found during a translet scan.
 * Implementations of this interface can be used to apply custom logic to determine
 * whether a discovered file should be processed as a translet.
 *
 * @since 2.0.0
 */
@FunctionalInterface
public interface TransletScanFilter {

    /**
     * Determines whether a scanned file should be included for translet creation.
     *
     * @param transletName the potential name of the translet that would be generated from the file
     * @param scannedFile the actual file that was discovered by the scanner
     * @return {@code true} to include the file, {@code false} to exclude it
     */
    boolean filter(String transletName, File scannedFile);

}
