package org.json;

import static java.lang.String.format;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
Public Domain.
*/
/**
 * A JSON Pointer is a simple query language defined for JSON documents by
 * <a href="https://tools.ietf.org/html/rfc6901">RFC 6901</a>.
 *
 * In a nutshell, JSONPointer allows the user to navigate into a JSON document
 * using strings, and retrieve targeted objects, like a simple form of XPATH.
 * Path segments are separated by the '/' char, which signifies the root of
 * the document when it appears as the first char of the string. Array
 * elements are navigated using ordinals, counting from 0. JSONPointer strings
 * may be extended to any arbitrary number of segments. If the navigation
 * is successful, the matched item is returned. A matched item may be a
 * JSONObject, a JSONArray, or a JSON value. If the JSONPointer string building
 * fails, an appropriate exception is thrown. If the navigation fails to find
 * a match, a JSONPointerException is thrown.
 *
 * @author JSON.org
 * @version 2016-05-14
 */
public class JSONPointer {

    // used for URL encoding and decoding
    private static final String ENCODING = "utf-8";

    /**
     * This class allows the user to build a JSONPointer in steps, using
     * exactly one segment in each step.
     */
    public static class Builder {

        /**
         * Constructs a new Builder object.
         */
        public Builder() {
        }

        // Segments for the eventual JSONPointer string
        private final List<String> refTokens = new ArrayList<String>();

        public JSONPointer build() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public Builder append(String token) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public Builder append(int arrayIndex) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    public static Builder builder() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    // Segments for the JSONPointer string
    private final List<String> refTokens;

    /**
     * Pre-parses and initializes a new {@code JSONPointer} instance. If you want to
     * evaluate the same JSON Pointer on different JSON documents then it is recommended
     * to keep the {@code JSONPointer} instances due to performance considerations.
     *
     * @param pointer the JSON String or URI Fragment representation of the JSON pointer.
     * @throws IllegalArgumentException if {@code pointer} is not a valid JSON pointer
     */
    public JSONPointer(final String pointer) {
        if (pointer == null) {
            throw new NullPointerException("pointer cannot be null");
        }
        if (pointer.isEmpty() || "#".equals(pointer)) {
            this.refTokens = Collections.emptyList();
            return;
        }
        String refs;
        if (pointer.startsWith("#/")) {
            refs = pointer.substring(2);
            try {
                refs = URLDecoder.decode(refs, ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else if (pointer.startsWith("/")) {
            refs = pointer.substring(1);
        } else {
            throw new IllegalArgumentException("a JSON pointer should start with '/' or '#/'");
        }
        this.refTokens = new ArrayList<String>();
        int slashIdx = -1;
        int prevSlashIdx = 0;
        do {
            prevSlashIdx = slashIdx + 1;
            slashIdx = refs.indexOf('/', prevSlashIdx);
            if (prevSlashIdx == slashIdx || prevSlashIdx == refs.length()) {
                // found 2 slashes in a row ( obj//next )
                // or single slash at the end of a string ( obj/test/ )
                this.refTokens.add("");
            } else if (slashIdx >= 0) {
                final String token = refs.substring(prevSlashIdx, slashIdx);
                this.refTokens.add(unescape(token));
            } else {
                // last item after separator, or no separator at all.
                final String token = refs.substring(prevSlashIdx);
                this.refTokens.add(unescape(token));
            }
        } while (slashIdx >= 0);
        // using split does not take into account consecutive separators or "ending nulls"
        //for (String token : refs.split("/")) {
        //    this.refTokens.add(unescape(token));
        //}
    }

    /**
     * Constructs a new JSONPointer instance with the provided list of reference tokens.
     *
     * @param refTokens A list of strings representing the reference tokens for the JSON Pointer.
     *                  Each token identifies a step in the path to the targeted value.
     */
    public JSONPointer(List<String> refTokens) {
        this.refTokens = new ArrayList<String>(refTokens);
    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc6901#section-3">rfc6901 section 3</a>
     */
    private static String unescape(String token) {
        return token.replace("~1", "/").replace("~0", "~");
    }

    public Object queryFrom(Object document) throws JSONPointerException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Matches a JSONArray element by ordinal position
     * @param current the JSONArray to be evaluated
     * @param indexToken the array index in string form
     * @return the matched object. If no matching item is found a
     * @throws JSONPointerException is thrown if the index is out of bounds
     */
    private static Object readByIndexToken(Object current, String indexToken) throws JSONPointerException {
        try {
            int index = Integer.parseInt(indexToken);
            JSONArray currentArr = (JSONArray) current;
            if (index >= currentArr.length()) {
                throw new JSONPointerException(format("index %s is out of bounds - the array has %d elements", indexToken, Integer.valueOf(currentArr.length())));
            }
            try {
                return currentArr.get(index);
            } catch (JSONException e) {
                throw new JSONPointerException("Error reading value at index position " + index, e);
            }
        } catch (NumberFormatException e) {
            throw new JSONPointerException(format("%s is not an array index", indexToken), e);
        }
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Escapes path segment values to an unambiguous form.
     * The escape char to be inserted is '~'. The chars to be escaped
     * are ~, which maps to ~0, and /, which maps to ~1.
     * @param token the JSONPointer segment value to be escaped
     * @return the escaped value for the token
     *
     * @see <a href="https://tools.ietf.org/html/rfc6901#section-3">rfc6901 section 3</a>
     */
    private static String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }

    public String toURIFragment() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
