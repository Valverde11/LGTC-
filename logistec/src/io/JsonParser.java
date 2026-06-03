package io;

import java.io.*;
import java.nio.file.*;


public class JsonParser {

    private final String src;
    private int pos;

    public JsonParser(String json) {
        this.src = json;
        this.pos = 0;
    }

    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    // ── public entry point ──────────────────────────────────────────────────

    /** Parse the root object and return a JsonObject. */
    public JsonObject parseObject() {
        skipWhitespace();
        expect('{');
        JsonObject obj = new JsonObject();
        skipWhitespace();
        if (peek() == '}') { pos++; return obj; }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            Object value = parseValue();
            obj.put(key, value);
            skipWhitespace();
            if (peek() == '}') { pos++; break; }
            expect(',');
        }
        return obj;
    }

    // ── internal parsers ────────────────────────────────────────────────────

    private Object parseValue() {
        char c = peek();
        if (c == '"')  return parseString();
        if (c == '{')  return parseObject();
        if (c == '[')  return parseArray();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n')  { parseNull(); return null; }
        return parseNumber();
    }

    public JsonArray parseArray() {
        expect('[');
        JsonArray arr = new JsonArray();
        skipWhitespace();
        if (peek() == ']') { pos++; return arr; }
        while (true) {
            skipWhitespace();
            arr.add(parseValue());
            skipWhitespace();
            if (peek() == ']') { pos++; break; }
            expect(',');
        }
        return arr;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') break;
            if (c == '\\') {
                char esc = src.charAt(pos++);
                switch (esc) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default  -> sb.append(esc);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Number parseNumber() {
        int start = pos;
        if (peek() == '-') pos++;
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.' || src.charAt(pos) == 'e' || src.charAt(pos) == 'E' || src.charAt(pos) == '+' || src.charAt(pos) == '-')) pos++;
        String num = src.substring(start, pos);
        if (num.contains(".") || num.contains("e") || num.contains("E"))
            return Double.parseDouble(num);
        return Long.parseLong(num);
    }

    private boolean parseBoolean() {
        if (src.startsWith("true", pos))  { pos += 4; return true; }
        if (src.startsWith("false", pos)) { pos += 5; return false; }
        throw new RuntimeException("Invalid boolean at pos " + pos);
    }

    private void parseNull() {
        if (src.startsWith("null", pos)) { pos += 4; return; }
        throw new RuntimeException("Invalid null at pos " + pos);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private char peek() {
        if (pos >= src.length()) throw new RuntimeException("Unexpected end of JSON");
        return src.charAt(pos);
    }

    private void expect(char c) {
        if (peek() != c) throw new RuntimeException("Expected '" + c + "' but got '" + peek() + "' at pos " + pos);
        pos++;
    }

    private void skipWhitespace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }

    // ── inner data classes ──────────────────────────────────────────────────

    /** Simple map-like JSON object backed by parallel arrays. */
    public static class JsonObject {
        private final String[] keys   = new String[64];
        private final Object[] values = new Object[64];
        private int size = 0;

        public void put(String key, Object value) {
            keys[size] = key;
            values[size] = value;
            size++;
        }

        public Object get(String key) {
            for (int i = 0; i < size; i++)
                if (keys[i].equals(key)) return values[i];
            return null;
        }

        public String getString(String key) {
            Object v = get(key);
            return v == null ? null : v.toString();
        }

        public int getInt(String key) {
            Object v = get(key);
            if (v == null) throw new RuntimeException("Key not found: " + key);
            return ((Number) v).intValue();
        }

        public int getInt(String key, int defaultVal) {
            Object v = get(key);
            return v == null ? defaultVal : ((Number) v).intValue();
        }

        public double getDouble(String key) {
            Object v = get(key);
            if (v == null) throw new RuntimeException("Key not found: " + key);
            return ((Number) v).doubleValue();
        }

        public JsonArray getArray(String key) {
            return (JsonArray) get(key);
        }

        public JsonObject getObject(String key) {
            return (JsonObject) get(key);
        }

        public boolean has(String key) {
            return get(key) != null;
        }
    }

    /** Simple list-like JSON array. */
    public static class JsonArray {
        private Object[] data = new Object[32];
        private int size = 0;

        public void add(Object o) {
            if (size == data.length) {
                Object[] newData = new Object[data.length * 2];
                System.arraycopy(data, 0, newData, 0, size);
                data = newData;
            }
            data[size++] = o;
        }

        public int size() { return size; }

        public Object get(int i) { return data[i]; }

        public JsonObject getObject(int i) { return (JsonObject) data[i]; }

        public String getString(int i) { return data[i].toString(); }
    }
}
