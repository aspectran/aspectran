/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.jpa.test.pagination;

import com.aspectran.core.activity.Translet;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.function.LongSupplier;

/**
 * <p>Created: 2025-04-21</p>
 */
public class PageInfo {

    private static final int DEFAULT_PAGE_SIZE = 5;

    private final int number;

    private final int size;

    private final long offset;

    private long totalElements;

    private int totalPages;

    public PageInfo(int number, int size) {
        this.number = number;
        this.size = size;
        this.offset = (long)(number - 1) * size;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }

    public long getOffset() {
        return offset;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
        this.totalPages = (totalElements > 0 ? (int)((totalElements - 1) / size + 1) : 0);
    }

    public void setTotalElements(int actualPageSize, LongSupplier totalSupplier) {
        Assert.notNull(totalSupplier, "TotalSupplier must not be null");
        if (isPartialPage(actualPageSize)) {
            if (isFirstPage()) {
                setTotalElements(actualPageSize);
            } if (actualPageSize > 0) {
                setTotalElements(offset + actualPageSize);
            }
        } else {
            setTotalElements(totalSupplier.getAsLong());
        }
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isPartialPage(int actualPageSize) {
        return (actualPageSize < size);
    }

    public boolean isFirstPage() {
        return (number == 1);
    }

    public boolean isLastPage() {
        return (number == totalPages);
    }

    public boolean hasPreviousPage() {
        return (number > 1);
    }

    @NonNull
    public static PageInfo of(@NonNull Translet translet) {
        return of(translet, DEFAULT_PAGE_SIZE);
    }

    @NonNull
    public static PageInfo of(@NonNull Translet translet, int defaultPageSize) {
        String pageNumber = translet.getParameter("page");
        String pageSize = translet.getParameter("size");
        int number = StringUtils.isEmpty(pageNumber) ? 1 : Integer.parseInt(pageNumber);
        int size = StringUtils.isEmpty(pageSize) ? defaultPageSize : Integer.parseInt(pageSize);
        return of(number, size);
    }

    @NonNull
    public static PageInfo of(int number, int size) {
        if (number < 1) {
            number = 1;
        }
        if (size < 1) {
            size = DEFAULT_PAGE_SIZE;
        }
        return new PageInfo(number, size);
    }

}
