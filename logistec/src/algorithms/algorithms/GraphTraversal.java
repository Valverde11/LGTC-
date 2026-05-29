package algorithms;

import graph.Graph;
import util.LinkedList;
import util.Queue;
import util.Stack;

public class GraphTraversal {

    private GraphTraversal() {} // utility class

    // ── BFS ──────────────────────────────────────────────────────────────────

    public static LinkedList<Integer> bfs(Graph g, int src) {
        int V = g.numVertices();
        boolean[] visited = new boolean[V];
        LinkedList<Integer> order = new LinkedList<>();
        Queue<Integer> queue = new Queue<>();

        visited[src] = true;
        queue.enqueue(src);

        while (!queue.isEmpty()) {
            int u = queue.dequeue();
            order.addLast(u);
            for (Graph.AdjEntry e : g.adj(u)) {
                if (!visited[e.target]) {
                    visited[e.target] = true;
                    queue.enqueue(e.target);
                }
            }
        }
        return order;
    }

    /**
     * BFS that also fills the {@code parent} array (used for path reconstruction).
     *
     * @param  g       the graph
     * @param  src     source vertex index
     * @param  parent  int[V] output — parent[v] = predecessor of v in BFS tree, or -1
     * @return list of vertices in BFS order
     */
    public static LinkedList<Integer> bfs(Graph g, int src, int[] parent) {
        int V = g.numVertices();
        boolean[] visited = new boolean[V];
        LinkedList<Integer> order = new LinkedList<>();
        Queue<Integer> queue = new Queue<>();

        for (int i = 0; i < V; i++) parent[i] = -1;
        visited[src] = true;
        queue.enqueue(src);

        while (!queue.isEmpty()) {
            int u = queue.dequeue();
            order.addLast(u);
            for (Graph.AdjEntry e : g.adj(u)) {
                if (!visited[e.target]) {
                    visited[e.target] = true;
                    parent[e.target] = u;
                    queue.enqueue(e.target);
                }
            }
        }
        return order;
    }

    // ── DFS ──────────────────────────────────────────────────────────────────

    /**
     * Depth-First Search from source vertex {@code src} (iterative, using a stack).
     * <p>
     * Avoids stack-overflow for large graphs — suitable for production use.
     *
     * @param  g   the graph
     * @param  src source vertex index
     * @return list of vertex indices in DFS visit order
     */
    public static LinkedList<Integer> dfs(Graph g, int src) {
        int V = g.numVertices();
        boolean[] visited = new boolean[V];
        LinkedList<Integer> order = new LinkedList<>();
        Stack<Integer> stack = new Stack<>();

        stack.push(src);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (visited[u]) continue;
            visited[u] = true;
            order.addLast(u);
            // Push neighbours in reverse order so we visit in natural order
            LinkedList<Graph.AdjEntry> neighbours = g.adj(u);
            // collect into array then push reversed
            Object[] arr = neighbours.toArray();
            for (int i = arr.length - 1; i >= 0; i--) {
                Graph.AdjEntry e = (Graph.AdjEntry) arr[i];
                if (!visited[e.target]) stack.push(e.target);
            }
        }
        return order;
    }

    /**
     * Recursive DFS pre-order starting at {@code src}.
     * Fills {@code order} with visited vertex indices.
     * Used for MST pre-order traversal (heuristic routing).
     *
     * @param g       the graph (or MST adjacency list)
     * @param src     current vertex
     * @param visited boolean[V] visited flags
     * @param order   output list
     */
    public static void dfsPreorder(Graph g, int src, boolean[] visited, LinkedList<Integer> order) {
        visited[src] = true;
        order.addLast(src);
        for (Graph.AdjEntry e : g.adj(src)) {
            if (!visited[e.target]) {
                dfsPreorder(g, e.target, visited, order);
            }
        }
    }

    // ── Connected components ─────────────────────────────────────────────────

    /**
     * Return the number of connected components in the graph.
     * O(V + E).
     */
    public static int connectedComponents(Graph g) {
        int V = g.numVertices();
        boolean[] visited = new boolean[V];
        int count = 0;
        for (int i = 0; i < V; i++) {
            if (!visited[i]) {
                bfsVisit(g, i, visited);
                count++;
            }
        }
        return count;
    }

    private static void bfsVisit(Graph g, int src, boolean[] visited) {
        Queue<Integer> q = new Queue<>();
        visited[src] = true;
        q.enqueue(src);
        while (!q.isEmpty()) {
            int u = q.dequeue();
            for (Graph.AdjEntry e : g.adj(u))
                if (!visited[e.target]) { visited[e.target] = true; q.enqueue(e.target); }
        }
    }
}
