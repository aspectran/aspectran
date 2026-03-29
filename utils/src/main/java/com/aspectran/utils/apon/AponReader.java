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
package com.aspectran.utils.apon;

import com.aspectran.utils.Assert;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * A streaming reader that parses APON (Aspectran Parameters Object Notation) text
 * into {@link Parameters} containers.
 * <p>
 * This class supports reading from strings, files, and generic {@link Reader} instances.
 * The parser handles various APON features such as nested objects, arrays, value type hints,
 * multi-line text blocks, and comments.
 * </p>
 */
public class AponReader {

    private final BufferedReader reader;

    /**
     * Creates a new AponReader for the given APON-formatted string.
     * @param apon the APON formatted string
     */
    public AponReader(String apon) {
        this(new StringReader(apon));
    }

    /**
     * Creates a new AponReader for the given {@link Reader}.
     * @param reader the character stream to read from
     */
    public AponReader(Reader reader) {
        Assert.notNull(reader, "reader must not be null");
        if (reader instanceof BufferedReader bufferedReader) {
            this.reader = bufferedReader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    /**
     * Reads an APON document and populates a new {@link VariableParameters} instance.
     * @return a new {@code Parameters} object containing the parsed data
     * @throws AponParseException if an error occurs during parsing
     */
    public Parameters read() throws AponParseException {
        Parameters parameters = new VariableParameters();
        return read(parameters);
    }

    /**
     * Reads an APON document and populates the given {@link Parameters} object.
     * @param <T> the type of the parameters object
     * @param parameters the {@code Parameters} object to populate
     * @return the populated {@code Parameters} object
     * @throws AponParseException if an error occurs during parsing
     */
    public <T extends Parameters> T read(T parameters) throws AponParseException {
        Assert.notNull(parameters, "parameters must not be null");
        AponParser aponParser = new AponParser(reader);
        return aponParser.parse(parameters);
    }

    /**
     * Closes the reader.
     */
    public void close() {
        try {
            reader.close();
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * A static utility method that parses an APON-formatted string into a new {@link VariableParameters} object.
     * @param apon the APON-formatted string
     * @return a new {@code Parameters} object containing the parsed data
     * @throws AponParseException if an error occurs during parsing
     */
    public static Parameters read(String apon) throws AponParseException {
        Parameters parameters = new VariableParameters();
        return read(apon, parameters);
    }

    /**
     * A static utility method that parses an APON-formatted string into a new container of the given type.
     * @param <T> the type of the new container
     * @param apon the APON-formatted string
     * @param requiredType the concrete {@link Parameters} implementation to instantiate
     * @return a new, populated container instance
     * @throws AponParseException if parsing fails or the type cannot be instantiated
     */
    public static <T extends Parameters> T read(String apon, Class<T> requiredType) throws AponParseException {
        T parameters = ClassUtils.createInstance(requiredType);
        return read(apon, parameters);
    }

    /**
     * A static utility method that parses an APON-formatted string into a given {@link Parameters} object.
     * @param <T> the type of the parameters object
     * @param apon the APON-formatted string
     * @param parameters the {@code Parameters} object to populate
     * @return the populated {@code Parameters} object
     * @throws AponParseException if an error occurs during parsing
     */
    public static <T extends Parameters> T read(String apon, T parameters) throws AponParseException {
        Assert.notNull(apon, "apon must not be null");
        Assert.notNull(parameters, "parameters must not be null");
        if (StringUtils.isEmpty(apon)) {
            return parameters;
        }
        AponReader aponReader = new AponReader(apon);
        try {
            return aponReader.read(parameters);
        } finally {
            aponReader.close();
        }
    }

    /**
     * A static utility method that parses an APON-formatted file into a new {@link VariableParameters} object.
     * @param file the file to parse
     * @return a new {@code Parameters} object containing the parsed data
     * @throws AponParseException if an error occurs during parsing
     */
    public static Parameters read(File file) throws AponParseException {
        return read(file, (String)null);
    }

    /**
     * A static utility method that parses an APON-formatted file into a new {@link VariableParameters} object.
     * @param file the file to parse
     * @param encoding the character encoding of the file
     * @return a new {@code Parameters} object containing the parsed data
     * @throws AponParseException if an error occurs during parsing
     */
    public static Parameters read(File file, String encoding) throws AponParseException {
        Assert.notNull(file, "file must not be null");
        Parameters parameters = new VariableParameters();
        return read(file, encoding, parameters);
    }

    /**
     * A static utility method that parses an APON-formatted file into a given {@link Parameters} object.
     * @param <T> the type of the parameters object
     * @param file the file to parse
     * @param parameters the {@code Parameters} object to populate
     * @return the populated {@code Parameters} object
     * @throws AponParseException if an error occurs during parsing
     */
    public static <T extends Parameters> T read(File file, T parameters) throws AponParseException {
        return read(file, null, parameters);
    }

    /**
     * A static utility method that parses an APON-formatted file into a given {@link Parameters} object.
     * @param <T> the type of the parameters object
     * @param file the file to parse
     * @param encoding the character encoding of the file
     * @param parameters the {@code Parameters} object to populate
     * @return the populated {@code Parameters} object
     * @throws AponParseException if an error occurs during parsing
     */
    public static <T extends Parameters> T read(File file, String encoding, T parameters)
            throws AponParseException {
        Assert.notNull(file, "file must not be null");
        Assert.notNull(parameters, "parameters must not be null");
        AponReader aponReader = null;
        try {
            if (encoding == null) {
                aponReader = new AponReader(new FileReader(file));
            } else {
                aponReader = new AponReader(new InputStreamReader(new FileInputStream(file), encoding));
            }
            return aponReader.read(parameters);
        } catch (AponParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AponParseException("Failed to read APON from file: " + file, e);
        } finally {
            if (aponReader != null) {
                aponReader.close();
            }
        }
    }

    /**
     * A static utility method that parses an APON-formatted stream into a new {@link VariableParameters} object.
     * @param reader the character stream to read from
     * @return a new {@code Parameters} object containing the parsed data
     * @throws AponParseException if an error occurs during parsing
     */
    public static Parameters read(Reader reader) throws AponParseException {
        Assert.notNull(reader, "reader must not be null");
        try (AponReaderCloseable aponReader = new AponReaderCloseable(reader)) {
            return aponReader.read();
        }
    }

    /**
     * A static utility method that parses an APON-formatted stream into a given {@link Parameters} object.
     * @param <T> the type of the parameters object
     * @param reader the character stream to read from
     * @param parameters the {@code Parameters} object to populate
     * @return the populated {@code Parameters} object
     * @throws AponParseException if an error occurs during parsing
     */
    public static <T extends Parameters> T read(Reader reader, T parameters) throws AponParseException {
        try (AponReaderCloseable aponReader = new AponReaderCloseable(reader)) {
            return aponReader.read(parameters);
        }
    }

}
