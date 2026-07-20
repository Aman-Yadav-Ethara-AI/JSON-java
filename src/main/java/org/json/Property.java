package org.json;

/*
Public Domain.
*/
import java.util.Enumeration;
import java.util.Properties;

/**
 * Converts a Property file data into JSONObject and back.
 * @author JSON.org
 * @version 2015-05-05
 */
public class Property {

    /**
     * Constructs a new Property object.
     */
    public Property() {
    }

    public static JSONObject toJSONObject(java.util.Properties properties) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static Properties toProperties(JSONObject jo) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
