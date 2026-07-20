package org.json;

import java.io.IOException;
import java.io.Writer;

/**
 * Performance optimised alternative for {@link java.io.StringWriter}
 * using internally a {@link StringBuilder} instead of a {@link StringBuffer}.
 */
public class StringBuilderWriter extends Writer {

    private final StringBuilder builder;

    /**
     * Create a new string builder writer using the default initial string-builder buffer size.
     */
    public StringBuilderWriter() {
        builder = new StringBuilder();
        lock = builder;
    }

    /**
     * Create a new string builder writer using the specified initial string-builder buffer size.
     *
     * @param initialSize The number of {@code char} values that will fit into this buffer
     *                    before it is automatically expanded
     *
     * @throws IllegalArgumentException If {@code initialSize} is negative
     */
    public StringBuilderWriter(int initialSize) {
        builder = new StringBuilder(initialSize);
        lock = builder;
    }

    @Override
    public void write(int c) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void write(char[] cbuf, int offset, int length) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void write(String str) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void write(String str, int offset, int length) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public StringBuilderWriter append(CharSequence csq) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public StringBuilderWriter append(CharSequence csq, int start, int end) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public StringBuilderWriter append(char c) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
