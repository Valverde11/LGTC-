package util;


public class Queue<T> {

    private final LinkedList<T> list = new LinkedList<>();

    /** Add element to the back. O(1). */
    public void enqueue(T item) { list.addLast(item); }

    /** Remove and return element from the front. O(1). */
    public T dequeue() { return list.removeFirst(); }

    /** Return front element without removing. O(1). */
    public T peek() { return list.peekFirst(); }

    public boolean isEmpty() { return list.isEmpty(); }
    public int size()        { return list.size(); }
    public void clear()      { list.clear(); }

    @Override
    public String toString() { return list.toString(); }

    haolascas
}
