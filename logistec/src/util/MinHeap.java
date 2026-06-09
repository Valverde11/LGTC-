package util;

/**
 * Cola de prioridad mínima implementada como heap binario sobre un arreglo dinámico.
 *
 * <p>Reemplaza {@code java.util.PriorityQueue}. La raíz siempre contiene
 * el elemento con la menor clave numérica (mínimo). El arreglo crece x2 cuando se llena.
 *
 * <p>Invariante del heap: {@code key[padre] <= key[hijo]} para todo nodo.
 *
 * @param <T> Tipo de los elementos almacenados.
 * @author LogísTEC Team
 * @version 1.0
 */
public class MinHeap<T> {

    // =========================================================================
    // ENTRADA INTERNA (par elemento + clave)
    // Encapsulamos item y key juntos para no necesitar dos arreglos paralelos.
    // =========================================================================
    private static class Entry<T> {
        T      item; // el elemento real almacenado
        double key;  // su prioridad numérica (menor = más urgente)

        Entry(T item, double key) {
            this.item = item;
            this.key  = key;
        }
    }

    // =========================================================================
    // CAMPOS
    // El heap se representa como un arreglo donde para el nodo en índice i:
    //   padre   = (i - 1) / 2
    //   hijo izq = 2*i + 1
    //   hijo der = 2*i + 2
    // =========================================================================
    @SuppressWarnings("unchecked")
    private Entry<T>[] heap = new Entry[16]; // arreglo interno; crece dinámicamente
    private int        size;                 // número de elementos activos en heap[0..size-1]

    /**
     * Inserta un elemento con la clave de prioridad dada. O(log n).
     *
     * @param item Elemento a insertar.
     * @param key  Clave de prioridad (menor = mayor urgencia).
     */
    public void insert(T item, double key) {
        if (size == heap.length) grow(); // el arreglo está lleno, duplicamos capacidad

        // Insertamos al final del arreglo y luego "subimos" para restaurar el heap
        heap[size] = new Entry<>(item, key);
        siftUp(size); // restaura la invariante hacia arriba
        size++;
    }

    /**
     * Extrae y retorna el elemento con la clave mínima (la raíz). O(log n).
     *
     * @return Elemento con la menor clave.
     * @throws RuntimeException Si el heap está vacío.
     */
    public T extractMin() {
        if (size == 0) throw new RuntimeException("La cola de prioridad está vacía");

        T min = heap[0].item; // guardamos la raíz antes de sobreescribirla

        size--;
        if (size > 0) {
            // Movemos el último elemento a la raíz y "bajamos" para restaurar el heap
            heap[0]    = heap[size];
            heap[size] = null;       // liberamos referencia para el GC
            siftDown(0);             // restaura la invariante hacia abajo
        } else {
            heap[0] = null; // el heap quedó vacío
        }
        return min;
    }

    /**
     * Retorna el elemento mínimo sin extraerlo. O(1).
     *
     * @return Elemento en la raíz del heap.
     * @throws RuntimeException Si el heap está vacío.
     */
    public T peekMin() {
        if (size == 0) throw new RuntimeException("La cola de prioridad está vacía");
        return heap[0].item; // la raíz siempre es el mínimo
    }

    /**
     * Retorna la clave del elemento mínimo sin extraerlo. O(1).
     *
     * @return Clave del elemento raíz.
     */
    public double peekMinKey() {
        if (size == 0) throw new RuntimeException("La cola de prioridad está vacía");
        return heap[0].key;
    }

    /**
     * Reduce la clave de un elemento ya presente. O(n búsqueda + log n sift-up).
     * Si el elemento no está, se inserta con la nueva clave.
     *
     * @param item   Elemento a actualizar.
     * @param newKey Nueva clave (debe ser menor que la actual para tener efecto).
     */
    public void decreaseKey(T item, double newKey) {
        // Búsqueda lineal O(n) — limitación conocida del diseño
        for (int i = 0; i < size; i++) {
            if (heap[i].item.equals(item)) {
                if (newKey < heap[i].key) {
                    heap[i].key = newKey; // actualizamos la clave
                    siftUp(i);            // subimos el nodo si ahora viola el heap con su padre
                }
                return; // encontrado y procesado
            }
        }
        // No estaba en el heap: lo insertamos
        insert(item, newKey);
    }

    /**
     * Retorna la clave de un elemento, o {@code Double.MAX_VALUE} si no está. O(n).
     *
     * @param item Elemento a buscar.
     * @return Clave actual o {@code Double.MAX_VALUE}.
     */
    public double getKey(T item) {
        for (int i = 0; i < size; i++)
            if (heap[i].item.equals(item)) return heap[i].key;
        return Double.MAX_VALUE; // valor centinela "no encontrado"
    }

    /**
     * Indica si el elemento está presente. O(n).
     *
     * @param item Elemento a buscar.
     * @return {@code true} si está en el heap.
     */
    public boolean contains(T item) {
        for (int i = 0; i < size; i++)
            if (heap[i].item.equals(item)) return true;
        return false;
    }

    /** @return {@code true} si el heap está vacío. */
    public boolean isEmpty() { return size == 0; }

    /** @return Número de elementos en el heap. */
    public int size() { return size; }

    /** Elimina todos los elementos del heap. */
    public void clear() {
        // Liberamos cada referencia para que el GC pueda reclamarlas
        for (int i = 0; i < size; i++) heap[i] = null;
        size = 0;
    }

    // =========================================================================
    // OPERACIONES INTERNAS DE HEAP
    // =========================================================================

    /**
     * Sube el nodo en la posición {@code i} mientras su clave sea menor
     * que la de su padre. Restaura la invariante heap hacia arriba.
     *
     * @param i Índice del nodo a subir.
     */
    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2; // índice del padre
            if (heap[parent].key <= heap[i].key) break; // invariante satisfecha
            swap(parent, i); // intercambiamos hijo con padre
            i = parent;      // continuamos desde la nueva posición
        }
    }

    /**
     * Baja el nodo en la posición {@code i} mientras su clave sea mayor
     * que la del hijo más pequeño. Restaura la invariante heap hacia abajo.
     *
     * @param i Índice del nodo a bajar.
     */
    private void siftDown(int i) {
        while (true) {
            int left     = 2 * i + 1; // hijo izquierdo
            int right    = 2 * i + 2; // hijo derecho
            int smallest = i;         // asumimos que el padre es el menor

            // Verificamos si el hijo izquierdo es menor que el padre actual
            if (left < size && heap[left].key < heap[smallest].key)
                smallest = left;

            // Verificamos si el hijo derecho es aún menor
            if (right < size && heap[right].key < heap[smallest].key)
                smallest = right;

            if (smallest == i) break; // el padre ya es el mínimo; heap ok

            swap(i, smallest); // intercambiamos con el hijo menor
            i = smallest;      // continuamos bajando desde la nueva posición
        }
    }

    /**
     * Intercambia dos entradas en el arreglo del heap.
     *
     * @param a Índice de la primera entrada.
     * @param b Índice de la segunda entrada.
     */
    private void swap(int a, int b) {
        Entry<T> tmp = heap[a];
        heap[a] = heap[b];
        heap[b] = tmp;
    }

    /**
     * Duplica la capacidad del arreglo interno cuando se llena.
     * Copia los elementos existentes al nuevo arreglo con System.arraycopy.
     */
    @SuppressWarnings("unchecked")
    private void grow() {
        Entry<T>[] newHeap = new Entry[heap.length * 2];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }
}
