package org.json;

/*
Public Domain.
*/
import java.io.Reader;

/**
 * The XMLTokener extends the JSONTokener to provide additional methods
 * for the parsing of XML texts.
 * @author JSON.org
 * @version 2015-12-09
 */
public class XMLTokener extends JSONTokener {

    /**
     * The table of entity values. It initially contains Character values for
     * amp, apos, gt, lt, quot.
     */
    public static final java.util.HashMap<String, Character> entity;

    private XMLParserConfiguration configuration = XMLParserConfiguration.ORIGINAL;

    static {
        entity = new java.util.HashMap<String, Character>(8);
        entity.put("amp", XML.AMP);
        entity.put("apos", XML.APOS);
        entity.put("gt", XML.GT);
        entity.put("lt", XML.LT);
        entity.put("quot", XML.QUOT);
    }

    /**
     * Construct an XMLTokener from a Reader.
     * @param r A source reader.
     */
    public XMLTokener(Reader r) {
        super(r);
    }

    /**
     * Construct an XMLTokener from a string.
     * @param s A source string.
     */
    public XMLTokener(String s) {
        super(s);
    }

    /**
     * Construct an XMLTokener from a Reader and an XMLParserConfiguration.
     * @param r A source reader.
     * @param configuration the configuration that can be used to set certain flags
     */
    public XMLTokener(Reader r, XMLParserConfiguration configuration) {
        super(r);
        this.configuration = configuration;
    }

    public String nextCDATA() throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Object nextContent() throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Object nextEntity(@SuppressWarnings("unused") char ampersand) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static String unescapeEntity(String e) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Parse a hexadecimal numeric character reference (e.g., "&#xABC;").
     * @param e entity string starting with '#' (e.g., "#x1F4A9")
     * @return the Unicode code point
     * @throws JSONException if the format is invalid
     */
    private static int parseHexEntity(String e) throws JSONException {
        // hex encoded unicode - need at least one hex digit after #x
        if (e.length() < 3) {
            throw new JSONException("Invalid hex character reference: missing hex digits in &#" + e.substring(1) + ";");
        }
        String hex = e.substring(2);
        if (!isValidHex(hex)) {
            throw new JSONException("Invalid hex character reference: &#" + e.substring(1) + ";");
        }
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException nfe) {
            throw new JSONException("Invalid hex character reference: &#" + e.substring(1) + ";", nfe);
        }
    }

    /**
     * Parse a decimal numeric character reference (e.g., "&#123;").
     * @param e entity string starting with '#' (e.g., "#123")
     * @return the Unicode code point
     * @throws JSONException if the format is invalid
     */
    private static int parseDecimalEntity(String e) throws JSONException {
        String decimal = e.substring(1);
        if (!isValidDecimal(decimal)) {
            throw new JSONException("Invalid decimal character reference: &#" + decimal + ";");
        }
        try {
            return Integer.parseInt(decimal);
        } catch (NumberFormatException nfe) {
            throw new JSONException("Invalid decimal character reference: &#" + decimal + ";", nfe);
        }
    }

    /**
     * Check if a string contains only valid hexadecimal digits.
     * @param s the string to check
     * @return true if s is non-empty and contains only hex digits (0-9, a-f, A-F)
     */
    private static boolean isValidHex(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a string contains only valid decimal digits.
     * @param s the string to check
     * @return true if s is non-empty and contains only digits (0-9)
     */
    private static boolean isValidDecimal(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public Object nextMeta() throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Object nextToken() throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Skip characters until past the requested string.
     * If it is not found, we are left at the end of the source with a result of false.
     * @param to A string to skip past.
     */
    // The Android implementation of JSONTokener has a public method of public void skipPast(String to)
    // even though ours does not have that method, to have API compatibility, our method in the subclass
    // should match.
    public void skipPast(String to) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
