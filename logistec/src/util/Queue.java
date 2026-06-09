package util;

/**
 * Cola FIFO (First In, First Out) genérica, implementada sobre {@link LinkedList}.
 * Usada por el algoritmo BFS y otros componentes del sistema.
 *
 * <p>Todas las operaciones son O(1).
 *
 * @param <T> Tipo de los elementos almacenados en la cola.
 * @author LogísTEC Team
 * @version 1.0
 */
public class Queue<T> {

    private final LinkedList<T> list = new LinkedList<>();

    /**
     * Agrega un elemento al final de la cola. O(1).
     *
     * @param item Elemento a encolar.
     */
    public void enqueue(T item) { list.addLast(item); }

    /**
     * Elimina y retorna el elemento al frente de la cola. O(1).
     *
     * @return El elemento más antiguo en la cola.
     * @throws RuntimeException Si la cola está vacía.
     */
    public T dequeue() { return list.removeFirst(); }

    /**
     * Retorna el elemento al frente de la cola sin eliminarlo. O(1).
     *
     * @return El elemento más antiguo en la cola.
     * @throws RuntimeException Si la cola está vacía.
     */
    public T peek() { return list.peekFirst(); }

    /**
     * Indica si la cola está vacía.
     *
     * @return {@code true} si no hay elementos en la cola.
     */
    public boolean isEmpty() { return list.isEmpty(); }

    /**
     * Retorna el número de elementos en la cola.
     *
     * @return Tamaño actual de la cola.
     */
    public int size() { return list.size(); }

    /**
     * Elimina todos los elementos de la cola.
     */
    public void clear() { list.clear(); }

    @Override
    public String toString() { return list.toString(); }
}
