package org.json;

import java.io.*;
import java.nio.charset.Charset;

/*
Public Domain.
 */
/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 * @author JSON.org
 * @version 2014-05-03
 */
public class JSONTokener {

    /**
     * current read character position on the current line.
     */
    private long character;

    /**
     * flag to indicate if the end of the input has been found.
     */
    private boolean eof;

    /**
     * current read index of the input.
     */
    private long index;

    /**
     * current line of the input.
     */
    private long line;

    /**
     * previous character read from the input.
     */
    private char previous;

    /**
     * Reader for the input.
     */
    private final Reader reader;

    /**
     * flag to indicate that a previous character was requested.
     */
    private boolean usePrevious;

    /**
     * the number of characters read in the previous line.
     */
    private long characterPreviousLine;

    // access to this object is required for strict mode checking
    private JSONParserConfiguration jsonParserConfiguration;

    /**
     * Construct a JSONTokener from a Reader. The caller must close the Reader.
     *
     * @param reader the source.
     */
    public JSONTokener(Reader reader) {
        this(reader, new JSONParserConfiguration());
    }

    /**
     * Construct a JSONTokener from a Reader with a given JSONParserConfiguration. The caller must close the Reader.
     *
     * @param reader the source.
     * @param jsonParserConfiguration A JSONParserConfiguration instance that controls the behavior of the parser.
     */
    public JSONTokener(Reader reader, JSONParserConfiguration jsonParserConfiguration) {
        this.jsonParserConfiguration = jsonParserConfiguration;
        this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
        this.eof = false;
        this.usePrevious = false;
        this.previous = 0;
        this.index = 0;
        this.character = 1;
        this.characterPreviousLine = 0;
        this.line = 1;
    }

    /**
     * Construct a JSONTokener from an InputStream. The caller must close the input stream.
     * @param inputStream The source.
     */
    public JSONTokener(InputStream inputStream) {
        this(inputStream, new JSONParserConfiguration());
    }

    /**
     * Construct a JSONTokener from an InputStream. The caller must close the input stream.
     * @param inputStream The source.
     * @param jsonParserConfiguration A JSONParserConfiguration instance that controls the behavior of the parser.
     */
    public JSONTokener(InputStream inputStream, JSONParserConfiguration jsonParserConfiguration) {
        this(new InputStreamReader(inputStream, Charset.forName("UTF-8")), jsonParserConfiguration);
    }

    /**
     * Construct a JSONTokener from a string.
     *
     * @param source A source string.
     */
    public JSONTokener(String source) {
        this(new StringReader(source));
    }

    /**
     * Construct a JSONTokener from an InputStream. The caller must close the input stream.
     * @param source The source.
     * @param jsonParserConfiguration A JSONParserConfiguration instance that controls the behavior of the parser.
     */
    public JSONTokener(String source, JSONParserConfiguration jsonParserConfiguration) {
        this(new StringReader(source), jsonParserConfiguration);
    }

    public JSONParserConfiguration getJsonParserConfiguration() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Setter
     * @param jsonParserConfiguration new value for jsonParserConfiguration
     *
     * @deprecated method should not be used
     */
    @Deprecated
    public void setJsonParserConfiguration(JSONParserConfiguration jsonParserConfiguration) {
        this.jsonParserConfiguration = jsonParserConfiguration;
    }

    public void back() throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Decrements the indexes for the {@link #back()} method based on the previous character read.
     */
    private void decrementIndexes() {
        this.index--;
        if (this.previous == '\r' || this.previous == '\n') {
            this.line--;
            this.character = this.characterPreviousLine;
        } else if (this.character > 0) {
            this.character--;
        }
    }

    public static int dehexchar(char c) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean end() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean more() throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public char next() throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected char getPrevious() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Increments the internal indexes according to the previous character
     * read and the character passed as the current character.
     * @param c the current character read.
     */
    private void incrementIndexes(int c) {
        if (c > 0) {
            this.index++;
            if (c == '\r') {
                this.line++;
                this.characterPreviousLine = this.character;
                this.character = 0;
            } else if (c == '\n') {
                if (this.previous != '\r') {
                    this.line++;
                    this.characterPreviousLine = this.character;
                }
                this.character = 0;
            } else {
                this.character++;
            }
        }
    }

    public char next(char c) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String next(int n) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public char nextClean() throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String nextString(char quote) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String nextTo(char delimiter) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String nextTo(String delimiters) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Object nextValue() throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    Object nextSimpleValue(char c) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public char skipTo(char to) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONException syntaxError(String message) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONException syntaxError(String message, Throwable causedBy) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void close() throws IOException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
