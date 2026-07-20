package org.json;

/*
Public Domain.
*/
/**
 * This provides static methods to convert an XML text into a JSONArray or
 * JSONObject, and to covert a JSONArray or JSONObject into an XML text using
 * the JsonML transform.
 *
 * @author JSON.org
 * @version 2016-01-30
 */
public class JSONML {

    /**
     * Constructs a new JSONML object.
     * @deprecated (Utility class cannot be instantiated)
     */
    @Deprecated
    public JSONML() {
    }

    /**
     * Safely cast parse result to JSONArray with proper type checking.
     * @param result The result from parse() method
     * @return JSONArray if result is a JSONArray
     * @throws JSONException if result is not a JSONArray
     */
    private static JSONArray toJSONArraySafe(Object result) throws JSONException {
        if (result instanceof JSONArray) {
            return (JSONArray) result;
        }
        throw new JSONException("Expected JSONArray but got " + (result == null ? "null" : result.getClass().getSimpleName()));
    }

    /**
     * Safely cast parse result to JSONObject with proper type checking.
     * @param result The result from parse() method
     * @return JSONObject if result is a JSONObject
     * @throws JSONException if result is not a JSONObject
     */
    private static JSONObject toJSONObjectSafe(Object result) throws JSONException {
        if (result instanceof JSONObject) {
            return (JSONObject) result;
        }
        throw new JSONException("Expected JSONObject but got " + (result == null ? "null" : result.getClass().getSimpleName()));
    }

    /**
     * Parse XML values and store them in a JSONArray.
     * @param x       The XMLTokener containing the source string.
     * @param arrayForm true if array form, false if object form.
     * @param ja      The JSONArray that is containing the current tag or null
     *     if we are at the outermost level.
     * @param keepStrings	Don't type-convert text nodes and attribute values
     * @return A JSONArray if the value is the outermost tag, otherwise null.
     * @throws JSONException if a parsing error occurs
     */
    private static Object parse(XMLTokener x, boolean arrayForm, JSONArray ja, boolean keepStrings, int currentNestingDepth) throws JSONException {
        return parse(x, arrayForm, ja, keepStrings ? JSONMLParserConfiguration.KEEP_STRINGS : JSONMLParserConfiguration.ORIGINAL, currentNestingDepth);
    }

