package org.json;

/*
Public Domain.
*/
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration object for the XML parser. The configuration is immutable.
 * @author AylwardJ
 */
@SuppressWarnings({ "" })
public class XMLParserConfiguration extends ParserConfiguration {

    /**
     * The default maximum nesting depth when parsing a XML document to JSON.
     */
    //    public static final int DEFAULT_MAXIMUM_NESTING_DEPTH = 512; // We could override
    /**
     * Allow user to control how numbers are parsed
     */
    private boolean keepNumberAsString;

    /**
     * Allow user to control how booleans are parsed
     */
    private boolean keepBooleanAsString;

    /**
     * Original Configuration of the XML Parser.
     */
    public static final XMLParserConfiguration ORIGINAL = new XMLParserConfiguration();

    /**
     * Original configuration of the XML Parser except that values are kept as strings.
     */
    public static final XMLParserConfiguration KEEP_STRINGS = new XMLParserConfiguration().withKeepStrings(true);

    /**
     * The name of the key in a JSON Object that indicates a CDATA section. Historically this has
     * been the value "content" but can be changed. Use <code>null</code> to indicate no CDATA
     * processing.
     */
    private String cDataTagName;

    /**
     * When parsing the XML into JSON, specifies if values with attribute xsi:nil="true"
     * should be kept as attribute(<code>false</code>), or they should be converted to
     * <code>null</code>(<code>true</code>)
     */
    private boolean convertNilAttributeToNull;

    /**
     * When creating an XML from JSON Object, an empty tag by default will self-close.
     * If it has to be closed explicitly, with empty content between start and end tag,
     * this flag is to be turned on.
     */
    private boolean closeEmptyTag;

    /**
     * This will allow type conversion for values in XML if xsi:type attribute is defined
     */
    private Map<String, XMLXsiTypeConverter<?>> xsiTypeMap;

    /**
     * When parsing the XML into JSON, specifies the tags whose values should be converted
     * to arrays
     */
    private Set<String> forceList;

    /**
     * Flag to indicate whether white space should be trimmed when parsing XML.
     * The default behaviour is to trim white space. When this is set to false, inputting XML
     * with tags that are the same as the value of cDataTagName is unsupported. It is recommended to set cDataTagName
     * to a distinct value in this case.
     */
    private boolean shouldTrimWhiteSpace;

    /**
     * Default parser configuration. Does not keep strings (tries to implicitly convert
     * values), and the CDATA Tag Name is "content". Trims whitespace.
     */
    public XMLParserConfiguration() {
        super();
        this.cDataTagName = "content";
        this.convertNilAttributeToNull = false;
        this.xsiTypeMap = Collections.emptyMap();
        this.forceList = Collections.emptySet();
        this.shouldTrimWhiteSpace = true;
    }

    /**
     * Configure the parser string processing and use the default CDATA Tag Name as "content".
     * @param keepStrings <code>true</code> to parse all values as string.
     *      <code>false</code> to try and convert XML string values into a JSON value.
     * @deprecated This constructor has been deprecated in favor of using the new builder
     *      pattern for the configuration.
     *      This constructor may be removed in a future release.
     */
    @Deprecated
    public XMLParserConfiguration(final boolean keepStrings) {
        this(keepStrings, "content", false);
    }

    /**
     * Configure the parser string processing to try and convert XML values to JSON values and
     * use the passed CDATA Tag Name the processing value. Pass <code>null</code> to
     * disable CDATA processing
     * @param cDataTagName <code>null</code> to disable CDATA processing. Any other value
     *      to use that value as the JSONObject key name to process as CDATA.
     * @deprecated This constructor has been deprecated in favor of using the new builder
     *      pattern for the configuration.
     *      This constructor may be removed in a future release.
     */
    @Deprecated
    public XMLParserConfiguration(final String cDataTagName) {
        this(false, cDataTagName, false);
    }

    /**
     * Configure the parser to use custom settings.
     * @param keepStrings <code>true</code> to parse all values as string.
     *      <code>false</code> to try and convert XML string values into a JSON value.
     * @param cDataTagName <code>null</code> to disable CDATA processing. Any other value
     *      to use that value as the JSONObject key name to process as CDATA.
     * @deprecated This constructor has been deprecated in favor of using the new builder
     *      pattern for the configuration.
     *      This constructor may be removed in a future release.
     */
    @Deprecated
    public XMLParserConfiguration(final boolean keepStrings, final String cDataTagName) {
        super(keepStrings, DEFAULT_MAXIMUM_NESTING_DEPTH);
        this.cDataTagName = cDataTagName;
        this.convertNilAttributeToNull = false;
    }

