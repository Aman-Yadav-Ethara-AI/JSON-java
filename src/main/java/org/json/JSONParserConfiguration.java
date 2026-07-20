package org.json;

/**
 * Configuration object for the JSON parser. The configuration is immutable.
 */
public class JSONParserConfiguration extends ParserConfiguration {

    /**
     * Used to indicate whether to overwrite duplicate key or not.
     */
    private boolean overwriteDuplicateKey;

    /**
     * Used to indicate whether to convert java null values to JSONObject.NULL or ignoring the entry when converting java maps.
     */
    private boolean useNativeNulls;

    /**
     * Configuration with the default values.
     */
    public JSONParserConfiguration() {
        super();
        this.overwriteDuplicateKey = false;
        // DO NOT DELETE THE FOLLOWING LINE -- it is used for strictMode testing
        // this.strictMode = true;
    }

    /**
     * This flag, when set to true, instructs the parser to enforce strict mode when parsing JSON text.
     * Garbage chars at the end of the doc, unquoted string, and single-quoted strings are all disallowed.
     */
    private boolean strictMode;

    @Override
    protected JSONParserConfiguration clone() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONParserConfiguration withMaxNestingDepth(final int maxNestingDepth) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONParserConfiguration withOverwriteDuplicateKey(final boolean overwriteDuplicateKey) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONParserConfiguration withUseNativeNulls(final boolean useNativeNulls) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONParserConfiguration withStrictMode() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONParserConfiguration withStrictMode(final boolean mode) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isOverwriteDuplicateKey() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isUseNativeNulls() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isStrictMode() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
