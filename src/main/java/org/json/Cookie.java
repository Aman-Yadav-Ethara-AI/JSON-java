package org.json;

import java.util.Locale;

/*
Public Domain.
*/
/**
 * Convert a web browser cookie specification to a JSONObject and back.
 * JSON and Cookies are both notations for name/value pairs.
 * See also: <a href="https://tools.ietf.org/html/rfc6265">https://tools.ietf.org/html/rfc6265</a>
 * @author JSON.org
 * @version 2015-12-09
 */
public class Cookie {

    /**
     * Constructs a new Cookie object.
     * @deprecated (Utility class cannot be instantiated)
     */
    @Deprecated()
    public Cookie() {
    }

    public static String escape(String string) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static JSONObject toJSONObject(String string) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static String toString(JSONObject jo) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static String unescape(String string) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
