package org.json;

/*
Public Domain.
 */
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>
 * &nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2016-08/15
 */
public class JSONArray implements Iterable<Object> {

    /**
     * The arrayList where the JSONArray's properties are kept.
     */
    private final ArrayList<Object> myArrayList;

    /**
     * Construct an empty JSONArray.
     */
    public JSONArray() {
        this.myArrayList = new ArrayList<Object>();
    }

    /**
     * Construct a JSONArray from a JSONTokener.
     *
     * @param x
     *            A JSONTokener
     * @throws JSONException
     *             If there is a syntax error.
     */
    public JSONArray(JSONTokener x) throws JSONException {
        this(x, x.getJsonParserConfiguration());
    }

    /**
     * Constructs a JSONArray from a JSONTokener and a JSONParserConfiguration.
     *
     * @param x                       A JSONTokener instance from which the JSONArray is constructed.
     * @param jsonParserConfiguration A JSONParserConfiguration instance that controls the behavior of the parser.
     * @throws JSONException If a syntax error occurs during the construction of the JSONArray.
     */
    public JSONArray(JSONTokener x, JSONParserConfiguration jsonParserConfiguration) throws JSONException {
        this();
        boolean isInitial = x.getPrevious() == 0;
        if (x.nextClean() != '[') {
            throw x.syntaxError("A JSONArray text must start with '['");
        }
        char nextChar = x.nextClean();
        if (nextChar == 0) {
            // array is unclosed. No ']' found, instead EOF
            throw x.syntaxError("Expected a ',' or ']'");
        } else if (nextChar == ',' && jsonParserConfiguration.isStrictMode()) {
            throw x.syntaxError("Array content starts with a ','");
        }
        if (nextChar != ']') {
            x.back();
            for (; ; ) {
                if (x.nextClean() == ',') {
                    x.back();
                    this.myArrayList.add(JSONObject.NULL);
                } else {
                    x.back();
                    this.myArrayList.add(x.nextValue());
                }
                if (checkForSyntaxError(x, jsonParserConfiguration, isInitial))
                    return;
            }
        } else {
            if (isInitial && jsonParserConfiguration.isStrictMode() && x.nextClean() != 0) {
                throw x.syntaxError("Strict mode error: Unparsed characters found at end of input text");
            }
        }
    }

    /**
     * Convenience function. Checks for JSON syntax error.
     * @param x                       A JSONTokener instance from which the JSONArray is constructed.
     * @param jsonParserConfiguration A JSONParserConfiguration instance that controls the behavior of the parser.
     * @param isInitial               Boolean indicating position of char
     * @return                        true if a syntax error has occurred, otherwise false
     */
    private boolean checkForSyntaxError(JSONTokener x, JSONParserConfiguration jsonParserConfiguration, boolean isInitial) {
        char nextChar;
        switch(x.nextClean()) {
            case 0:
                // array is unclosed. No ']' found, instead EOF
                throw x.syntaxError("Expected a ',' or ']'");
            case ',':
                nextChar = x.nextClean();
                if (nextChar == 0) {
                    // array is unclosed. No ']' found, instead EOF
                    throw x.syntaxError("Expected a ',' or ']'");
                }
                if (nextChar == ']') {
                    // trailing commas are not allowed in strict mode
                    if (jsonParserConfiguration.isStrictMode()) {
                        throw x.syntaxError("Strict mode error: Expected another array element");
                    }
                    return true;
                }
                if (nextChar == ',') {
                    // Consecutive commas are not allowed in strict mode.
                    // Otherwise, the tokener is backed up, and a null object is inserted by the calling code.
                    if (jsonParserConfiguration.isStrictMode()) {
                        throw x.syntaxError("Strict mode error: Expected a valid array element");
                    }
                }
                x.back();
                break;
            case ']':
                if (isInitial && jsonParserConfiguration.isStrictMode() && x.nextClean() != 0) {
                    throw x.syntaxError("Strict mode error: Unparsed characters found at end of input text");
                }
                return true;
            default:
                throw x.syntaxError("Expected a ',' or ']'");
        }
        return false;
    }

    /**
     * Construct a JSONArray from a source JSON text.
     *
     * @param source
     *            A string that begins with <code>[</code>&nbsp;<small>(left
     *            bracket)</small> and ends with <code>]</code>
     *            &nbsp;<small>(right bracket)</small>.
     * @throws JSONException
     *             If there is a syntax error.
     */
    public JSONArray(String source) throws JSONException {
        this(source, new JSONParserConfiguration());
    }

