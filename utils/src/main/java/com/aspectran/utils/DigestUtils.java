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
package com.aspectran.utils;

import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Miscellaneous methods for calculating digests (MD5, SHA-1, SHA-256, SHA-512).
 *
 * <p>Mainly for internal use within the framework; consider
 * <a href="https://commons.apache.org/codec/">Apache Commons Codec</a>
 * for a more comprehensive suite of digest utilities.</p>
 */
@NullMarked
public class DigestUtils {

    public static final String MD5_ALGORITHM_NAME = "MD5";

    public static final String SHA_1_ALGORITHM_NAME = "SHA-1";

    public static final String SHA_256_ALGORITHM_NAME = "SHA-256";

    public static final String SHA_512_ALGORITHM_NAME = "SHA-512";

    private static final char[] HEX_CHARS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final int BUFFER_SIZE = 4096;

    /**
     * This class cannot be instantiated.
     */
    private DigestUtils() {
    }

    // --- MD5 ---

    /**
     * Calculate the MD5 digest of the given bytes.
     * @param bytes the bytes to calculate the digest over
     * @return the digest
     */
    public static byte[] md5Digest(byte[] bytes) {
        return digest(MD5_ALGORITHM_NAME, bytes);
    }

    /**
     * Calculate the MD5 digest of the given string using UTF-8 encoding.
     * @param text the string to calculate the digest over
     * @return the digest
     */
    public static byte[] md5Digest(String text) {
        return md5Digest(text, StandardCharsets.UTF_8);
    }

    /**
     * Calculate the MD5 digest of the given string using the specified charset.
     * @param text the string to calculate the digest over
     * @param charset the charset to use
     * @return the digest
     */
    public static byte[] md5Digest(String text, Charset charset) {
        return digest(MD5_ALGORITHM_NAME, text.getBytes(charset));
    }

    /**
     * Calculate the MD5 digest of the given stream.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param inputStream the InputStream to calculate the digest over
     * @return the digest
     * @throws IOException if an I/O error occurs
     */
    public static byte[] md5Digest(InputStream inputStream) throws IOException {
        return digest(MD5_ALGORITHM_NAME, inputStream);
    }

    /**
     * Return a hexadecimal string representation of the MD5 digest of the given bytes.
     * @param bytes the bytes to calculate the digest over
     * @return a hexadecimal digest string
     */
    public static String md5DigestAsHex(byte[] bytes) {
        return digestAsHexString(MD5_ALGORITHM_NAME, bytes);
    }

    /**
     * Return a hexadecimal string representation of the MD5 digest of the given string using UTF-8 encoding.
     * @param text the string to calculate the digest over
     * @return a hexadecimal digest string
     */
    public static String md5DigestAsHex(String text) {
        return md5DigestAsHex(text, StandardCharsets.UTF_8);
    }

    /**
     * Return a hexadecimal string representation of the MD5 digest of the given string using the specified charset.
     * @param text the string to calculate the digest over
     * @param charset the charset to use
     * @return a hexadecimal digest string
     */
    public static String md5DigestAsHex(String text, Charset charset) {
        return digestAsHexString(MD5_ALGORITHM_NAME, text.getBytes(charset));
    }

    /**
     * Return a hexadecimal string representation of the MD5 digest of the given stream.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param inputStream the InputStream to calculate the digest over
     * @return a hexadecimal digest string
     * @throws IOException if an I/O error occurs
     */
    public static String md5DigestAsHex(InputStream inputStream) throws IOException {
        return digestAsHexString(MD5_ALGORITHM_NAME, inputStream);
    }

    /**
     * Append a hexadecimal string representation of the MD5 digest of the given
     * bytes to the given {@link StringBuilder}.
     * @param bytes the bytes to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     */
    public static StringBuilder appendMd5DigestAsHex(byte[] bytes, StringBuilder builder) {
        return appendDigestAsHex(MD5_ALGORITHM_NAME, bytes, builder);
    }

