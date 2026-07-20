package org.json;

/*
Public Domain.
*/
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.GenericArrayType;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its external
 * form is a string wrapped in curly braces with colons between the names and
 * values, and commas between the values and names. The internal form is an
 * object having <code>get</code> and <code>opt</code> methods for accessing
 * the values by name, and <code>put</code> methods for adding or replacing
 * values by name. The values can be any of these types: <code>Boolean</code>,
 * <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>,
 * <code>String</code>, or the <code>JSONObject.NULL</code> object. A
 * JSONObject constructor can be used to convert an external form JSON text
 * into an internal form whose values can be retrieved with the
 * <code>get</code> and <code>opt</code> methods, or to convert values into a
 * JSON text using the <code>put</code> and <code>toString</code> methods. A
 * <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you. The opt methods differ from the get methods in that they
 * do not throw. Instead, they return a specified value, such as null.
 * <p>
 * The <code>put</code> methods add or replace values in an object. For
 * example,
 *
 * <pre>
 * myString = new JSONObject()
 *         .put(&quot;JSON&quot;, &quot;Hello, World!&quot;).toString();
 * </pre>
 *
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON syntax rules. The constructors are more forgiving in the texts they
 * will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a
 * quote or single quote, and if they do not contain leading or trailing
 * spaces, and if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>,
 * or <code>null</code>.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2016-08-15
 */
public class JSONObject {

    /**
     * JSONObject.NULL is equivalent to the value that JavaScript calls null,
     * whilst Java's null is equivalent to the value that JavaScript calls
     * undefined.
     */
    private static final class Null {

        @Override
        @SuppressWarnings("lgtm[java/unchecked-cast-in-equals]")
        public boolean equals(Object object) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public int hashCode() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }

    /**
     *  Regular Expression Pattern that matches JSON Numbers. This is primarily used for
     *  output to guarantee that we are always writing valid JSON.
     */
    static final Pattern NUMBER_PATTERN = Pattern.compile("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?");

    /**
     * The map where the JSONObject's properties are kept.
     */
    private final Map<String, Object> map;

    public Class<? extends Map> getMapType() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * It is sometimes more convenient and less ambiguous to have a
     * <code>NULL</code> object than to use Java's <code>null</code> value.
     * <code>JSONObject.NULL.equals(null)</code> returns <code>true</code>.
     * <code>JSONObject.NULL.toString()</code> returns <code>"null"</code>.
     */
    public static final Object NULL = new Null();