    /**
     * Construct a JSONArray from a source JSON text.
     *
     * @param source
     *            A string that begins with <code>[</code>&nbsp;<small>(left
     *            bracket)</small> and ends with <code>]</code>
     *            &nbsp;<small>(right bracket)</small>.
     * @param jsonParserConfiguration the parser config object
     * @throws JSONException
     *             If there is a syntax error.
     */
    public JSONArray(String source, JSONParserConfiguration jsonParserConfiguration) throws JSONException {
        this(new JSONTokener(source, jsonParserConfiguration), jsonParserConfiguration);
    }

    /**
     * Construct a JSONArray from a Collection.
     *
     * @param collection
     *            A Collection.
     */
    public JSONArray(Collection<?> collection) {
        this(collection, 0, new JSONParserConfiguration());
    }

    /**
     * Construct a JSONArray from a Collection.
     *
     * @param collection
     *            A Collection.
     * @param jsonParserConfiguration
     *            Configuration object for the JSON parser
     */
    public JSONArray(Collection<?> collection, JSONParserConfiguration jsonParserConfiguration) {
        this(collection, 0, jsonParserConfiguration);
    }

    /**
     * Construct a JSONArray from a collection with recursion depth.
     *
     * @param collection
     *             A Collection.
     * @param recursionDepth
     *             Variable for tracking the count of nested object creations.
     * @param jsonParserConfiguration
     *             Configuration object for the JSON parser
     */
    JSONArray(Collection<?> collection, int recursionDepth, JSONParserConfiguration jsonParserConfiguration) {
        if (recursionDepth > jsonParserConfiguration.getMaxNestingDepth()) {
            throw new JSONException("JSONArray has reached recursion depth limit of " + jsonParserConfiguration.getMaxNestingDepth());
        }
        if (collection == null) {
            this.myArrayList = new ArrayList<Object>();
        } else {
            this.myArrayList = new ArrayList<Object>(collection.size());
            this.addAll(collection, true, recursionDepth, jsonParserConfiguration);
        }
    }

    /**
     * Construct a JSONArray from an Iterable. This is a shallow copy.
     *
     * @param iter
     *            A Iterable collection.
     */
    public JSONArray(Iterable<?> iter) {
        this();
        if (iter == null) {
            return;
        }
        this.addAll(iter, true);
    }

    /**
     * Construct a JSONArray from another JSONArray. This is a shallow copy.
     *
     * @param array
     *            A array.
     */
    public JSONArray(JSONArray array) {
        if (array == null) {
            this.myArrayList = new ArrayList<Object>();
        } else {
            // shallow copy directly the internal array lists as any wrapping
            // should have been done already in the original JSONArray
            this.myArrayList = new ArrayList<Object>(array.myArrayList);
        }
    }