    /**
     * Append a hexadecimal string representation of the MD5 digest of the given
     * inputStream to the given {@link StringBuilder}.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param inputStream the inputStream to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     * @throws IOException if an I/O error occurs
     */
    public static StringBuilder appendMd5DigestAsHex(InputStream inputStream, StringBuilder builder) throws IOException {
        return appendDigestAsHex(MD5_ALGORITHM_NAME, inputStream, builder);
    }

    // --- SHA-1 ---

    /**
     * Calculate the SHA-1 digest of the given bytes.
     * @param bytes the bytes to calculate the digest over
     * @return the digest
     */
    public static byte[] sha1Digest(byte[] bytes) {
        return digest(SHA_1_ALGORITHM_NAME, bytes);
    }

    /**
     * Calculate the SHA-1 digest of the given string using UTF-8 encoding.
     * @param text the string to calculate the digest over
     * @return the digest
     */
    public static byte[] sha1Digest(String text) {
        return sha1Digest(text, StandardCharsets.UTF_8);
    }

    /**
     * Calculate the SHA-1 digest of the given string using the specified charset.
     * @param text the string to calculate the digest over
     * @param charset the charset to use
     * @return the digest
     */
    public static byte[] sha1Digest(String text, Charset charset) {
        return digest(SHA_1_ALGORITHM_NAME, text.getBytes(charset));
    }

    /**
     * Calculate the SHA-1 digest of the given stream.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param inputStream the InputStream to calculate the digest over
     * @return the digest
     * @throws IOException if an I/O error occurs
     */
    public static byte[] sha1Digest(InputStream inputStream) throws IOException {
        return digest(SHA_1_ALGORITHM_NAME, inputStream);
    }

    /**
     * Return a hexadecimal string representation of the SHA-1 digest of the given bytes.
     * @param bytes the bytes to calculate the digest over
     * @return a hexadecimal digest string
     */
    public static String sha1DigestAsHex(byte[] bytes) {
        return digestAsHexString(SHA_1_ALGORITHM_NAME, bytes);
    }

    /**
     * Return a hexadecimal string representation of the SHA-1 digest of the given string using UTF-8 encoding.
     * @param text the string to calculate the digest over
     * @return a hexadecimal digest string
     */
    public static String sha1DigestAsHex(String text) {
        return sha1DigestAsHex(text, StandardCharsets.UTF_8);
    }

    /**
     * Return a hexadecimal string representation of the SHA-1 digest of the given string using the specified charset.
     * @param text the string to calculate the digest over
     * @param charset the charset to use
     * @return a hexadecimal digest string
     */
    public static String sha1DigestAsHex(String text, Charset charset) {
        return digestAsHexString(SHA_1_ALGORITHM_NAME, text.getBytes(charset));
    }

    /**
     * Return a hexadecimal string representation of the SHA-1 digest of the given stream.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param inputStream the InputStream to calculate the digest over
     * @return a hexadecimal digest string
     * @throws IOException if an I/O error occurs
     */
    public static String sha1DigestAsHex(InputStream inputStream) throws IOException {
        return digestAsHexString(SHA_1_ALGORITHM_NAME, inputStream);
    }

    /**
     * Append a hexadecimal string representation of the SHA-1 digest of the given
     * bytes to the given {@link StringBuilder}.
     * @param bytes the bytes to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     */
    public static StringBuilder appendSha1DigestAsHex(byte[] bytes, StringBuilder builder) {
        return appendDigestAsHex(SHA_1_ALGORITHM_NAME, bytes, builder);
    }

    /**
     * Append a hexadecimal string representation of the SHA-1 digest of the given
     * inputStream to the given {@link StringBuilder}.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param inputStream the inputStream to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     * @throws IOException if an I/O error occurs
     */
    public static StringBuilder appendSha1DigestAsHex(InputStream inputStream, StringBuilder builder) throws IOException {
        return appendDigestAsHex(SHA_1_ALGORITHM_NAME, inputStream, builder);
    }