    /**
     * Configure the parser to use custom settings.
     * @param keepStrings <code>true</code> to parse all values as string.
     *      <code>false</code> to try and convert XML string values into a JSON value.
     * @param cDataTagName <code>null</code> to disable CDATA processing. Any other value
     *      to use that value as the JSONObject key name to process as CDATA.
     * @param convertNilAttributeToNull <code>true</code> to parse values with attribute xsi:nil="true" as null.
     *                                  <code>false</code> to parse values with attribute xsi:nil="true" as {"xsi:nil":true}.
     * @deprecated This constructor has been deprecated in favor of using the new builder
     *      pattern for the configuration.
     *      This constructor may be removed or marked private in a future release.
     */
    @Deprecated
    public XMLParserConfiguration(final boolean keepStrings, final String cDataTagName, final boolean convertNilAttributeToNull) {
        super(false, DEFAULT_MAXIMUM_NESTING_DEPTH);
        this.keepNumberAsString = keepStrings;
        this.keepBooleanAsString = keepStrings;
        this.cDataTagName = cDataTagName;
        this.convertNilAttributeToNull = convertNilAttributeToNull;
    }

    /**
     * Configure the parser to use custom settings.
     * @param keepStrings <code>true</code> to parse all values as string.
     *      <code>false</code> to try and convert XML string values into a JSON value.
     * @param cDataTagName <code>null</code> to disable CDATA processing. Any other value
     *      to use that value as the JSONObject key name to process as CDATA.
     * @param convertNilAttributeToNull <code>true</code> to parse values with attribute xsi:nil="true" as null.
     *                                  <code>false</code> to parse values with attribute xsi:nil="true" as {"xsi:nil":true}.
     * @param xsiTypeMap  <code>new HashMap<String, XMLXsiTypeConverter<?>>()</code> to parse values with attribute
     *                   xsi:type="integer" as integer,  xsi:type="string" as string
     * @param forceList  <code>new HashSet<String>()</code> to parse the provided tags' values as arrays
     * @param maxNestingDepth <code>int</code> to limit the nesting depth
     * @param closeEmptyTag <code>boolean</code> to turn on explicit end tag for tag with empty value
     */
    private XMLParserConfiguration(final boolean keepStrings, final String cDataTagName, final boolean convertNilAttributeToNull, final Map<String, XMLXsiTypeConverter<?>> xsiTypeMap, final Set<String> forceList, final int maxNestingDepth, final boolean closeEmptyTag, final boolean keepNumberAsString, final boolean keepBooleanAsString) {
        super(false, maxNestingDepth);
        this.keepNumberAsString = keepNumberAsString;
        this.keepBooleanAsString = keepBooleanAsString;
        this.cDataTagName = cDataTagName;
        this.convertNilAttributeToNull = convertNilAttributeToNull;
        this.xsiTypeMap = Collections.unmodifiableMap(xsiTypeMap);
        this.forceList = Collections.unmodifiableSet(forceList);
        this.closeEmptyTag = closeEmptyTag;
    }

    @Override
    protected XMLParserConfiguration clone() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    @Override
    public XMLParserConfiguration withKeepStrings(final boolean newVal) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public XMLParserConfiguration withKeepNumberAsString(final boolean newVal) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public XMLParserConfiguration withKeepBooleanAsString(final boolean newVal) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String getcDataTagName() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isKeepNumberAsString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isKeepBooleanAsString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public XMLParserConfiguration withcDataTagName(final String newVal) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isConvertNilAttributeToNull() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public XMLParserConfiguration withConvertNilAttributeToNull(final boolean newVal) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Map<String, XMLXsiTypeConverter<?>> getXsiTypeMap() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public XMLParserConfiguration withXsiTypeMap(final Map<String, XMLXsiTypeConverter<?>> xsiTypeMap) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Set<String> getForceList() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public XMLParserConfiguration withForceList(final Set<String> forceList) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    @Override
    public XMLParserConfiguration withMaxNestingDepth(int maxNestingDepth) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public XMLParserConfiguration withCloseEmptyTag(boolean closeEmptyTag) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public XMLParserConfiguration withShouldTrimWhitespace(boolean shouldTrimWhiteSpace) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isCloseEmptyTag() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean shouldTrimWhiteSpace() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
