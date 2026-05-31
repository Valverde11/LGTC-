package util;

public class MinHeap<T> {

    // ── Internal entry ───────────────────────────────────────────────────────

    private static class Entry<T> {
        T item;
        double key;
        Entry(T item, double key) { this.item = item; this.key = key; }
    }

    // ── Fields ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Entry<T>[] heap = new Entry[16];
    private int size;

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Insert item with given priority key. O(log n).
     */
    public void insert(T item, double key) {
        if (size == heap.length) grow();
        heap[size] = new Entry<>(item, key);
        siftUp(size);
        size++;
    }

    /**
     * Remove and return the item with the minimum key. O(log n).
     */
    public T extractMin() {
        if (size == 0) throw new RuntimeException("PriorityQueue is empty");
        T min = heap[0].item;
        size--;
        if (size > 0) {
            heap[0] = heap[size];
            heap[size] = null;
            siftDown(0);
        } else {
            heap[0] = null;
        }
        return min;
    }

    /**
     * Return the item with the minimum key without removing. O(1).
     */
    public T peekMin() {
        if (size == 0) throw new RuntimeException("PriorityQueue is empty");
        return heap[0].item;
    }

    /**
     * Return the key of the minimum element. O(1).
     */
    public double peekMinKey() {
        if (size == 0) throw new RuntimeException("PriorityQueue is empty");
        return heap[0].key;
    }

    /**
     * Decrease the key of an existing item. O(n + log n).
     * If item is not found, it is inserted with the new key.
     */
    public void decreaseKey(T item, double newKey) {
        for (int i = 0; i < size; i++) {
            if (heap[i].item.equals(item)) {
                if (newKey < heap[i].key) {
                    heap[i].key = newKey;
                    siftUp(i);
                }
                return;
            }
        }
        // item not present — insert
        insert(item, newKey);
    }

    /**
     * Return the current key of an item, or Double.MAX_VALUE if not present.
     */
    public double getKey(T item) {
        for (int i = 0; i < size; i++)
            if (heap[i].item.equals(item)) return heap[i].key;
        return Double.MAX_VALUE;
    }

    /** True if item is present in the heap. O(n). */
    public boolean contains(T item) {
        for (int i = 0; i < size; i++)
            if (heap[i].item.equals(item)) return true;
        return false;
    }

    public boolean isEmpty() { return size == 0; }
    public int size()        { return size; }

    public void clear() {
        for (int i = 0; i < size; i++) heap[i] = null;
        size = 0;
    }

    // ── Heap operations ──────────────────────────────────────────────────────

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (heap[parent].key <= heap[i].key) break;
            swap(parent, i);
            i = parent;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int left  = 2 * i + 1;
            int right = 2 * i + 2;
            int smallest = i;
            if (left  < size && heap[left].key  < heap[smallest].key) smallest = left;
            if (right < size && heap[right].key < heap[smallest].key) smallest = right;
            if (smallest == i) break;
            swap(i, smallest);
            i = smallest;
        }
    }

    private void swap(int a, int b) {
        Entry<T> tmp = heap[a];
        heap[a] = heap[b];
        heap[b] = tmp;
    }

    @SuppressWarnings("unchecked")
    private void grow() {
        Entry<T>[] newHeap = new Entry[heap.length * 2];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }
}
