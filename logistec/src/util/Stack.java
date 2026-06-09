package util;

/**
 * Pila LIFO (Last In, First Out) genérica, implementada sobre {@link LinkedList}.
 * Usada por el algoritmo DFS iterativo y otros componentes del sistema.
 *
 * <p>Todas las operaciones son O(1).
 *
 * @param <T> Tipo de los elementos almacenados en la pila.
 * @author LogísTEC Team
 * @version 1.0
 */
public class Stack<T> {

    private final LinkedList<T> list = new LinkedList<>();

    /**
     * Apila un elemento en la cima. O(1).
     *
     * @param item Elemento a apilar.
     */
    public void push(T item) { list.addFirst(item); }

    /**
     * Desapila y retorna el elemento en la cima. O(1).
     *
     * @return El elemento más recientemente apilado.
     * @throws RuntimeException Si la pila está vacía.
     */
    public T pop() { return list.removeFirst(); }

    /**
     * Retorna el elemento en la cima sin desapilarlo. O(1).
     *
     * @return El elemento más recientemente apilado.
     * @throws RuntimeException Si la pila está vacía.
     */
    public T peek() { return list.peekFirst(); }

    /**
     * Indica si la pila está vacía.
     *
     * @return {@code true} si no hay elementos en la pila.
     */
    public boolean isEmpty() { return list.isEmpty(); }

    /**
     * Retorna el número de elementos en la pila.
     *
     * @return Tamaño actual de la pila.
     */
    public int size() { return list.size(); }

    /**
     * Elimina todos los elementos de la pila.
     */
    public void clear() { list.clear(); }

    @Override
    public String toString() { return list.toString(); }
}