    // --- SHA-256 ---

    /**
     * Calculate the SHA-256 digest of the given bytes.
     * @param bytes the bytes to calculate the digest over
     * @return the digest
     */
    public static byte[] sha256Digest(byte[] bytes) {
        return digest(SHA_256_ALGORITHM_NAME, bytes);
    }

    /**
     * Calculate the SHA-256 digest of the given string using UTF-8 encoding.
     * @param text the string to calculate the digest over
     * @return the digest
     */
    public static byte[] sha256Digest(String text) {
        return sha256Digest(text, StandardCharsets.UTF_8);
    }

    /**
     * Calculate the SHA-256 digest of the given string using the specified charset.
     * @param text the string to calculate the digest over
     * @param charset the charset to use
     * @return the digest
     */
    public static byte[] sha256Digest(String text, Charset charset) {
        return digest(SHA_256_ALGORITHM_NAME, text.getBytes(charset));
    }

    /**
     * Calculate the SHA-256 digest of the given stream.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param inputStream the InputStream to calculate the digest over
     * @return the digest
     * @throws IOException if an I/O error occurs
     */
    public static byte[] sha256Digest(InputStream inputStream) throws IOException {
        return digest(SHA_256_ALGORITHM_NAME, inputStream);
    }

    /**
     * Return a hexadecimal string representation of the SHA-256 digest of the given bytes.
     * @param bytes the bytes to calculate the digest over
     * @return a hexadecimal digest string
     */
    public static String sha256DigestAsHex(byte[] bytes) {
        return digestAsHexString(SHA_256_ALGORITHM_NAME, bytes);
    }

    /**
     * Return a hexadecimal string representation of the SHA-256 digest of the given string using UTF-8 encoding.
     * @param text the string to calculate the digest over
     * @return a hexadecimal digest string
     */
    public static String sha256DigestAsHex(String text) {
        return sha256DigestAsHex(text, StandardCharsets.UTF_8);
    }

    /**
     * Return a hexadecimal string representation of the SHA-256 digest of the given string using the specified charset.
     * @param text the string to calculate the digest over
     * @param charset the charset to use
     * @return a hexadecimal digest string
     */
    public static String sha256DigestAsHex(String text, Charset charset) {
        return digestAsHexString(SHA_256_ALGORITHM_NAME, text.getBytes(charset));
    }

    /**
     * Return a hexadecimal string representation of the SHA-256 digest of the given stream.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param inputStream the InputStream to calculate the digest over
     * @return a hexadecimal digest string
     * @throws IOException if an I/O error occurs
     */
    public static String sha256DigestAsHex(InputStream inputStream) throws IOException {
        return digestAsHexString(SHA_256_ALGORITHM_NAME, inputStream);
    }

    /**
     * Append a hexadecimal string representation of the SHA-256 digest of the given
     * bytes to the given {@link StringBuilder}.
     * @param bytes the bytes to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     */
    public static StringBuilder appendSha256DigestAsHex(byte[] bytes, StringBuilder builder) {
        return appendDigestAsHex(SHA_256_ALGORITHM_NAME, bytes, builder);
    }

    /**
     * Append a hexadecimal string representation of the SHA-256 digest of the given
     * inputStream to the given {@link StringBuilder}.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param inputStream the inputStream to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     * @throws IOException if an I/O error occurs
     */
    public static StringBuilder appendSha256DigestAsHex(InputStream inputStream, StringBuilder builder) throws IOException {
        return appendDigestAsHex(SHA_256_ALGORITHM_NAME, inputStream, builder);
    }

    // --- SHA-512 ---

    /**
     * Calculate the SHA-512 digest of the given bytes.
     * @param bytes the bytes to calculate the digest over
     * @return the digest
     */
    public static byte[] sha512Digest(byte[] bytes) {
        return digest(SHA_512_ALGORITHM_NAME, bytes);
    }

