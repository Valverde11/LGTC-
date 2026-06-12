package algorithms;

import graph.Graph;
import util.LinkedList;
import util.Queue;
import util.Stack;

/**
 * Algoritmos de recorrido de grafos: BFS y DFS. Complejidad O(V+E).
 * Clase utilitaria: no instanciable.
 *
 * @author Alejandro Arias Lopez
 */
public class GraphTraversal {

    /** Constructor privado — clase utilitaria. */
    private GraphTraversal() {}

    /**
     * BFS desde {@code src}. Visita por niveles usando cola FIFO.
     *
     * @param g   Grafo a recorrer.
     * @param src Vértice origen.
     * @return Lista de vértices en orden BFS.
     */
    public static LinkedList<Integer> bfs(Graph g, int src) {
        int V = g.numVertices();
        boolean[]           visited = new boolean[V];
        LinkedList<Integer> order   = new LinkedList<>();
        Queue<Integer>      queue   = new Queue<>();

        // Arrancamos marcando el origen como visitado antes de encolarlo
        // (evita procesarlo más de una vez si aparece en múltiples listas de adyacencia)
        visited[src] = true;
        queue.enqueue(src);

        while (!queue.isEmpty()) {
            int u = queue.dequeue(); // procesamos el frente de la cola
            order.addLast(u);        // registramos el orden de visita

            // Exploramos todos los vecinos de u
            for (Graph.AdjEntry e : g.adj(u)) {
                if (!visited[e.target]) {
                    visited[e.target] = true;      // marcamos antes de encolar
                    queue.enqueue(e.target);        // añadimos al siguiente nivel
                }
            }
        }
        return order;
    }

    /**
     * BFS con registro de predecesores para reconstrucción de caminos.
     *
     * @param g      Grafo a recorrer.
     * @param src    Vértice origen.
     * @param parent Arreglo de salida; {@code parent[v]} = predecesor de v en BFS o -1.
     * @return Lista de vértices en orden BFS.
     */
    public static LinkedList<Integer> bfs(Graph g, int src, int[] parent) {
        int V = g.numVertices();
        boolean[]           visited = new boolean[V];
        LinkedList<Integer> order   = new LinkedList<>();
        Queue<Integer>      queue   = new Queue<>();

        // Inicializamos todos los predecesores en -1 (sin predecesor conocido)
        for (int i = 0; i < V; i++) parent[i] = -1;

        visited[src] = true;
        queue.enqueue(src);

        while (!queue.isEmpty()) {
            int u = queue.dequeue();
            order.addLast(u);
            for (Graph.AdjEntry e : g.adj(u)) {
                if (!visited[e.target]) {
                    visited[e.target] = true;
                    parent[e.target]  = u;          // u es el predecesor de e.target
                    queue.enqueue(e.target);
                }
            }
        }
        return order;
    }

    /**
     * DFS iterativo desde {@code src} usando pila LIFO.
     * Versión iterativa para evitar StackOverflowError en grafos grandes.
     *
     * @param g   Grafo a recorrer.
     * @param src Vértice origen.
     * @return Lista de vértices en orden DFS.
     */
    public static LinkedList<Integer> dfs(Graph g, int src) {
        int V = g.numVertices();
        boolean[]           visited = new boolean[V];
        LinkedList<Integer> order   = new LinkedList<>();
        Stack<Integer>      stack   = new Stack<>();

        stack.push(src);

        while (!stack.isEmpty()) {
            int u = stack.pop();

            // Verificamos al desapilar (no al apilar) porque un mismo nodo
            // puede ser apilado múltiples veces antes de ser procesado
            if (visited[u]) continue;

            visited[u] = true;
            order.addLast(u);

            // Apilamos vecinos en orden inverso para visitar en el orden natural
            Object[] arr = g.adj(u).toArray();
            for (int i = arr.length - 1; i >= 0; i--) {
                Graph.AdjEntry e = (Graph.AdjEntry) arr[i];
                if (!visited[e.target]) stack.push(e.target);
            }
        }
        return order;
    }

    /**
     * DFS preorden recursivo desde {@code src}.
     * Usado para recorrer el MST en la heurística MST-based de ruteo.
     *
     * @param g       Grafo (o subgrafo MST) a recorrer.
     * @param src     Vértice actual en la recursión.
     * @param visited Arreglo de flags de visita compartido entre llamadas.
     * @param order   Lista de salida donde se acumulan los vértices en preorden.
     */
    public static void dfsPreorder(Graph g, int src, boolean[] visited, LinkedList<Integer> order) {
        // Preorden: procesamos el nodo ANTES de recurrir en sus hijos
        visited[src] = true;
        order.addLast(src);

        // Recursamos en cada vecino no visitado (en el MST cada nodo tiene a lo sumo un hijo por rama)
        for (Graph.AdjEntry e : g.adj(src)) {
            if (!visited[e.target]) {
                dfsPreorder(g, e.target, visited, order);
            }
        }
    }

    /**
     * Cuenta el número de componentes conexas usando BFS. O(V+E).
     *
     * @param g Grafo a analizar.
     * @return Número de componentes conexas.
     */
    public static int connectedComponents(Graph g) {
        int       V       = g.numVertices();
        boolean[] visited = new boolean[V];
        int       count   = 0;

        // Lanzamos un BFS desde cada vértice no visitado.
        // Cada BFS cubre exactamente una componente conexa.
        for (int i = 0; i < V; i++) {
            if (!visited[i]) {
                bfsVisit(g, i, visited); // marca todos los alcanzables desde i
                count++;                 // una nueva componente descubierta
            }
        }
        return count;
    }

    /**
     * BFS auxiliar que solo marca vértices como visitados (sin retornar orden).
     * Usado por {@link #connectedComponents(Graph)}.
     *
     * @param g       Grafo a recorrer.
     * @param src     Vértice de inicio.
     * @param visited Arreglo compartido de flags de visita.
     */
    private static void bfsVisit(Graph g, int src, boolean[] visited) {
        Queue<Integer> q = new Queue<>();
        visited[src] = true;
        q.enqueue(src);
        while (!q.isEmpty()) {
            int u = q.dequeue();
            for (Graph.AdjEntry e : g.adj(u)) {
                if (!visited[e.target]) {
                    visited[e.target] = true;
                    q.enqueue(e.target);
                }
            }
        }
    }
}
