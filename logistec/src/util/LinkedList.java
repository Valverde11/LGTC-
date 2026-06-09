package util;

import java.util.Iterator;

/**
 * Lista enlazada simple genérica (TDA Lista Enlazada).
 *
 * <p>Implementación propia que reemplaza {@code java.util.ArrayList} y
 * {@code java.util.LinkedList}. Sirve como base para {@link Queue} y {@link Stack},
 * y como lista de adyacencia del grafo.
 *
 * @param <T> Tipo de los elementos almacenados.
 * @author LogísTEC Team
 * @version 1.0
 */
public class LinkedList<T> implements Iterable<T> {

    // =========================================================================
    // NODO INTERNO
    // Cada nodo guarda un dato y un puntero al siguiente.
    // Al ser privado y estático, no tiene acceso a los genéricos de la clase
    // exterior, por eso se declara como Node<T> con su propio tipo genérico.
    // =========================================================================
    private static class Node<T> {
        T       data; // dato almacenado en este nodo
        Node<T> next; // referencia al siguiente nodo (null si es el último)

        Node(T data) {
            this.data = data;
            this.next = null; // por defecto sin sucesor
        }
    }

    // =========================================================================
    // CAMPOS DE INSTANCIA
    // head → primer nodo de la lista
    // tail → último nodo (permite addLast en O(1) sin recorrer toda la lista)
    // size → contador de elementos para O(1) en size()
    // =========================================================================
    private Node<T> head; // primer nodo; null si la lista está vacía
    private Node<T> tail; // último nodo; null si la lista está vacía
    private int     size; // cantidad de elementos actualmente en la lista

    // =========================================================================
    // INSERCIÓN
    // =========================================================================

    /**
     * Agrega un elemento al final de la lista. O(1).
     *
     * @param item Elemento a agregar.
     */
    public void addLast(T item) {
        Node<T> node = new Node<>(item);
        if (head == null) {
            // Lista vacía: el nuevo nodo es tanto cabeza como cola
            head = tail = node;
        } else {
            // Encadenamos el nuevo nodo detrás del último y actualizamos tail
            tail.next = node;
            tail = node;
        }
        size++;
    }

    /**
     * Agrega un elemento al inicio de la lista. O(1).
     *
     * @param item Elemento a agregar.
     */
    public void addFirst(T item) {
        Node<T> node = new Node<>(item);
        node.next = head;  // el nuevo nodo apunta al anterior primero
        head = node;       // head ahora es el nuevo nodo
        if (tail == null) {
            // Si la lista estaba vacía, tail también debe apuntar al único nodo
            tail = node;
        }
        size++;
    }

    // =========================================================================
    // ELIMINACIÓN
    // =========================================================================

    /**
     * Elimina y retorna el primer elemento de la lista. O(1).
     *
     * @return El primer elemento.
     * @throws RuntimeException Si la lista está vacía.
     */
    public T removeFirst() {
        if (head == null) throw new RuntimeException("La lista está vacía");
        T data = head.data;   // guardamos el dato antes de desvincular el nodo
        head = head.next;     // avanzamos head al siguiente nodo
        if (head == null) {
            // La lista quedó vacía; tail también debe quedar en null
            tail = null;
        }
        size--;
        return data;
    }

    /**
     * Elimina y retorna el elemento en la posición {@code i}. O(n).
     *
     * @param i Índice del elemento (0-based).
     * @return El elemento eliminado.
     * @throws IndexOutOfBoundsException Si el índice está fuera del rango.
     */
    public T remove(int i) {
        rangeCheck(i);
        if (i == 0) return removeFirst(); // caso especial O(1)

        // Recorremos hasta el nodo ANTERIOR al que queremos eliminar
        Node<T> prev = head;
        for (int j = 0; j < i - 1; j++) prev = prev.next;

        T data = prev.next.data;       // guardamos el dato del nodo a eliminar
        prev.next = prev.next.next;    // saltamos el nodo eliminado

        if (prev.next == null) {
            // Si eliminamos el último nodo, actualizamos tail
            tail = prev;
        }
        size--;
        return data;
    }