    /**
     * Calculate the SHA-512 digest of the given string using UTF-8 encoding.
     * @param text the string to calculate the digest over
     * @return the digest
     */
    public static byte[] sha512Digest(String text) {
        return sha512Digest(text, StandardCharsets.UTF_8);
    }

    /**
     * Calculate the SHA-512 digest of the given string using the specified charset.
     * @param text the string to calculate the digest over
     * @param charset the charset to use
     * @return the digest
     */
    public static byte[] sha512Digest(String text, Charset charset) {
        return digest(SHA_512_ALGORITHM_NAME, text.getBytes(charset));
    }

    /**
     * Calculate the SHA-512 digest of the given stream.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param inputStream the InputStream to calculate the digest over
     * @return the digest
     * @throws IOException if an I/O error occurs
     */
    public static byte[] sha512Digest(InputStream inputStream) throws IOException {
        return digest(SHA_512_ALGORITHM_NAME, inputStream);
    }

    /**
     * Return a hexadecimal string representation of the SHA-512 digest of the given bytes.
     * @param bytes the bytes to calculate the digest over
     * @return a hexadecimal digest string
     */
    public static String sha512DigestAsHex(byte[] bytes) {
        return digestAsHexString(SHA_512_ALGORITHM_NAME, bytes);
    }

    /**
     * Return a hexadecimal string representation of the SHA-512 digest of the given string using UTF-8 encoding.
     * @param text the string to calculate the digest over
     * @return a hexadecimal digest string
     */
    public static String sha512DigestAsHex(String text) {
        return sha512DigestAsHex(text, StandardCharsets.UTF_8);
    }

    /**
     * Return a hexadecimal string representation of the SHA-512 digest of the given string using the specified charset.
     * @param text the string to calculate the digest over
     * @param charset the charset to use
     * @return a hexadecimal digest string
     */
    public static String sha512DigestAsHex(String text, Charset charset) {
        return digestAsHexString(SHA_512_ALGORITHM_NAME, text.getBytes(charset));
    }

    /**
     * Return a hexadecimal string representation of the SHA-512 digest of the given stream.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param inputStream the InputStream to calculate the digest over
     * @return a hexadecimal digest string
     * @throws IOException if an I/O error occurs
     */
    public static String sha512DigestAsHex(InputStream inputStream) throws IOException {
        return digestAsHexString(SHA_512_ALGORITHM_NAME, inputStream);
    }

    /**
     * Append a hexadecimal string representation of the SHA-512 digest of the given
     * bytes to the given {@link StringBuilder}.
     * @param bytes the bytes to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     */
    public static StringBuilder appendSha512DigestAsHex(byte[] bytes, StringBuilder builder) {
        return appendDigestAsHex(SHA_512_ALGORITHM_NAME, bytes, builder);
    }

    /**
     * Append a hexadecimal string representation of the SHA-512 digest of the given
     * inputStream to the given {@link StringBuilder}.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param inputStream the inputStream to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     * @throws IOException if an I/O error occurs
     */
    public static StringBuilder appendSha512DigestAsHex(InputStream inputStream, StringBuilder builder) throws IOException {
        return appendDigestAsHex(SHA_512_ALGORITHM_NAME, inputStream, builder);
    }

    // --- Generic Digest Methods ---

    /**
     * Calculate the digest of the given bytes using the specified algorithm.
     * @param algorithm the digest algorithm to use
     * @param bytes the bytes to calculate the digest over
     * @return the digest
     */
    public static byte[] digest(String algorithm, byte[] bytes) {
        Assert.notNull(algorithm, "algorithm must not be null");
        Assert.notNull(bytes, "bytes must not be null");
        return getDigest(algorithm).digest(bytes);
    }

