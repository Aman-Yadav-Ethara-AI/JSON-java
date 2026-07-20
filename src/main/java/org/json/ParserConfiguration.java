package org.json;

/*
Public Domain.
*/
/**
 * Configuration base object for parsers. The configuration is immutable.
 */
@SuppressWarnings({ "" })
public class ParserConfiguration {

    /**
     * Used to indicate there's no defined limit to the maximum nesting depth when parsing a document.
     */
    public static final int UNDEFINED_MAXIMUM_NESTING_DEPTH = -1;

    /**
     * Used to indicate there's no defined limit to the maximum number length
     */
    public static final int UNDEFINED_MAXIMUM_NUMBER_LENGTH = -1;

    /**
     * The default maximum nesting depth when parsing a document.
     */
    public static final int DEFAULT_MAXIMUM_NESTING_DEPTH = 512;

    /**
     * The default  max number length
     */
    public static final int DEFAULT_MAX_NUMBER_LENGTH = 1000;

    /**
     * Specifies if values should be kept as strings (<code>true</code>), or if
     * they should try to be guessed into JSON values (numeric, boolean, string).
     */
    protected boolean keepStrings;

    /**
     * The maximum nesting depth when parsing an object.
     */
    protected int maxNestingDepth;

    /**
     * The max number of chars for any number. Exceeding this limit will cause the value to be converted to a string
     */
    protected int maxNumberLength;

    /**
     * Constructs a new ParserConfiguration with default settings.
     */
    public ParserConfiguration() {
        this.keepStrings = false;
        this.maxNestingDepth = DEFAULT_MAXIMUM_NESTING_DEPTH;
        this.maxNumberLength = DEFAULT_MAX_NUMBER_LENGTH;
    }

    /**
     * Constructs a new ParserConfiguration with the specified settings. Use the with* methods instead of calling this ctor.
     *
     * @param keepStrings     A boolean indicating whether to preserve strings during parsing.
     * @param maxNestingDepth An integer representing the maximum allowed nesting depth.
     * @deprecated Use the with*() methods instead
     */
    @Deprecated
    protected ParserConfiguration(final boolean keepStrings, final int maxNestingDepth) {
        this.keepStrings = keepStrings;
        this.maxNestingDepth = maxNestingDepth;
        this.maxNumberLength = DEFAULT_MAX_NUMBER_LENGTH;
    }

    @Override
    protected ParserConfiguration clone() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isKeepStrings() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    public <T extends ParserConfiguration> T withKeepStrings(final boolean newVal) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int getMaxNestingDepth() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    public <T extends ParserConfiguration> T withMaxNestingDepth(int maxNestingDepth) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int getMaxNumberLength() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    public <T extends ParserConfiguration> T withMaxNumberLength(int maxNumberLength) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