    /**
     * Set of method names that should be excluded when identifying record-style accessors.
     * These are common bean/Object method names that are not property accessors.
     */
    private static final Set<String> EXCLUDED_RECORD_METHOD_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("get", "is", "set", "toString", "hashCode", "equals", "clone", "notify", "notifyAll", "wait")));

    /**
     * Construct an empty JSONObject.
     */
    public JSONObject() {
        // HashMap is used on purpose to ensure that elements are unordered by
        // the specification.
        // JSON tends to be a portable transfer format to allows the container
        // implementations to rearrange their items for a faster element
        // retrieval based on associative access.
        // Therefore, an implementation mustn't rely on the order of the item.
        this.map = new HashMap<String, Object>();
    }

    /**
     * Construct a JSONObject from a subset of another JSONObject. An array of
     * strings is used to identify the keys that should be copied. Missing keys
     * are ignored.
     *
     * @param jo
     *            A JSONObject.
     * @param names
     *            An array of strings.
     */
    public JSONObject(JSONObject jo, String... names) {
        this(names.length);
        for (int i = 0; i < names.length; i += 1) {
            try {
                this.putOnce(names[i], jo.opt(names[i]));
            } catch (Exception ignore) {
                // exception thrown for missing key
            }
        }
    }

    /**
     * Construct a JSONObject from a JSONTokener.
     *
     * @param x
     *            A JSONTokener object containing the source string.
     * @throws JSONException
     *             If there is a syntax error in the source string or a
     *             duplicated key.
     */
    public JSONObject(JSONTokener x) throws JSONException {
        this(x, x.getJsonParserConfiguration());
    }

    /**
     * Construct a JSONObject from a JSONTokener with custom json parse configurations.
     *
     * @param x
     *            A JSONTokener object containing the source string.
     * @param jsonParserConfiguration
     *            Variable to pass parser custom configuration for json parsing.
     * @throws JSONException
     *             If there is a syntax error in the source string or a
     *             duplicated key.
     */
    public JSONObject(JSONTokener x, JSONParserConfiguration jsonParserConfiguration) throws JSONException {
        this();
        boolean isInitial = x.getPrevious() == 0;
        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        for (; ; ) {
            if (parseJSONObject(x, jsonParserConfiguration, isInitial)) {
                return;
            }
        }
    }

    /**
     * Parses entirety of JSON object
     *
     * @param jsonTokener Parses text as tokens
     * @param jsonParserConfiguration Variable to pass parser custom configuration for json parsing.
     * @param isInitial True if start of document, else false
     * @return True if done building object, else false
     */
    private boolean parseJSONObject(JSONTokener jsonTokener, JSONParserConfiguration jsonParserConfiguration, boolean isInitial) {
        Object obj;
        String key;
        boolean doneParsing = false;
        char c = jsonTokener.nextClean();
        switch(c) {
            case 0:
                throw jsonTokener.syntaxError("A JSONObject text must end with '}'");
            case '}':
                if (isInitial && jsonParserConfiguration.isStrictMode() && jsonTokener.nextClean() != 0) {
                    throw jsonTokener.syntaxError("Strict mode error: Unparsed characters found at end of input text");
                }
                return true;
            default:
                obj = jsonTokener.nextSimpleValue(c);
                key = obj.toString();
        }
        checkKeyForStrictMode(jsonTokener, jsonParserConfiguration, obj);
        // The key is followed by ':'.
        c = jsonTokener.nextClean();
        if (c != ':') {
            throw jsonTokener.syntaxError("Expected a ':' after a key");
        }
        // Use syntaxError(..) to include error location
        if (key != null) {
            // Check if key exists
            boolean keyExists = this.opt(key) != null;
            if (keyExists && !jsonParserConfiguration.isOverwriteDuplicateKey()) {
                throw jsonTokener.syntaxError("Duplicate key \"" + key + "\"");
            }
            Object value = jsonTokener.nextValue();
            // Only add value if non-null
            if (value != null) {
                this.put(key, value);
            }
        }
        // Pairs are separated by ','.
        if (parseEndOfKeyValuePair(jsonTokener, jsonParserConfiguration, isInitial)) {
            doneParsing = true;
        }
        return doneParsing;
    }

    /**
     * Checks for valid end of key:value pair
     * @param jsonTokener Parses text as tokens
     * @param jsonParserConfiguration Variable to pass parser custom configuration for json parsing.
     * @param isInitial True if end of JSON object, else false
     * @return
     */
    private static boolean parseEndOfKeyValuePair(JSONTokener jsonTokener, JSONParserConfiguration jsonParserConfiguration, boolean isInitial) {
        switch(jsonTokener.nextClean()) {
            case ';':
                // In strict mode semicolon is not a valid separator
                if (jsonParserConfiguration.isStrictMode()) {
                    throw jsonTokener.syntaxError("Strict mode error: Invalid character ';' found");
                }
                break;
            case ',':
                if (jsonTokener.nextClean() == '}') {
                    // trailing commas are not allowed in strict mode
                    if (jsonParserConfiguration.isStrictMode()) {
                        throw jsonTokener.syntaxError("Strict mode error: Expected another object element");
                    }
                    // End of JSON object
                    return true;
                }
                if (jsonTokener.end()) {
                    throw jsonTokener.syntaxError("A JSONObject text must end with '}'");
                }
                jsonTokener.back();
                break;
            case '}':
                if (isInitial && jsonParserConfiguration.isStrictMode() && jsonTokener.nextClean() != 0) {
                    throw jsonTokener.syntaxError("Strict mode error: Unparsed characters found at end of input text");
                }
                // End of JSON object
                return true;
            default:
                throw jsonTokener.syntaxError("Expected a ',' or '}'");
        }
        // Not at end of JSON object
        return false;
    }

    /**
     * Throws error if key violates strictMode
     * @param jsonTokener Parses text as tokens
     * @param jsonParserConfiguration Variable to pass parser custom configuration for json parsing.
     * @param obj Value to be checked
     */
    private static void checkKeyForStrictMode(JSONTokener jsonTokener, JSONParserConfiguration jsonParserConfiguration, Object obj) {
        if (jsonParserConfiguration != null && jsonParserConfiguration.isStrictMode()) {
            if (obj instanceof Boolean) {
                throw jsonTokener.syntaxError(String.format("Strict mode error: key '%s' cannot be boolean", obj.toString()));
            }
            if (obj == JSONObject.NULL) {
                throw jsonTokener.syntaxError(String.format("Strict mode error: key '%s' cannot be null", obj.toString()));
            }
            if (obj instanceof Number) {
                throw jsonTokener.syntaxError(String.format("Strict mode error: key '%s' cannot be number", obj.toString()));
            }
        }
    }

    /**
     * Construct a JSONObject from a Map.
     *
     * @param m
     *            A map object that can be used to initialize the contents of
     *            the JSONObject.
     * @throws JSONException
     *            If a value in the map is non-finite number.
     * @throws NullPointerException
     *            If a key in the map is <code>null</code>
     */
    public JSONObject(Map<?, ?> m) {
        this(m, 0, new JSONParserConfiguration());
    }

    /**
     * Construct a JSONObject from a Map with custom json parse configurations.
     *
     * @param m
     *            A map object that can be used to initialize the contents of
     *            the JSONObject.
     * @param jsonParserConfiguration
     *            Variable to pass parser custom configuration for json parsing.
     */
    public JSONObject(Map<?, ?> m, JSONParserConfiguration jsonParserConfiguration) {
        this(m, 0, jsonParserConfiguration);
    }

    /**
     * Construct a JSONObject from a map with recursion depth.
     */
    private JSONObject(Map<?, ?> m, int recursionDepth, JSONParserConfiguration jsonParserConfiguration) {
        if (recursionDepth > jsonParserConfiguration.getMaxNestingDepth()) {
            throw new JSONException("JSONObject has reached recursion depth limit of " + jsonParserConfiguration.getMaxNestingDepth());
        }
        if (m == null) {
            this.map = new HashMap<String, Object>();
        } else {
            this.map = new HashMap<String, Object>(m.size());
            for (final Entry<?, ?> e : m.entrySet()) {
                if (e.getKey() == null) {
                    throw new NullPointerException("Null key.");
                }
                final Object value = e.getValue();
                if (value != null || jsonParserConfiguration.isUseNativeNulls()) {
                    testValidity(value);
                    this.map.put(String.valueOf(e.getKey()), wrap(value, recursionDepth + 1, jsonParserConfiguration));
                }
            }
        }
    }

    /**
     * Construct a JSONObject from an Object using bean getters. It reflects on
     * all of the public methods of the object. For each of the methods with no
     * parameters and a name starting with <code>"get"</code> or
     * <code>"is"</code> followed by an uppercase letter, the method is invoked,
     * and a key and the value returned from the getter method are put into the
     * new JSONObject.
     * <p>
     * The key is formed by removing the <code>"get"</code> or <code>"is"</code>
     * prefix. If the second remaining character is not upper case, then the
     * first character is converted to lower case.
     * <p>
     * Methods that are <code>static</code>, return <code>void</code>,
     * have parameters, or are "bridge" methods, are ignored.
     * <p>
     * For example, if an object has a method named <code>"getName"</code>, and
     * if the result of calling <code>object.getName()</code> is
     * <code>"Larry Fine"</code>, then the JSONObject will contain
     * <code>"name": "Larry Fine"</code>.
     * <p>
     * The {@link JSONPropertyName} annotation can be used on a bean getter to
     * override key name used in the JSONObject. For example, using the object
     * above with the <code>getName</code> method, if we annotated it with:
     * <pre>
     * &#64;JSONPropertyName("FullName")
     * public String getName() { return this.name; }
     * </pre>
     * The resulting JSON object would contain <code>"FullName": "Larry Fine"</code>
     * <p>
     * Similarly, the {@link JSONPropertyName} annotation can be used on non-
     * <code>get</code> and <code>is</code> methods. We can also override key
     * name used in the JSONObject as seen below even though the field would normally
     * be ignored:
     * <pre>
     * &#64;JSONPropertyName("FullName")
     * public String fullName() { return this.name; }
     * </pre>
     * The resulting JSON object would contain <code>"FullName": "Larry Fine"</code>
     * <p>
     * The {@link JSONPropertyIgnore} annotation can be used to force the bean property
     * to not be serialized into JSON. If both {@link JSONPropertyIgnore} and
     * {@link JSONPropertyName} are defined on the same method, a depth comparison is
     * performed and the one closest to the concrete class being serialized is used.
     * If both annotations are at the same level, then the {@link JSONPropertyIgnore}
     * annotation takes precedent and the field is not serialized.
     * For example, the following declaration would prevent the <code>getName</code>
     * method from being serialized:
     * <pre>
     * &#64;JSONPropertyName("FullName")
     * &#64;JSONPropertyIgnore
     * public String getName() { return this.name; }
     * </pre>
     *
     * @param bean
     *            An object that has getter methods that should be used to make
     *            a JSONObject.
     * @throws JSONException
     *            If a getter returned a non-finite number.
     */
    public JSONObject(Object bean) {
        this();
        this.populateMap(bean, new JSONParserConfiguration());
    }

    public JSONObject(Object bean, JSONParserConfiguration jsonParserConfiguration) {
        this();
        this.populateMap(bean, jsonParserConfiguration);
    }

    private JSONObject(Object bean, Set<Object> objectsRecord) {
        this();
        this.populateMap(bean, objectsRecord, new JSONParserConfiguration());
    }

    /**
     * Construct a JSONObject from an Object, using reflection to find the
     * public members. The resulting JSONObject's keys will be the strings from
     * the names array, and the values will be the field values associated with
     * those keys in the object. If a key is not found or not visible, then it
     * will not be copied into the new JSONObject.
     *
     * @param object
     *            An object that has fields that should be used to make a
     *            JSONObject.
     * @param names
     *            An array of strings, the names of the fields to be obtained
     *            from the object.
     */
    public JSONObject(Object object, String... names) {
        this(names.length);
        Class<?> c = object.getClass();
        for (int i = 0; i < names.length; i += 1) {
            String name = names[i];
            try {
                this.putOpt(name, c.getField(name).get(object));
            } catch (Exception ignore) {
                // if invalid, do not include key:value pair in JSONObject
            }
        }
    }

    /**
     * Construct a JSONObject from a source JSON text string. This is the most
     * commonly used JSONObject constructor.
     *
     * @param source
     *            A string beginning with <code>{</code>&nbsp;<small>(left
     *            brace)</small> and ending with <code>}</code>
     *            &nbsp;<small>(right brace)</small>.
     * @exception JSONException
     *                If there is a syntax error in the source string or a
     *                duplicated key.
     */
    public JSONObject(String source) throws JSONException {
        this(source, new JSONParserConfiguration());
    }

    /**
     * Construct a JSONObject from a source JSON text string with custom json parse configurations.
     * This is the most commonly used JSONObject constructor.
     *
     * @param source
     *            A string beginning with <code>{</code>&nbsp;<small>(left
     *            brace)</small> and ending with <code>}</code>
     *            &nbsp;<small>(right brace)</small>.
     * @param jsonParserConfiguration
     *            Variable to pass parser custom configuration for json parsing.
     * @exception JSONException
     *                If there is a syntax error in the source string or a
     *                duplicated key.
     */
    public JSONObject(String source, JSONParserConfiguration jsonParserConfiguration) throws JSONException {
        this(new JSONTokener(source, jsonParserConfiguration), jsonParserConfiguration);
    }

    /**
     * Construct a JSONObject from a ResourceBundle.
     *
     * @param baseName
     *            The ResourceBundle base name.
     * @param locale
     *            The Locale to load the ResourceBundle for.
     * @throws JSONException
     *             If any JSONExceptions are detected.
     */
    public JSONObject(String baseName, Locale locale) throws JSONException {
        this();
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, Thread.currentThread().getContextClassLoader());
        // Iterate through the keys in the bundle.
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key != null) {
                // Go through the path, ensuring that there is a nested JSONObject for each
                // segment except the last. Add the value using the last segment's name into
                // the deepest nested JSONObject.
                String[] path = ((String) key).split("\\.");
                int last = path.length - 1;
                JSONObject target = this;
                for (int i = 0; i < last; i += 1) {
                    String segment = path[i];
                    JSONObject nextTarget = target.optJSONObject(segment);
                    if (nextTarget == null) {
                        nextTarget = new JSONObject();
                        target.put(segment, nextTarget);
                    }
                    target = nextTarget;
                }
                target.put(path[last], bundle.getString((String) key));
            }
        }
    }

    /**
     * Constructor to specify an initial capacity of the internal map. Useful for library
     * internal calls where we know, or at least can best guess, how big this JSONObject
     * will be.
     *
     * @param initialCapacity initial capacity of the internal map.
     */
    protected JSONObject(int initialCapacity) {
        this.map = new HashMap<String, Object>(initialCapacity);
    }

    public JSONObject accumulate(String key, Object value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject append(String key, Object value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static String doubleToString(double d) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Object get(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean getBoolean(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public BigInteger getBigInteger(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public BigInteger getBigInteger(String key, JSONParserConfiguration jsonParserConfiguration) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public BigDecimal getBigDecimal(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double getDouble(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public float getFloat(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Number getNumber(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int getInt(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray getJSONArray(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject getJSONObject(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long getLong(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static String[] getNames(JSONObject jo) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static String[] getNames(Object object) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String getString(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean has(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject increment(String key) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isNull(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Iterator<String> keys() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Set<String> keySet() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int length() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void clear() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray names() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static String numberToString(Number number) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Object opt(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key, E defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean optBoolean(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean optBoolean(String key, boolean defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Boolean optBooleanObject(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Boolean optBooleanObject(String key, Boolean defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public BigDecimal optBigDecimal(String key, BigDecimal defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static BigDecimal objectToBigDecimal(Object val, BigDecimal defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static BigDecimal objectToBigDecimal(Object val, BigDecimal defaultValue, boolean exact) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public BigInteger optBigInteger(String key, BigInteger defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public BigInteger optBigInteger(String key, BigInteger defaultValue, JSONParserConfiguration jsonParserConfiguration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static BigInteger objectToBigInteger(Object val, BigInteger defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static BigInteger objectToBigInteger(Object val, BigInteger defaultValue, JSONParserConfiguration jsonParserConfiguration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Convenience method to attempt conversion of value to BigInteger.
     * Added to reduce complexity of objectToBigInteger()
     * @param val the value to be converted
     * @param defaultValue the default value to use if conversion is not attempted or fails
     * @param maxNumberLength the max length allowed for BigIntegers
     * @return the converted value, or the defaultValue
     */
    private static BigInteger attemptConversionToBigInteger(Object val, BigInteger defaultValue, int maxNumberLength) {
        // don't check if it's a string in case of unchecked Number subclasses
        try {
            /**
             * the other opt functions handle implicit conversions, i.e.
             * jo.put("double",1.1d);
             * jo.optInt("double"); -- will return 1, not an error
             * this conversion to BigDecimal then to BigInteger is to maintain
             * that type cast support that may truncate the decimal.
             */
            final String valStr = val.toString();
            if (isDecimalNotation(valStr)) {
                BigDecimal bd = new BigDecimal(valStr);
                if (maxNumberLength != ParserConfiguration.UNDEFINED_MAXIMUM_NUMBER_LENGTH && (long) bd.precision() - bd.scale() > maxNumberLength) {
                    return defaultValue;
                }
                return bd.toBigInteger();
            }
            return new BigInteger(valStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public double optDouble(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double optDouble(String key, double defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Double optDoubleObject(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Double optDoubleObject(String key, Double defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public float optFloat(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public float optFloat(String key, float defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Float optFloatObject(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Float optFloatObject(String key, Float defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int optInt(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int optInt(String key, int defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Integer optIntegerObject(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Integer optIntegerObject(String key, Integer defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray optJSONArray(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray optJSONArray(String key, JSONArray defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject optJSONObject(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject optJSONObject(String key, JSONObject defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long optLong(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long optLong(String key, long defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Long optLongObject(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Long optLongObject(String key, Long defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Number optNumber(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Number optNumber(String key, Number defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String optString(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String optString(String key, String defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Populates the internal map of the JSONObject with the bean properties. The
     * bean can not be recursive.
     *
     * @see JSONObject#JSONObject(Object)
     *
     * @param bean
     *            the bean
     * @throws JSONException
     *            If a getter returned a non-finite number.
     */
    private void populateMap(Object bean, JSONParserConfiguration jsonParserConfiguration) {
        populateMap(bean, Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>()), jsonParserConfiguration);
    }

    /**
     * Convert a bean into a json object
     * @param bean object tobe converted
     * @param objectsRecord set of all objects for this method
     * @param jsonParserConfiguration json parser settings
     */
    private void populateMap(Object bean, Set<Object> objectsRecord, JSONParserConfiguration jsonParserConfiguration) {
        Class<?> klass = bean.getClass();
        // If klass is a System class then set includeSuperClass to false.
        // Check if this is a Java record type
        boolean isRecord = isRecordType(klass);
        Method[] methods = getMethods(klass);
        for (final Method method : methods) {
            if (isValidMethod(method)) {
                final String key = getKeyNameFromMethod(method, isRecord);
                if (key != null && !key.isEmpty()) {
                    processMethod(bean, objectsRecord, jsonParserConfiguration, method, key);
                }
            }
        }
    }

    /**
     * Processes method into json object entry if appropriate
     * @param bean object being processed (owns the method)
     * @param objectsRecord set of all objects for this method
     * @param jsonParserConfiguration json parser settings
     * @param method method being processed
     * @param key name of the method
     */
    private void processMethod(Object bean, Set<Object> objectsRecord, JSONParserConfiguration jsonParserConfiguration, Method method, String key) {
        try {
            final Object result = method.invoke(bean);
            if (result != null || jsonParserConfiguration.isUseNativeNulls()) {
                // check cyclic dependency and throw error if needed
                // the wrap and populateMap combination method is
                // itself DFS recursive
                if (objectsRecord.contains(result)) {
                    throw recursivelyDefinedObjectException(key);
                }
                objectsRecord.add(result);
                testValidity(result);
                this.map.put(key, wrap(result, objectsRecord));
                objectsRecord.remove(result);
                closeClosable(result);
            }
        } catch (IllegalAccessException ignore) {
            // ignore exception
        } catch (IllegalArgumentException ignore) {
            // ignore exception
        } catch (InvocationTargetException ignore) {
            // ignore exception
        }
    }

    /**
     * Checks if a class is a Java record type.
     * This uses reflection to check for the isRecord() method which was introduced in Java 16.
     * This approach works even when running on Java 6+ JVM.
     *
     * @param klass the class to check
     * @return true if the class is a record type, false otherwise
     */
    private static boolean isRecordType(Class<?> klass) {
        try {
            // Use reflection to check if Class has an isRecord() method (Java 16+)
            // This allows the code to compile on Java 6 while still detecting records at runtime
            Method isRecordMethod = Class.class.getMethod("isRecord");
            return (Boolean) isRecordMethod.invoke(klass);
        } catch (NoSuchMethodException e) {
            // isRecord() method doesn't exist - we're on Java < 16
            return false;
        } catch (Exception e) {
            // Any other reflection error - assume not a record
            return false;
        }
    }

    /**
     * This is a convenience method to simplify populate maps
     * @param klass the name of the object being checked
     * @return methods of klass
     */
    private static Method[] getMethods(Class<?> klass) {
        boolean includeSuperClass = klass.getClassLoader() != null;
        return includeSuperClass ? klass.getMethods() : klass.getDeclaredMethods();
    }

    private static boolean isValidMethodName(String name) {
        return !"getClass".equals(name) && !"getDeclaringClass".equals(name);
    }

    private static String getKeyNameFromMethod(Method method, boolean isRecordType) {
        final int ignoreDepth = getAnnotationDepth(method, JSONPropertyIgnore.class);
        if (ignoreDepth > 0) {
            final int forcedNameDepth = getAnnotationDepth(method, JSONPropertyName.class);
            if (forcedNameDepth < 0 || ignoreDepth <= forcedNameDepth) {
                // the hierarchy asked to ignore, and the nearest name override
                // was higher or non-existent
                return null;
            }
        }
        JSONPropertyName annotation = getAnnotation(method, JSONPropertyName.class);
        if (annotationValueNotEmpty(annotation)) {
            return annotation.value();
        }
        String key;
        final String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
            key = name.substring(3);
        } else if (name.startsWith("is") && name.length() > 2) {
            key = name.substring(2);
        } else {
            // Only check for record-style accessors if this is actually a record type
            // This maintains backward compatibility - classes with lowercase methods won't be affected
            if (isRecordType && isRecordStyleAccessor(name, method)) {
                return name;
            }
            return null;
        }
        // if the first letter in the key is not uppercase, then skip.
        // This is to maintain backwards compatibility before PR406
        // (https://github.com/stleary/JSON-java/pull/406/)
        if (key.isEmpty() || Character.isLowerCase(key.charAt(0))) {
            return null;
        }
        if (key.length() == 1) {
            key = key.toLowerCase(Locale.ROOT);
        } else if (!Character.isUpperCase(key.charAt(1))) {
            key = key.substring(0, 1).toLowerCase(Locale.ROOT) + key.substring(1);
        }
        return key;
    }

    /**
     * Checks if a method is a record-style accessor.
     * Record accessors have lowercase names without get/is prefixes and are not inherited from standard Java classes.
     *
     * @param methodName the name of the method
     * @param method the method to check
     * @return true if this is a record-style accessor, false otherwise
     */
    private static boolean isRecordStyleAccessor(String methodName, Method method) {
        if (methodName.isEmpty() || !Character.isLowerCase(methodName.charAt(0))) {
            return false;
        }
        // Exclude common bean/Object method names
        if (EXCLUDED_RECORD_METHOD_NAMES.contains(methodName)) {
            return false;
        }
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass == null || declaringClass == Object.class) {
            return false;
        }
        if (Enum.class.isAssignableFrom(declaringClass) || Number.class.isAssignableFrom(declaringClass)) {
            return false;
        }
        String className = declaringClass.getName();
        return !className.startsWith("java.") && !className.startsWith("javax.");
    }

    /**
     * checks if the annotation is not null and the {@link JSONPropertyName#value()} is not null and is not empty.
     * @param annotation the annotation to check
     * @return true if the annotation and the value is not null and not empty, false otherwise.
     */
    private static boolean annotationValueNotEmpty(JSONPropertyName annotation) {
        return annotation != null && annotation.value() != null && !annotation.value().isEmpty();
    }

    /**
     * Checks if the method is valid for the {@link #populateMap(Object, Set, JSONParserConfiguration)} use case
     * @param method the Method to check
     * @return true, if valid, false otherwise.
     */
    private static boolean isValidMethod(Method method) {
        final int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && method.getParameterTypes().length == 0 && !method.isBridge() && method.getReturnType() != Void.TYPE && isValidMethodName(method.getName());
    }

    /**
     * calls {@link Closeable#close()} on the input, if it is an instance of Closable.
     * @param input the input to close, if possible.
     */
    private static void closeClosable(Object input) {
        // we don't use the result anywhere outside of wrap
        // if it's a resource we should be sure to close it
        // after calling toString
        if (input instanceof Closeable) {
            try {
                ((Closeable) input).close();
            } catch (IOException ignore) {
                // close has failed; best effort has been made
            }
        }
    }

    /**
     * Searches the class hierarchy to see if the method or it's super
     * implementations and interfaces has the annotation.
     *
     * @param <A>
     *            type of the annotation
     *
     * @param m
     *            method to check
     * @param annotationClass
     *            annotation to look for
     * @return the {@link Annotation} if the annotation exists on the current method
     *         or one of its super class definitions
     */
    private static <A extends Annotation> A getAnnotation(final Method m, final Class<A> annotationClass) {
        // If we have invalid data the result is null
        if (m == null || annotationClass == null) {
            return null;
        }
        if (m.isAnnotationPresent(annotationClass)) {
            return m.getAnnotation(annotationClass);
        }
        // If we've already reached the Object class, return null;
        Class<?> c = m.getDeclaringClass();
        if (c.getSuperclass() == null) {
            return null;
        }
        // check directly implemented interfaces for the method being checked
        for (Class<?> i : c.getInterfaces()) {
            try {
                Method im = i.getMethod(m.getName(), m.getParameterTypes());
                return getAnnotation(im, annotationClass);
            } catch (final SecurityException ex) {
                // ignore this exception
            } catch (final NoSuchMethodException ex) {
                // ignore this excpetion
            }
        }
        // If the superclass is Object, no annotations will be found any more
        if (Object.class.equals(c.getSuperclass()))
            return null;
        try {
            return getAnnotation(c.getSuperclass().getMethod(m.getName(), m.getParameterTypes()), annotationClass);
        } catch (final SecurityException ex) {
            return null;
        } catch (final NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * Searches the class hierarchy to see if the method or it's super
     * implementations and interfaces has the annotation. Returns the depth of the
     * annotation in the hierarchy.
     *
     * @param m
     *            method to check
     * @param annotationClass
     *            annotation to look for
     * @return Depth of the annotation or -1 if the annotation is not on the method.
     */
    private static int getAnnotationDepth(final Method m, final Class<? extends Annotation> annotationClass) {
        // if we have invalid data the result is -1
        if (m == null || annotationClass == null) {
            return -1;
        }
        if (m.isAnnotationPresent(annotationClass)) {
            return 1;
        }
        // we've already reached the Object class
        Class<?> c = m.getDeclaringClass();
        if (c.getSuperclass() == null) {
            return -1;
        }
        // check directly implemented interfaces for the method being checked
        for (Class<?> i : c.getInterfaces()) {
            try {
                Method im = i.getMethod(m.getName(), m.getParameterTypes());
                int d = getAnnotationDepth(im, annotationClass);
                if (d > 0) {
                    // since the annotation was on the interface, add 1
                    return d + 1;
                }
            } catch (final SecurityException ex) {
                // Nothing to do here
            } catch (final NoSuchMethodException ex) {
                // Nothing to do here
            }
        }
        //If the superclass is Object, no annotations will be found any more
        if (Object.class.equals(c.getSuperclass()))
            return -1;
        try {
            int d = getAnnotationDepth(c.getSuperclass().getMethod(m.getName(), m.getParameterTypes()), annotationClass);
            if (d > 0) {
                // since the annotation was on the superclass, add 1
                return d + 1;
            }
            return -1;
        } catch (final SecurityException ex) {
            return -1;
        } catch (final NoSuchMethodException ex) {
            return -1;
        }
    }

    public JSONObject put(String key, boolean value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject put(String key, Collection<?> value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject put(String key, double value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject put(String key, float value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject put(String key, int value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject put(String key, long value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject put(String key, Map<?, ?> value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject put(String key, Object value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject putOnce(String key, Object value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject putOpt(String key, Object value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Object query(String jsonPointer) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Object query(JSONPointer jsonPointer) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Object optQuery(String jsonPointer) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Object optQuery(JSONPointer jsonPointer) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("resource")
    public static String quote(String string) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static Writer quote(String string, Writer w) throws IOException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Convenience method to reduce cognitive complexity of quote()
     * @param w      The Writer to which the quoted string will be appended.
     * @param c      Character to write
     * @throws IOException
     */
    private static void writeAsHex(Writer w, char c) throws IOException {
        String hhhh;
        if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
            w.write("\\u");
            hhhh = Integer.toHexString(c);
            w.write("0000", 0, 4 - hhhh.length());
            w.write(hhhh);
        } else {
            w.write(c);
        }
    }

    public Object remove(String key) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean similar(Object other) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private boolean checkSimilarEntries(Object other) {
        for (final Entry<String, ?> entry : this.entrySet()) {
            String name = entry.getKey();
            Object valueThis = entry.getValue();
            Object valueOther = ((JSONObject) other).get(name);
            if (valueThis == valueOther) {
                continue;
            }
            if (valueThis == null) {
                return false;
            }
            if (!checkObjectType(valueThis, valueOther)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convenience function. Compares types of two objects.
     * @param valueThis     Object whose type is being checked
     * @param valueOther    Reference object
     * @return  true if match, else false
     */
    private boolean checkObjectType(Object valueThis, Object valueOther) {
        if (valueThis instanceof JSONObject) {
            return ((JSONObject) valueThis).similar(valueOther);
        } else if (valueThis instanceof JSONArray) {
            return ((JSONArray) valueThis).similar(valueOther);
        } else if (valueThis instanceof Number && valueOther instanceof Number) {
            return isNumberSimilar((Number) valueThis, (Number) valueOther);
        } else if (valueThis instanceof JSONString && valueOther instanceof JSONString) {
            return ((JSONString) valueThis).toJSONString().equals(((JSONString) valueOther).toJSONString());
        } else if (!valueThis.equals(valueOther)) {
            return false;
        }
        return true;
    }

    static boolean isNumberSimilar(Number l, Number r) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private static boolean numberIsFinite(Number n) {
        if (n instanceof Double && (((Double) n).isInfinite() || ((Double) n).isNaN())) {
            return false;
        } else if (n instanceof Float && (((Float) n).isInfinite() || ((Float) n).isNaN())) {
            return false;
        }
        return true;
    }

    protected static boolean isDecimalNotation(final String val) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string.
     * Warning! stringToValue(String) uses the default max number length. If you want to override it,
     * use a suitable initialized JSONParserConfiguration and the method: stringToValue(String, JSONParserConfiguration).
     *
     * @param str A String. can not be null.
     * @return A simple JSON value.
     * @throws NullPointerException
     *             Thrown if the string is null.
     */
    // Changes to this method must be copied to the corresponding method in
    // the XML class to keep full support for Android
    public static Object stringToValue(String str) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string.
     *
     * @param string A String. can not be null.
     * @param jsonParserConfiguration the parser config
     * @return A simple JSON value. If the string represents a number that is too large,
     * a string will be returned.
     * @throws NullPointerException Thrown if the string is null.
     */
    // Changes to this method must be copied to the corresponding method in
    // the XML class to keep full support for Android
    public static Object stringToValue(String string, JSONParserConfiguration jsonParserConfiguration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    protected static Number stringToNumber(final String val) throws NumberFormatException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Convenience function. Block items like 00 01 etc. Java number parsers treat these as Octal.
     * @param val value to convert
     * @param initial first char of val
     * @throws exceptions if numbers are formatted incorrectly
     */
    private static void checkForInvalidNumberFormat(String val, char initial) {
        if (initial == '0' && val.length() > 1) {
            char at1 = val.charAt(1);
            if (at1 >= '0' && at1 <= '9') {
                throw new NumberFormatException("val [" + val + "] is not a valid number.");
            }
        } else if (initial == '-' && val.length() > 2) {
            char at1 = val.charAt(1);
            char at2 = val.charAt(2);
            if (at1 == '0' && at2 >= '0' && at2 <= '9') {
                throw new NumberFormatException("val [" + val + "] is not a valid number.");
            }
        }
    }

    /**
     * Convenience function. Handles val if it is a number
     * @param val value to convert
     * @param initial first char of val
     * @return val as a BigDecimal
     */
    private static Number getNumber(String val, char initial) {
        // Use a BigDecimal all the time so we keep the original
        // representation. BigDecimal doesn't support -0.0, ensure we
        // keep that by forcing a decimal.
        try {
            BigDecimal bd = new BigDecimal(val);
            if (initial == '-' && BigDecimal.ZERO.compareTo(bd) == 0) {
                return Double.valueOf(-0.0);
            }
            return bd;
        } catch (NumberFormatException retryAsDouble) {
            // this is to support "Hex Floats" like this: 0x1.0P-1074
            try {
                Double d = Double.valueOf(val);
                if (d.isNaN() || d.isInfinite()) {
                    throw new NumberFormatException("val [" + val + "] is not a valid number.");
                }
                return d;
            } catch (NumberFormatException ignore) {
                throw new NumberFormatException("val [" + val + "] is not a valid number.");
            }
        }
    }

    public static void testValidity(Object o) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("resource")
    public String toString(int indentFactor) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static String valueToString(Object value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static Object wrap(Object object) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static Object wrap(Object object, int recursionDepth, JSONParserConfiguration jsonParserConfiguration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    private static Object wrap(Object object, Set<Object> objectsRecord) {
        return wrap(object, objectsRecord, 0, new JSONParserConfiguration());
    }

    private static Object wrap(Object object, Set<Object> objectsRecord, int recursionDepth, JSONParserConfiguration jsonParserConfiguration) {
        try {
            if (NULL.equals(object)) {
                return NULL;
            }
            if (object instanceof JSONObject || object instanceof JSONArray || object instanceof JSONString || object instanceof String || object instanceof Byte || object instanceof Character || object instanceof Short || object instanceof Integer || object instanceof Long || object instanceof Boolean || object instanceof Float || object instanceof Double || object instanceof BigInteger || object instanceof BigDecimal || object instanceof Enum) {
                return object;
            }
            if (object instanceof Collection) {
                Collection<?> coll = (Collection<?>) object;
                return new JSONArray(coll, recursionDepth, jsonParserConfiguration);
            }
            if (object.getClass().isArray()) {
                return new JSONArray(object);
            }
            if (object instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) object;
                return new JSONObject(map, recursionDepth, jsonParserConfiguration);
            }
            Package objectPackage = object.getClass().getPackage();
            String objectPackageName = objectPackage != null ? objectPackage.getName() : "";
            if (objectPackageName.startsWith("java.") || objectPackageName.startsWith("javax.") || object.getClass().getClassLoader() == null) {
                return object.toString();
            }
            if (objectsRecord != null) {
                return new JSONObject(object, objectsRecord);
            }
            return new JSONObject(object);
        } catch (JSONException exception) {
            throw exception;
        } catch (Exception exception) {
            return null;
        }
    }

    public Writer write(Writer writer) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("resource")
    static final Writer writeValue(Writer writer, Object value, int indentFactor, int indent) throws JSONException, IOException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Convenience function to reduce cog complexity of calling method; writes value if string is valid
     * @param writer    Object doing the writing
     * @param value     Value to be written
     * @throws IOException if something goes wrong
     */
    private static void processJsonStringToWriteValue(Writer writer, Object value) throws IOException {
        // JSONString must be checked first, so it can overwrite behaviour of other types below
        Object o;
        try {
            o = ((JSONString) value).toJSONString();
        } catch (Exception e) {
            throw new JSONException(e);
        }
        writer.write(o != null ? o.toString() : quote(value.toString()));
    }

    /**
     * Convenience function to reduce cog complexity of calling method; writes value if number is valid
     * @param writer    Object doing the writing
     * @param value     Value to be written
     * @throws IOException if something goes wrong
     */
    private static void processNumberToWriteValue(Writer writer, Number value) throws IOException {
        // not all Numbers may match actual JSON Numbers. i.e. fractions or Imaginary
        final String numberAsString = numberToString(value);
        if (NUMBER_PATTERN.matcher(numberAsString).matches()) {
            writer.write(numberAsString);
        } else {
            // The Number value is not a valid JSON number.
            // Instead we will quote it as a string
            quote(numberAsString, writer);
        }
    }

    static final void indent(Writer writer, int indent) throws IOException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("resource")
    public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Convenience function. Writer attempts to write formatted content
     * @param writer
     *            Writes the serialized JSON
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indentation of the top level.
     * @param needsComma
     *            Boolean flag indicating a comma is needed
     * @throws IOException
     *            If something goes wrong
     */
    private void writeContent(Writer writer, int indentFactor, int indent, boolean needsComma) throws IOException {
        final int newIndent = indent + indentFactor;
        for (final Entry<String, ?> entry : this.entrySet()) {
            if (needsComma) {
                writer.write(',');
            }
            if (indentFactor > 0) {
                writer.write('\n');
            }
            indent(writer, newIndent);
            final String key = entry.getKey();
            writer.write(quote(key));
            writer.write(':');
            if (indentFactor > 0) {
                writer.write(' ');
            }
            attemptWriteValue(writer, indentFactor, newIndent, entry, key);
            needsComma = true;
        }
        if (indentFactor > 0) {
            writer.write('\n');
        }
        indent(writer, indent);
    }

    /**
     * Convenience function. Writer attempts to write a value.
     * @param writer
     *            Writes the serialized JSON
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indentation of the top level.
     * @param entry
     *            Contains the value being written
     * @param key
     *            Identifies the value
     * @throws JSONException if a called function has an error or a write error
     * occurs
     */
    private static void attemptWriteValue(Writer writer, int indentFactor, int indent, Entry<String, ?> entry, String key) {
        try {
            writeValue(writer, entry.getValue(), indentFactor, indent);
        } catch (Exception e) {
            throw new JSONException("Unable to write JSONObject value for key: " + key, e);
        }
    }

    public Map<String, Object> toMap() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Create a new JSONException in a common format for incorrect conversions.
     * @param key name of the key
     * @param valueType the type of value being coerced to
     * @param cause optional cause of the coercion failure
     * @return JSONException that can be thrown.
     */
    private static JSONException wrongValueFormatException(String key, String valueType, Object value, Throwable cause) {
        if (value == null) {
            return new JSONException("JSONObject[" + quote(key) + "] is not a " + valueType + " (null).", cause);
        }
        // don't try to toString collections or known object types that could be large.
        if (value instanceof Map || value instanceof Iterable || value instanceof JSONObject) {
            return new JSONException("JSONObject[" + quote(key) + "] is not a " + valueType + " (" + value.getClass() + ").", cause);
        }
        return new JSONException("JSONObject[" + quote(key) + "] is not a " + valueType + " (" + value.getClass() + " : " + value + ").", cause);
    }

    /**
     * Create a new JSONException in a common format for recursive object definition.
     * @param key name of the key
     * @return JSONException that can be thrown.
     */
    private static JSONException recursivelyDefinedObjectException(String key) {
        return new JSONException("JavaBean object contains recursively defined member variable of key " + quote(key));
    }

    /**
     * Helper method to extract the raw Class from Type.
     */
    private Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof GenericArrayType) {
            // Simplified handling for arrays
            return Object[].class;
        }
        // Fallback
        return Object.class;
    }

    /**
     * Extracts the element Type for a Collection Type.
     */
    private Type getElementType(Type type) {
        if (type instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) type).getActualTypeArguments();
            return args.length > 0 ? args[0] : Object.class;
        }
        return Object.class;
    }

    /**
     * Extracts the key and value Types for a Map Type.
     */
    private Type[] getMapTypes(Type type) {
        if (type instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) type).getActualTypeArguments();
            if (args.length == 2) {
                return args;
            }
        }
        // Default: String keys, Object values
        return new Type[] { Object.class, Object.class };
    }

    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(Class<T> clazz) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Recursively converts a value to the target Type, handling nested generics for Collections and Maps.
     */
    private Object convertValue(Object value, Type targetType) throws JSONException {
        if (value == null) {
            return null;
        }
        Class<?> rawType = getRawType(targetType);
        // Direct assignment
        if (rawType.isAssignableFrom(value.getClass())) {
            return value;
        }
        if (rawType == int.class || rawType == Integer.class) {
            return ((Number) value).intValue();
        } else if (rawType == double.class || rawType == Double.class) {
            return ((Number) value).doubleValue();
        } else if (rawType == float.class || rawType == Float.class) {
            return ((Number) value).floatValue();
        } else if (rawType == long.class || rawType == Long.class) {
            return ((Number) value).longValue();
        } else if (rawType == boolean.class || rawType == Boolean.class) {
            return value;
        } else if (rawType == String.class) {
            return value;
        } else if (rawType == BigDecimal.class) {
            return new BigDecimal((String) value);
        } else if (rawType == BigInteger.class) {
            return new BigInteger((String) value);
        }
        // Enum conversion
        if (rawType.isEnum() && value instanceof String) {
            return stringToEnum(rawType, (String) value);
        }
        // Collection handling (e.g., List<List<Map<String, Integer>>>)
        if (Collection.class.isAssignableFrom(rawType)) {
            if (value instanceof JSONArray) {
                Type elementType = getElementType(targetType);
                return fromJsonArray((JSONArray) value, rawType, elementType);
            }
        } else // Map handling (e.g., Map<Integer, List<String>>)
        if (Map.class.isAssignableFrom(rawType) && value instanceof JSONObject) {
            Type[] mapTypes = getMapTypes(targetType);
            Type keyType = mapTypes[0];
            Type valueType = mapTypes[1];
            return convertToMap((JSONObject) value, keyType, valueType, rawType);
        } else // POJO handling (including custom classes like Tuple<Integer, String, Integer>)
        if (!rawType.isPrimitive() && !rawType.isEnum() && value instanceof JSONObject) {
            // Recurse with the raw class for POJO deserialization
            return ((JSONObject) value).fromJson(rawType);
        }
        // Fallback
        return value.toString();
    }

    /**
     * Converts a JSONObject to a Map with the specified generic key and value Types.
     * Supports nested types via recursive convertValue.
     */
    private Map<?, ?> convertToMap(JSONObject jsonMap, Type keyType, Type valueType, Class<?> mapType) throws JSONException {
        try {
            @SuppressWarnings("unchecked")
            Map<Object, Object> createdMap = new HashMap();
            for (Object keyObj : jsonMap.keySet()) {
                String keyStr = (String) keyObj;
                Object mapValue = jsonMap.get(keyStr);
                // Convert key (e.g., String to Integer for Map<Integer, ...>)
                Object convertedKey = convertValue(keyStr, keyType);
                // Convert value recursively (handles nesting)
                Object convertedValue = convertValue(mapValue, valueType);
                createdMap.put(convertedKey, convertedValue);
            }
            return createdMap;
        } catch (Exception e) {
            throw new JSONException("Failed to convert JSONObject to Map: " + mapType.getName(), e);
        }
    }

    /**
     * Converts a String to an Enum value.
     * The unchecked warning is suppressed when casting valueOf() to E
     * @param enumClass enum class
     * @param value value of enum
     * @param <E> type of enum
     */
    @SuppressWarnings("unchecked")
    private <E> E stringToEnum(Class<?> enumClass, String value) throws JSONException {
        try {
            @SuppressWarnings("unchecked")
            Class<E> enumType = (Class<E>) enumClass;
            Method valueOfMethod = enumType.getMethod("valueOf", String.class);
            return (E) valueOfMethod.invoke(null, value);
        } catch (Exception e) {
            throw new JSONException("Failed to convert string to enum: " + value + " for " + enumClass.getName(), e);
        }
    }

    /**
     * Deserializes a JSONArray into a Collection, supporting nested generics.
     * Uses recursive convertValue for elements.
     */
    @SuppressWarnings("unchecked")
    private <T> Collection<T> fromJsonArray(JSONArray jsonArray, Class<?> collectionType, Type elementType) throws JSONException {
        try {
            Collection<T> collection = getCollection(collectionType);
            for (int i = 0; i < jsonArray.length(); i++) {
                Object jsonElement = jsonArray.get(i);
                // Recursively convert each element using the full element Type (handles nesting)
                Object convertedValue = convertValue(jsonElement, elementType);
                collection.add((T) convertedValue);
            }
            return collection;
        } catch (Exception e) {
            throw new JSONException("Failed to convert JSONArray to Collection: " + collectionType.getName(), e);
        }
    }

    /**
     * Creates and returns a new instance of a supported {@link Collection} implementation
     * based on the specified collection type.
     * <p>
     * This method currently supports the following collection types:
     * <ul>
     *   <li>{@code List.class}</li>
     *   <li>{@code ArrayList.class}</li>
     *   <li>{@code Set.class}</li>
     *   <li>{@code HashSet.class}</li>
     * </ul>
     * If the provided type does not match any of the supported types, a {@link JSONException}
     * is thrown.
     *
     * @param collectionType the {@link Class} object representing the desired collection type
     * @return a new empty instance of the specified collection type
     * @throws JSONException if the specified type is not a supported collection type
     */
    private Collection getCollection(Class<?> collectionType) throws JSONException {
        if (collectionType == List.class || collectionType == ArrayList.class) {
            return new ArrayList();
        } else if (collectionType == Set.class || collectionType == HashSet.class) {
            return new HashSet();
        } else {
            throw new JSONException("Unsupported Collection type: " + collectionType.getName());
        }
    }
}