    /**
     * Construct a JSONArray from an array.
     *
     * @param array
     *            Array. If the parameter passed is null, or not an array, an
     *            exception will be thrown.
     *
     * @throws JSONException
     *            If not an array or if an array value is non-finite number.
     * @throws NullPointerException
     *            Thrown if the array parameter is null.
     */
    public JSONArray(Object array) throws JSONException {
        this();
        if (!array.getClass().isArray()) {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
        this.addAll(array, true, 0);
    }

    /**
     * Construct a JSONArray with the specified initial capacity.
     *
     * @param initialCapacity
     *            the initial capacity of the JSONArray.
     * @throws JSONException
     *             If the initial capacity is negative.
     */
    public JSONArray(int initialCapacity) throws JSONException {
        if (initialCapacity < 0) {
            throw new JSONException("JSONArray initial capacity cannot be negative.");
        }
        this.myArrayList = new ArrayList<Object>(initialCapacity);
    }

    @Override
    public Iterator<Object> iterator() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Object get(int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean getBoolean(int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double getDouble(int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public float getFloat(int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Number getNumber(int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public <E extends Enum<E>> E getEnum(Class<E> clazz, int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public BigDecimal getBigDecimal(int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public BigInteger getBigInteger(int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public BigInteger getBigInteger(int index, JSONParserConfiguration jsonParserConfiguration) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int getInt(int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray getJSONArray(int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject getJSONObject(int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long getLong(int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String getString(int index) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isNull(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String join(String separator) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int length() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public void clear() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Object opt(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean optBoolean(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean optBoolean(int index, boolean defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Boolean optBooleanObject(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Boolean optBooleanObject(int index, Boolean defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double optDouble(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public double optDouble(int index, double defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Double optDoubleObject(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Double optDoubleObject(int index, Double defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public float optFloat(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public float optFloat(int index, float defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Float optFloatObject(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Float optFloatObject(int index, Float defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int optInt(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public int optInt(int index, int defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Integer optIntegerObject(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Integer optIntegerObject(int index, Integer defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, int index, E defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public BigInteger optBigInteger(int index, BigInteger defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public BigInteger optBigInteger(int index, BigInteger defaultValue, JSONParserConfiguration jsonParserConfiguration) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public BigDecimal optBigDecimal(int index, BigDecimal defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray optJSONArray(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray optJSONArray(int index, JSONArray defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject optJSONObject(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONObject optJSONObject(int index, JSONObject defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long optLong(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public long optLong(int index, long defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Long optLongObject(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Long optLongObject(int index, Long defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Number optNumber(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public Number optNumber(int index, Number defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String optString(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public String optString(int index, String defaultValue) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(boolean value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(Collection<?> value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(double value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(float value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(int value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(long value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(Map<?, ?> value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(Object value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(int index, boolean value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(int index, Collection<?> value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(int index, double value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(int index, float value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(int index, int value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(int index, long value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(int index, Map<?, ?> value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(int index, Map<?, ?> value, JSONParserConfiguration jsonParserConfiguration) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray put(int index, Object value) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray putAll(Collection<?> collection) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray putAll(Iterable<?> iter) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray putAll(JSONArray array) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public JSONArray putAll(Object array) throws JSONException {
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

    public Object remove(int index) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean similar(Object other) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Convenience function; checks for object similarity
     * @param valueThis
     *      Initial object to compare
     * @param valueOther
     *      Comparison object
     * @return  boolean
     */
    private boolean isSimilar(Object valueThis, Object valueOther) {
        if (valueThis instanceof JSONObject) {
            if (!((JSONObject) valueThis).similar(valueOther)) {
                return false;
            }
        } else if (valueThis instanceof JSONArray) {
            if (!((JSONArray) valueThis).similar(valueOther)) {
                return false;
            }
        } else if (valueThis instanceof Number && valueOther instanceof Number) {
            if (!JSONObject.isNumberSimilar((Number) valueThis, (Number) valueOther)) {
                return false;
            }
        } else if (valueThis instanceof JSONString && valueOther instanceof JSONString) {
            if (!((JSONString) valueThis).toJSONString().equals(((JSONString) valueOther).toJSONString())) {
                return false;
            }
        } else if (!valueThis.equals(valueOther)) {
            return false;
        }
        return true;
    }

    public JSONObject toJSONObject(JSONArray names) throws JSONException {
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

    public Writer write(Writer writer) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @SuppressWarnings("resource")
    public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Convenience function. Attempts to write
     * @param writer
     *            Writes the serialized JSON
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indentation of the top level.
     * @param i
     *            Index in array to be added
     */
    private void writeArrayAttempt(Writer writer, int indentFactor, int indent, int i) {
        try {
            JSONObject.writeValue(writer, this.myArrayList.get(i), indentFactor, indent);
        } catch (Exception e) {
            throw new JSONException("Unable to write JSONArray value at index: " + i, e);
        }
    }

    public List<Object> toList() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    /**
     * Add a collection's elements to the JSONArray.
     *
     * @param collection
     *            A Collection.
     * @param wrap
     *            {@code true} to call {@link JSONObject#wrap(Object)} for each item,
     *            {@code false} to add the items directly
     * @param recursionDepth
     *            Variable for tracking the count of nested object creations.
     */
    private void addAll(Collection<?> collection, boolean wrap, int recursionDepth, JSONParserConfiguration jsonParserConfiguration) {
        this.myArrayList.ensureCapacity(this.myArrayList.size() + collection.size());
        if (wrap) {
            for (Object o : collection) {
                this.put(JSONObject.wrap(o, recursionDepth + 1, jsonParserConfiguration));
            }
        } else {
            for (Object o : collection) {
                this.put(o);
            }
        }
    }

    /**
     * Add an Iterable's elements to the JSONArray.
     *
     * @param iter
     *            An Iterable.
     * @param wrap
     *            {@code true} to call {@link JSONObject#wrap(Object)} for each item,
     *            {@code false} to add the items directly
     */
    private void addAll(Iterable<?> iter, boolean wrap) {
        if (wrap) {
            for (Object o : iter) {
                this.put(JSONObject.wrap(o));
            }
        } else {
            for (Object o : iter) {
                this.put(o);
            }
        }
    }

    /**
     * Add an array's elements to the JSONArray.
     *
     * @param array
     *          Array. If the parameter passed is null, or not an array,
     *          JSONArray, Collection, or Iterable, an exception will be
     *          thrown.
     * @param wrap
     *          {@code true} to call {@link JSONObject#wrap(Object)} for each item,
     *          {@code false} to add the items directly
     * @throws JSONException
     *          If not an array or if an array value is non-finite number.
     */
    private void addAll(Object array, boolean wrap) throws JSONException {
        this.addAll(array, wrap, 0);
    }

    /**
     * Add an array's elements to the JSONArray.
     *
     * @param array
     *            Array. If the parameter passed is null, or not an array,
     *            JSONArray, Collection, or Iterable, an exception will be
     *            thrown.
     * @param wrap
     *          {@code true} to call {@link JSONObject#wrap(Object)} for each item,
     *          {@code false} to add the items directly
     * @param recursionDepth
     *          Variable for tracking the count of nested object creations.
     */
    private void addAll(Object array, boolean wrap, int recursionDepth) {
        addAll(array, wrap, recursionDepth, new JSONParserConfiguration());
    }

    /**
     *  Add an array's elements to the JSONArray.
     * `
     *  @param array
     *             Array. If the parameter passed is null, or not an array,
     *             JSONArray, Collection, or Iterable, an exception will be
     *             thrown.
     *  @param wrap
     *             {@code true} to call {@link JSONObject#wrap(Object)} for each item,
     *             {@code false} to add the items directly
     *  @param recursionDepth
     *             Variable for tracking the count of nested object creations.
     *  @param jsonParserConfiguration
     *             Variable to pass parser custom configuration for json parsing.
     *  @throws JSONException
     *             If not an array or if an array value is non-finite number.
     *  @throws NullPointerException
     *             Thrown if the array parameter is null.
     */
    private void addAll(Object array, boolean wrap, int recursionDepth, JSONParserConfiguration jsonParserConfiguration) throws JSONException {
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            this.myArrayList.ensureCapacity(this.myArrayList.size() + length);
            if (wrap) {
                for (int i = 0; i < length; i += 1) {
                    this.put(JSONObject.wrap(Array.get(array, i), recursionDepth + 1, jsonParserConfiguration));
                }
            } else {
                for (int i = 0; i < length; i += 1) {
                    this.put(Array.get(array, i));
                }
            }
        } else if (array instanceof JSONArray) {
            // use the built in array list `addAll` as all object
            // wrapping should have been completed in the original
            // JSONArray
            this.myArrayList.addAll(((JSONArray) array).myArrayList);
        } else if (array instanceof Collection) {
            this.addAll((Collection<?>) array, wrap, recursionDepth, jsonParserConfiguration);
        } else if (array instanceof Iterable) {
            this.addAll((Iterable<?>) array, wrap);
        } else {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
    }

    /**
     * Create a new JSONException in a common format for incorrect conversions.
     * @param idx index of the item
     * @param valueType the type of value being coerced to
     * @param cause optional cause of the coercion failure
     * @return JSONException that can be thrown.
     */
    private static JSONException wrongValueFormatException(int idx, String valueType, Object value, Throwable cause) {
        if (value == null) {
            return new JSONException("JSONArray[" + idx + "] is not a " + valueType + " (null).", cause);
        }
        // don't try to toString collections or known object types that could be large.
        if (value instanceof Map || value instanceof Iterable || value instanceof JSONObject) {
            return new JSONException("JSONArray[" + idx + "] is not a " + valueType + " (" + value.getClass() + ").", cause);
        }
        return new JSONException("JSONArray[" + idx + "] is not a " + valueType + " (" + value.getClass() + " : " + value + ").", cause);
    }
}