    /**
     * Elimina la primera ocurrencia del elemento {@code item}. O(n).
     *
     * @param item Elemento a eliminar.
     * @return {@code true} si fue encontrado y eliminado.
     */
    public boolean remove(T item) {
        Node<T> prev = null;
        Node<T> cur  = head;

        // Recorremos buscando el primer nodo cuyo dato sea igual a item
        while (cur != null) {
            if (cur.data.equals(item)) {
                // Encontrado: desvinculamos el nodo
                if (prev == null) {
                    head = cur.next; // era el primero
                } else {
                    prev.next = cur.next; // saltamos cur
                }
                if (cur.next == null) tail = prev; // era el último
                size--;
                return true;
            }
            prev = cur;
            cur  = cur.next;
        }
        return false; // no se encontró
    }

    // =========================================================================
    // ACCESO
    // =========================================================================

    /**
     * Retorna el primer elemento sin eliminarlo. O(1).
     *
     * @return El primer elemento.
     * @throws RuntimeException Si la lista está vacía.
     */
    public T peekFirst() {
        if (head == null) throw new RuntimeException("La lista está vacía");
        return head.data;
    }

    /**
     * Retorna el elemento en la posición {@code i}. O(n).
     *
     * @param i Índice (0-based).
     * @return Elemento en esa posición.
     * @throws IndexOutOfBoundsException Si el índice es inválido.
     */
    public T get(int i) {
        rangeCheck(i);
        // Recorremos desde head hasta la posición i
        Node<T> cur = head;
        for (int j = 0; j < i; j++) cur = cur.next;
        return cur.data;
    }

    /**
     * Reemplaza el elemento en la posición {@code i}. O(n).
     *
     * @param i    Índice (0-based).
     * @param item Nuevo elemento.
     */
    public void set(int i, T item) {
        rangeCheck(i);
        Node<T> cur = head;
        for (int j = 0; j < i; j++) cur = cur.next;
        cur.data = item; // reemplazamos el dato en el nodo encontrado
    }

    // =========================================================================
    // BÚSQUEDA
    // =========================================================================

    /**
     * Indica si la lista contiene al menos una ocurrencia del elemento. O(n).
     *
     * @param item Elemento a buscar.
     * @return {@code true} si el elemento está presente.
     */
    public boolean contains(T item) {
        return indexOf(item) >= 0; // reutilizamos indexOf
    }

    /**
     * Retorna el índice de la primera ocurrencia del elemento, o -1. O(n).
     *
     * @param item Elemento a buscar.
     * @return Índice de la primera ocurrencia, o -1 si no se encuentra.
     */
    public int indexOf(T item) {
        Node<T> cur = head;
        for (int i = 0; i < size; i++) {
            if (cur.data.equals(item)) return i; // encontrado
            cur = cur.next;
        }
        return -1; // no encontrado
    }

    // =========================================================================
    // ESTADO
    // =========================================================================

    /** @return Número de elementos en la lista. */
    public int size()       { return size; }

    /** @return {@code true} si la lista no tiene elementos. */
    public boolean isEmpty(){ return size == 0; }

    /** Elimina todos los elementos. Las referencias se liberan para el GC. O(1). */
    public void clear()     { head = tail = null; size = 0; }

    // =========================================================================
    // CONVERSIÓN
    // =========================================================================

    /**
     * Retorna un arreglo con todos los elementos en orden. O(n).
     *
     * @return Arreglo de tipo {@code T[]}.
     */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] arr = new Object[size];
        Node<T>  cur = head;
        for (int i = 0; i < size; i++) {
            arr[i] = cur.data;
            cur    = cur.next;
        }
        return (T[]) arr;
    }

    // =========================================================================
    // ITERABLE — permite usar la lista en un for-each
    // =========================================================================

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Node<T> cur = head; // cursor interno que avanza de nodo en nodo

            @Override public boolean hasNext() { return cur != null; }

            @Override public T next() {
                T d = cur.data;
                cur = cur.next; // avanzamos el cursor antes de retornar
                return d;
            }
        };
    }

    // =========================================================================
    // AUXILIARES PRIVADOS
    // =========================================================================

    /**
     * Verifica que el índice {@code i} esté dentro del rango [0, size).
     *
     * @param i Índice a verificar.
     * @throws IndexOutOfBoundsException Si el índice está fuera de rango.
     */
    private void rangeCheck(int i) {
        if (i < 0 || i >= size)
            throw new IndexOutOfBoundsException(
                "Índice " + i + " fuera de rango para tamaño " + size);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> cur = head;
        while (cur != null) {
            sb.append(cur.data);
            if (cur.next != null) sb.append(", ");
            cur = cur.next;
        }
        return sb.append("]").toString();
    }
}
