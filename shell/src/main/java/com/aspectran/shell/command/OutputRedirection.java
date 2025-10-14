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
package com.aspectran.shell.command;

import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents an output redirection operation (e.g., {@code >} or {@code >>})
 * parsed from a command line.
 * <p>This class holds the redirection operator and the target file (operand).</p>
 *
 * <p>Created: 2017. 3. 8.</p>
 */
public class OutputRedirection {

    private final Operator operator;

    private String operand;

    /**
     * Instantiates a new output redirection.
     * @param operator the redirection operator
     */
    public OutputRedirection(@NonNull Operator operator) {
        this.operator = operator;
    }

    /**
     * Gets the operand (e.g., the file path).
     * @return the operand
     */
    public String getOperand() {
        return operand;
    }

    /**
     * Sets the operand (e.g., the file path).
     * @param operand the operand
     */
    public void setOperand(String operand) {
        this.operand = operand;
    }

    /**
     * Gets the redirection operator.
     * @return the operator
     */
    public Operator getOperator() {
        return operator;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof OutputRedirection redirection) {
            return redirection.getOperand().equals(operand) &&
                    redirection.getOperator().equals(operator);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17;
        result = prime * result + operator.hashCode();
        result = prime * result + (operand != null ? operand.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("operator", operator);
        tsb.append("operand", operand);
        return tsb.toString();
    }

    /**
     * Serializes a collection of redirections into a string.
     * @param redirectionList the list of redirections
     * @return the serialized string
     */
    @NonNull
    public static String serialize(Collection<OutputRedirection> redirectionList) {
        StringBuilder sb = new StringBuilder();
        if (redirectionList != null) {
            for (OutputRedirection redirection : redirectionList) {
                if (!sb.isEmpty()) {
                    sb.append(" ");
                }
                sb.append(redirection.getOperator()).append(" ");
                sb.append(redirection.getOperand());
            }
        }
        return sb.toString();
    }

    /**
     * Creates a {@link PrintWriter} for the specified output redirections.
     * <p>If multiple redirections are provided, a {@link MultiWriter} is used to
     * write to all destinations simultaneously.</p>
     * @param redirectionList a list of the output redirections
     * @param console the shell console instance
     * @return a {@code PrintWriter} for the redirected output, or {@code null} if no redirections are given
     * @throws IOException if an I/O error occurs
     */
    @Nullable
    public static PrintWriter determineOutputWriter(List<OutputRedirection> redirectionList, ShellConsole console)
            throws IOException {
        Writer[] redirectionWriters = getRedirectionWriters(redirectionList, console);
        PrintWriter outputWriter = null;
        if (redirectionWriters != null) {
            if (redirectionWriters.length == 1) {
                outputWriter = new PrintWriter(redirectionWriters[0]);
            } else if (redirectionWriters.length > 1) {
                outputWriter = new PrintWriter(new MultiWriter(redirectionWriters));
            }
        }
        return outputWriter;
    }

    /**
     * Creates an array of {@link Writer} instances for the given redirections.
     * @param redirectionList the list of redirections
     * @param console the shell console, used for resolving relative paths
     * @return an array of writers, or {@code null} if the list is empty
     * @throws IOException if a file cannot be created
     */
    private static Writer[] getRedirectionWriters(List<OutputRedirection> redirectionList, ShellConsole console)
            throws IOException {
        if (redirectionList != null && !redirectionList.isEmpty()) {
            List<Writer> writers = new ArrayList<>(redirectionList.size());
            for (OutputRedirection redirection : redirectionList) {
                if (!StringUtils.hasText(redirection.getOperand())) {
                    throw new IllegalArgumentException("Redirect destination file not specified");
                }
                File file;
                Path path = Paths.get(redirection.getOperand());
                if (path.isAbsolute()) {
                    file = path.toFile();
                } else if (console.getWorkingDir() != null) {
                    file = new File(console.getWorkingDir(), redirection.getOperand());
                } else {
                    file = new File(redirection.getOperand());
                }
                if (file.getParentFile() != null) {
                    if (!file.getParentFile().mkdirs()) {
                        throw new IOException("Could not create directory for output file");
                    }
                }
                boolean append = (redirection.getOperator() == Operator.APPEND_OUT);
                OutputStream stream = new FileOutputStream(file, append);
                writers.add(new OutputStreamWriter(stream, console.getEncoding()));
            }
            return writers.toArray(new Writer[0]);
        } else {
            return null;
        }
    }

    /**
     * Defines the output redirection operators.
     */
    public enum Operator {

        /**
         * Overwrites the destination file with the command output.
         */
        OVERWRITE_OUT(">"), // >

        /**
         * Appends the command output to the end of the destination file.
         */
        APPEND_OUT(">>"); // >>

        private final String alias;

        Operator(String alias) {
            this.alias = alias;
        }

        @Override
        public String toString() {
            return this.alias;
        }

    }

    /**
     * A {@link Writer} that delegates its operations to an array of underlying writers.
     *
     * <p>Created: 2017. 3. 9.</p>
     */
    public static class MultiWriter extends Writer {

        private final Writer[] writers;

        public MultiWriter(Writer[] writers) {
            this.writers = writers;
        }

        @Override
        public void write(@NonNull char[] buf, int off, int len) throws IOException {
            for (Writer writer : writers) {
                writer.write(buf, off, len);
            }
        }

        @Override
        public void flush() throws IOException {
            for (Writer writer : writers) {
                try {
                    writer.flush();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        @Override
        public void close() throws IOException {
            int unclosed = 0;
            for (Writer writer : writers) {
                try {
                    writer.close();
                } catch (IOException e) {
                    unclosed++;
                }
            }
            if (unclosed > 0) {
                throw new IOException("Failed to close multi-writer");
            }
        }

    }

}