    /**
     * Parse XML values and store them in a JSONArray.
     * @param x       The XMLTokener containing the source string.
     * @param arrayForm true if array form, false if object form.
     * @param ja      The JSONArray that is containing the current tag or null
     *     if we are at the outermost level.
     * @param config  The parser configuration:
     *     JSONMLParserConfiguration.ORIGINAL is the default behaviour;
     *     JSONMLParserConfiguration.KEEP_STRINGS means Don't type-convert text nodes and attribute values.
     * @return A JSONArray if the value is the outermost tag, otherwise null.
     * @throws JSONException if a parsing error occurs
     */
    private static Object parse(XMLTokener x, boolean arrayForm, JSONArray ja, JSONMLParserConfiguration config, int currentNestingDepth) throws JSONException {
        String attribute;
        char c;
        String closeTag = null;
        int i;
        JSONArray newja = null;
        JSONObject newjo = null;
        Object token;
        String tagName = null;
        // Test for and skip past these forms:
        //      <!-- ... -->
        //      <![  ... ]]>
        //      <!   ...   >
        //      <?   ...  ?>
        while (true) {
            if (!x.more()) {
                throw x.syntaxError("Bad XML");
            }
            token = x.nextContent();
            if (token == XML.LT) {
                token = x.nextToken();
                if (token instanceof Character) {
                    if (token == XML.SLASH) {
                        // Close tag </
                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw new JSONException("Expected a closing name instead of '" + token + "'.");
                        }
                        if (x.nextToken() != XML.GT) {
                            throw x.syntaxError("Misshaped close tag");
                        }
                        return token;
                    } else if (token == XML.BANG) {
                        // <!
                        c = x.next();
                        if (c == '-') {
                            if (x.next() == '-') {
                                x.skipPast("-->");
                            } else {
                                x.back();
                            }
                        } else if (c == '[') {
                            token = x.nextToken();
                            if ("CDATA".equals(token) && x.next() == '[') {
                                if (ja != null) {
                                    ja.put(x.nextCDATA());
                                }
                            } else {
                                throw x.syntaxError("Expected 'CDATA['");
                            }
                        } else {
                            i = 1;
                            do {
                                token = x.nextMeta();
                                if (token == null) {
                                    throw x.syntaxError("Missing '>' after '<!'.");
                                } else if (token == XML.LT) {
                                    i += 1;
                                } else if (token == XML.GT) {
                                    i -= 1;
                                }
                            } while (i > 0);
                        }
                    } else if (token == XML.QUEST) {
                        // <?
                        x.skipPast("?>");
                    } else {
                        throw x.syntaxError("Misshaped tag");
                    }
                    // Open tag <
                } else {
                    if (!(token instanceof String)) {
                        throw x.syntaxError("Bad tagName '" + token + "'.");
                    }
                    tagName = (String) token;
                    newja = new JSONArray();
                    newjo = new JSONObject();
                    if (arrayForm) {
                        newja.put(tagName);
                        if (ja != null) {
                            ja.put(newja);
                        }
                    } else {
                        newjo.put("tagName", tagName);
                        if (ja != null) {
                            ja.put(newjo);
                        }
                    }
                    token = null;
                    for (; ; ) {
                        if (token == null) {
                            token = x.nextToken();
                        }
                        if (token == null) {
                            throw x.syntaxError("Misshaped tag");
                        }
                        if (!(token instanceof String)) {
                            break;
                        }
                        // attribute = value
                        attribute = (String) token;
                        if (!arrayForm && ("tagName".equals(attribute) || "childNode".equals(attribute))) {
                            throw x.syntaxError("Reserved attribute.");
                        }
                        token = x.nextToken();
                        if (token == XML.EQ) {
                            token = x.nextToken();
                            if (!(token instanceof String)) {
                                throw x.syntaxError("Missing value");
                            }
                            newjo.accumulate(attribute, config.isKeepStrings() ? ((String) token) : XML.stringToValue((String) token));
                            token = null;
                        } else {
                            newjo.accumulate(attribute, "");
                        }
                    }
                    if (arrayForm && newjo.length() > 0) {
                        newja.put(newjo);
                    }
                    // Empty tag <.../>
                    if (token == XML.SLASH) {
                        if (x.nextToken() != XML.GT) {
                            throw x.syntaxError("Misshaped tag");
                        }
                        if (ja == null) {
                            if (arrayForm) {
                                return newja;
                            }
                            return newjo;
                        }
                        // Content, between <...> and </...>
                    } else {
                        if (token != XML.GT) {
                            throw x.syntaxError("Misshaped tag");
                        }
                        if (currentNestingDepth == config.getMaxNestingDepth()) {
                            throw x.syntaxError("Maximum nesting depth of " + config.getMaxNestingDepth() + " reached");
                        }
                        closeTag = (String) parse(x, arrayForm, newja, config, currentNestingDepth + 1);
                        if (closeTag != null) {
                            if (!closeTag.equals(tagName)) {
                                throw x.syntaxError("Mismatched '" + tagName + "' and '" + closeTag + "'");
                            }
                            tagName = null;
                            if (!arrayForm && newja.length() > 0) {
                                newjo.put("childNodes", newja);
                            }
                            if (ja == null) {
                                if (arrayForm) {
                                    return newja;
                                }
                                return newjo;
                            }
                        }
                    }
                }
            } else {
                if (ja != null) {
                    Object value;
                    if (token instanceof String) {
                        String strToken = (String) token;
                        if (config.isKeepStrings()) {
                            value = XML.unescape(strToken);
                        } else {
                            value = XML.stringToValue(strToken);
                        }
                    } else {
                        value = token;
                    }
                    ja.put(value);
                }
            }
        }
    }

    public static JSONArray toJSONArray(String string) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static JSONArray toJSONArray(String string, boolean keepStrings) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static JSONArray toJSONArray(String string, JSONMLParserConfiguration config) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static JSONArray toJSONArray(XMLTokener x, JSONMLParserConfiguration config) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static JSONArray toJSONArray(XMLTokener x, boolean keepStrings) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static JSONArray toJSONArray(XMLTokener x) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static JSONObject toJSONObject(String string) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static JSONObject toJSONObject(String string, boolean keepStrings) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static JSONObject toJSONObject(String string, JSONMLParserConfiguration config) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static JSONObject toJSONObject(XMLTokener x) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static JSONObject toJSONObject(XMLTokener x, boolean keepStrings) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static JSONObject toJSONObject(XMLTokener x, JSONMLParserConfiguration config) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static String toString(JSONArray ja) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static String toString(JSONObject jo) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
