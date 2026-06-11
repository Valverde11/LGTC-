package io;

import java.io.*;
import java.nio.file.*;

/**
 * Parser JSON propio de LogísTEC para leer archivos de configuración.
 *
 * <p>Implementa un analizador sintáctico recursivo descendente que soporta
 * el esquema JSON definido en el proyecto: objetos anidados, arreglos,
 * strings, números enteros/decimales y booleanos.
 * No utiliza ninguna librería externa.
 *
 * @author Andres Aguilar
 * @version 1.0
 */
public class JsonParser {

    private final String src;
    private int pos;

    /**
     * Crea un parser para analizar el texto JSON proporcionado.
     *
     * @param json Cadena con el contenido JSON a analizar.
     */
    public JsonParser(String json) {
        this.src = json;
        this.pos = 0;
    }

    /**
     * Lee el contenido completo de un archivo de texto y lo retorna como String.
     *
     * @param path Ruta absoluta o relativa al archivo.
     * @return Contenido del archivo como cadena de texto.
     * @throws IOException Si el archivo no existe o no se puede leer.
     */
    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    /**
     * Analiza el JSON desde la posición actual y retorna el objeto raíz.
     * Debe llamarse cuando el JSON comienza con '{'.
     *
     * @return Objeto {@link JsonObject} con los pares clave-valor parseados.
     * @throws RuntimeException Si el JSON tiene formato inválido.
     */
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

    private Object parseValue() {
        char c = peek();
        if (c == '"')  return parseString();
        if (c == '{')  return parseObject();
        if (c == '[')  return parseArray();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n')  { parseNull(); return null; }
        return parseNumber();
    }

    /**
     * Analiza un arreglo JSON comenzando en '['.
     *
     * @return Objeto {@link JsonArray} con los elementos del arreglo.
     * @throws RuntimeException Si el JSON tiene formato inválido.
     */
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
        while (pos < src.length() && (Character.isDigit(src.charAt(pos))
                || src.charAt(pos) == '.' || src.charAt(pos) == 'e'
                || src.charAt(pos) == 'E' || src.charAt(pos) == '+'
                || src.charAt(pos) == '-')) pos++;
        String num = src.substring(start, pos);
        if (num.contains(".") || num.contains("e") || num.contains("E"))
            return Double.parseDouble(num);
        return Long.parseLong(num);
    }

    private boolean parseBoolean() {
        if (src.startsWith("true",  pos)) { pos += 4; return true; }
        if (src.startsWith("false", pos)) { pos += 5; return false; }
        throw new RuntimeException("Booleano inválido en posición " + pos);
    }

    private void parseNull() {
        if (src.startsWith("null", pos)) { pos += 4; return; }
        throw new RuntimeException("Null inválido en posición " + pos);
    }

    private char peek() {
        if (pos >= src.length()) throw new RuntimeException("Fin inesperado del JSON");
        return src.charAt(pos);
    }

    private void expect(char c) {
        if (peek() != c) throw new RuntimeException(
            "Se esperaba '" + c + "' pero se encontró '" + peek() + "' en posición " + pos);
        pos++;
    }

    private void skipWhitespace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }

    // ── Clases internas ──────────────────────────────────────────────────────

    /**
     * Representa un objeto JSON como un mapa de clave-valor respaldado por arreglos paralelos.
     */
    public static class JsonObject {
        private final String[] keys   = new String[64];
        private final Object[] values = new Object[64];
        private int size = 0;

        /**
         * Agrega un par clave-valor al objeto.
         *
         * @param key   Clave del atributo JSON.
         * @param value Valor asociado (puede ser String, Number, Boolean, JsonObject o JsonArray).
         */
        public void put(String key, Object value) {
            keys[size] = key; values[size] = value; size++;
        }

        /**
         * Retorna el valor asociado a la clave, o {@code null} si no existe.
         *
         * @param key Clave a buscar.
         * @return Valor asociado o {@code null}.
         */
        public Object get(String key) {
            for (int i = 0; i < size; i++)
                if (keys[i].equals(key)) return values[i];
            return null;
        }

        /**
         * Retorna el valor como String, o {@code null} si la clave no existe.
         *
         * @param key Clave del atributo.
         * @return Valor como String.
         */
        public String getString(String key) {
            Object v = get(key); return v == null ? null : v.toString();
        }

        /**
         * Retorna el valor como entero.
         *
         * @param key Clave del atributo.
         * @return Valor como int.
         * @throws RuntimeException Si la clave no existe.
         */
        public int getInt(String key) {
            Object v = get(key);
            if (v == null) throw new RuntimeException("Clave no encontrada: " + key);
            return ((Number) v).intValue();
        }

        /**
         * Retorna el valor como entero, o el valor por defecto si la clave no existe.
         *
         * @param key        Clave del atributo.
         * @param defaultVal Valor por defecto si la clave no existe.
         * @return Valor como int o defaultVal.
         */
        public int getInt(String key, int defaultVal) {
            Object v = get(key); return v == null ? defaultVal : ((Number) v).intValue();
        }

        /**
         * Retorna el valor como arreglo JSON.
         *
         * @param key Clave del atributo.
         * @return Objeto {@link JsonArray} o {@code null} si no existe.
         */
        public JsonArray getArray(String key) { return (JsonArray) get(key); }

        /**
         * Retorna el valor como objeto JSON anidado.
         *
         * @param key Clave del atributo.
         * @return Objeto {@link JsonObject} o {@code null} si no existe.
         */
        public JsonObject getObject(String key) { return (JsonObject) get(key); }

        /**
         * Indica si la clave existe en este objeto.
         *
         * @param key Clave a verificar.
         * @return {@code true} si la clave está presente.
         */
        public boolean has(String key) { return get(key) != null; }
    }

    /**
     * Representa un arreglo JSON como una lista de valores de tamaño dinámico.
     */
    public static class JsonArray {
        private Object[] data = new Object[32];
        private int size = 0;

        /**
         * Agrega un elemento al final del arreglo.
         *
         * @param o Elemento a agregar.
         */
        public void add(Object o) {
            if (size == data.length) {
                Object[] nd = new Object[data.length * 2];
                System.arraycopy(data, 0, nd, 0, size);
                data = nd;
            }
            data[size++] = o;
        }

        /**
         * Retorna el número de elementos en el arreglo.
         *
         * @return Tamaño del arreglo.
         */
        public int size() { return size; }

        /**
         * Retorna el elemento en la posición {@code i}.
         *
         * @param i Índice (0-based).
         * @return Elemento en esa posición.
         */
        public Object get(int i) { return data[i]; }

        /**
         * Retorna el elemento en la posición {@code i} como {@link JsonObject}.
         *
         * @param i Índice (0-based).
         * @return Objeto JSON en esa posición.
         */
        public JsonObject getObject(int i) { return (JsonObject) data[i]; }

        /**
         * Retorna el elemento en la posición {@code i} como String.
         *
         * @param i Índice (0-based).
         * @return Representación en String del elemento.
         */
        public String getString(int i) { return data[i].toString(); }
    }
}