    /**
     * Calculate the digest of the given stream using the specified algorithm.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param algorithm the digest algorithm to use
     * @param inputStream the InputStream to calculate the digest over
     * @return the digest
     * @throws IOException if an I/O error occurs
     */
    public static byte[] digest(String algorithm, InputStream inputStream) throws IOException {
        Assert.notNull(algorithm, "algorithm must not be null");
        Assert.notNull(inputStream, "inputStream must not be null");
        MessageDigest messageDigest = getDigest(algorithm);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            messageDigest.update(buffer, 0, bytesRead);
        }
        return messageDigest.digest();
    }

    /**
     * Return a hexadecimal string representation of the digest of the given bytes.
     * @param algorithm the digest algorithm to use
     * @param bytes the bytes to calculate the digest over
     * @return a hexadecimal digest string
     */
    public static String digestAsHexString(String algorithm, byte[] bytes) {
        char[] hexDigest = digestAsHexChars(algorithm, bytes);
        return new String(hexDigest);
    }

    /**
     * Return a hexadecimal string representation of the digest of the given stream.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param algorithm the digest algorithm to use
     * @param inputStream the InputStream to calculate the digest over
     * @return a hexadecimal digest string
     * @throws IOException if an I/O error occurs
     */
    public static String digestAsHexString(String algorithm, InputStream inputStream) throws IOException {
        char[] hexDigest = digestAsHexChars(algorithm, inputStream);
        return new String(hexDigest);
    }

    /**
     * Append a hexadecimal string representation of the digest of the given bytes to the given {@link StringBuilder}.
     * @param algorithm the digest algorithm to use
     * @param bytes the bytes to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     */
    public static StringBuilder appendDigestAsHex(String algorithm, byte[] bytes, StringBuilder builder) {
        Assert.notNull(builder, "builder must not be null");
        char[] hexDigest = digestAsHexChars(algorithm, bytes);
        return builder.append(hexDigest);
    }

    /**
     * Append a hexadecimal string representation of the digest of the given stream to the given {@link StringBuilder}.
     * <p>This method does <strong>not</strong> close the input stream.</p>
     * @param algorithm the digest algorithm to use
     * @param inputStream the inputStream to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     * @throws IOException if an I/O error occurs
     */
    public static StringBuilder appendDigestAsHex(
            String algorithm, InputStream inputStream, StringBuilder builder) throws IOException {
        Assert.notNull(builder, "builder must not be null");
        char[] hexDigest = digestAsHexChars(algorithm, inputStream);
        return builder.append(hexDigest);
    }

    /**
     * Encodes a byte array into a hexadecimal character array.
     * @param bytes the bytes to encode
     * @return a char array containing hexadecimal characters
     */
    public static char[] encodeHex(byte[] bytes) {
        Assert.notNull(bytes, "bytes must not be null");
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            chars[i * 2] = HEX_CHARS[(bytes[i] >>> 4) & 0x0f];
            chars[i * 2 + 1] = HEX_CHARS[bytes[i] & 0x0f];
        }
        return chars;
    }

    /**
     * Encodes a byte array into a hexadecimal string.
     * @param bytes the bytes to encode
     * @return a string containing hexadecimal characters
     */
    public static String encodeHexString(byte[] bytes) {
        return new String(encodeHex(bytes));
    }

    /**
     * Create a new {@link MessageDigest} with the given algorithm.
     * <p>Necessary because {@code MessageDigest} is not thread-safe.</p>
     */
    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Could not find MessageDigest with algorithm \"" + algorithm + "\"", ex);
        }
    }

    private static char[] digestAsHexChars(String algorithm, byte[] bytes) {
        byte[] digest = digest(algorithm, bytes);
        return encodeHex(digest);
    }

    private static char[] digestAsHexChars(String algorithm, InputStream inputStream) throws IOException {
        byte[] digest = digest(algorithm, inputStream);
        return encodeHex(digest);
    }

}
