package org.json;

/*
Public Domain.
*/
/**
 * The HTTPTokener extends the JSONTokener to provide additional methods
 * for the parsing of HTTP headers.
 * @author JSON.org
 * @version 2015-12-09
 */
public class HTTPTokener extends JSONTokener {

    /**
     * Construct an HTTPTokener from a string.
     * @param string A source string.
     */
    public HTTPTokener(String string) {
        super(string);
    }

    public String nextToken() throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
