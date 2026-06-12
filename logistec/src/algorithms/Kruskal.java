package algorithms;

import graph.Edge;
import graph.Graph;
import util.LinkedList;
import util.UnionFind;

/**
 * Algoritmo de Kruskal para el Árbol de Expansión Mínima (MST).
 * Ordena aristas por peso y las agrega si no forman ciclo. O(E log E).
 *
 * @author Alejandro Arias Lopez
 */
public class Kruskal {

    private final LinkedList<Edge> mstEdges;    // aristas seleccionadas para el MST
    private final int              totalWeight;  // suma de pesos de las aristas del MST
    private final long             elapsedNano;  // tiempo de ejecución en nanosegundos

    /**
     * Ejecuta Kruskal sobre el grafo {@code g}.
     *
     * @param g Grafo no dirigido ponderado.
     */
    public Kruskal(Graph g) {
        long start = System.nanoTime(); // marcamos el inicio para medir tiempo empírico

        int V = g.numVertices();

        // ── Paso 1: Recopilamos todas las aristas en un arreglo ───────────────
        // Kruskal necesita ordenar las aristas; para eso necesitamos acceso por índice.
        LinkedList<Edge> allEdges = g.getAllEdges();
        Edge[] edgeArr = new Edge[allEdges.size()];
        int idx = 0;
        for (Edge e : allEdges) edgeArr[idx++] = e;

        // ── Paso 2: Ordenamos por peso ascendente (merge sort propio) ─────────
        // Merge sort es O(E log E). No usamos Arrays.sort para cumplir la restricción
        // de no usar java.util en el núcleo algorítmico.
        sortEdges(edgeArr, 0, edgeArr.length - 1);

        // ── Paso 3: Procesamos aristas en orden y construimos el MST ──────────
        UnionFind uf = new UnionFind(V); // detecta ciclos en O(α(n))
        mstEdges = new LinkedList<>();
        int total = 0;

        for (Edge e : edgeArr) {
            // Intentamos unir los conjuntos de u y v.
            // union() retorna false si ya estaban en el mismo conjunto (formarían ciclo).
            if (uf.union(e.getU(), e.getV())) {
                mstEdges.addLast(e);    // arista aceptada en el MST
                total += e.getWeight(); // acumulamos el costo

                // El MST de un grafo conexo tiene exactamente V-1 aristas.
                // Si ya las tenemos, terminamos temprano.
                if (mstEdges.size() == V - 1) break;
            }
            // Si union() retornó false, la arista forma un ciclo y se descarta.
        }

        totalWeight = total;
        elapsedNano = System.nanoTime() - start;
    }

    // =========================================================================
    // MERGE SORT PROPIO — O(E log E)
    // =========================================================================

    /**
     * Ordena el subarreglo {@code arr[lo..hi]} por peso ascendente usando merge sort.
     *
     * @param arr Arreglo de aristas.
     * @param lo  Límite inferior del subarreglo (inclusivo).
     * @param hi  Límite superior del subarreglo (inclusivo).
     */
    private void sortEdges(Edge[] arr, int lo, int hi) {
        if (lo >= hi) return;             // caso base: subarreglo de 1 elemento (ya ordenado)
        int mid = (lo + hi) / 2;         // punto de división
        sortEdges(arr, lo, mid);          // ordenamos la mitad izquierda
        sortEdges(arr, mid + 1, hi);      // ordenamos la mitad derecha
        merge(arr, lo, mid, hi);          // fusionamos ambas mitades ordenadas
    }

    /**
     * Fusiona dos mitades ya ordenadas de {@code arr} en un solo subarreglo ordenado.
     *
     * @param arr Arreglo que contiene ambas mitades.
     * @param lo  Inicio de la primera mitad.
     * @param mid Fin de la primera mitad.
     * @param hi  Fin de la segunda mitad.
     */
    private void merge(Edge[] arr, int lo, int mid, int hi) {
        Edge[] tmp = new Edge[hi - lo + 1]; // arreglo temporal de fusión
        int i = lo, j = mid + 1, k = 0;

        // Comparamos elemento a elemento de ambas mitades y copiamos el menor
        while (i <= mid && j <= hi)
            tmp[k++] = arr[i].compareTo(arr[j]) <= 0 ? arr[i++] : arr[j++];

        // Copiamos los elementos restantes de la mitad izquierda (si quedan)
        while (i <= mid) tmp[k++] = arr[i++];
        // Copiamos los elementos restantes de la mitad derecha (si quedan)
        while (j <= hi)  tmp[k++] = arr[j++];

        // Copiamos el resultado de vuelta al arreglo original
        System.arraycopy(tmp, 0, arr, lo, tmp.length);
    }

    // =========================================================================
    // GETTERS DE RESULTADO
    // =========================================================================

    /**
     * Retorna las aristas del MST. @return Lista de aristas del MST.
     */
    public LinkedList<Edge> getMSTEdges()  { return mstEdges; }

    /**
     * Retorna el peso total del MST. @return Peso en metros.
     */
    public int              getTotalWeight() { return totalWeight; }

    /**
     * Retorna el tiempo de ejecución en nanosegundos. @return Nanosegundos.
     */
    public long             getElapsedNano() { return elapsedNano; }

    /**
     * Retorna el tiempo de ejecución en milisegundos. @return Milisegundos.
     */
    public double           getElapsedMs()   { return elapsedNano / 1_000_000.0; }

    /**
     * Construye un grafo solo con las aristas del MST, útil para DFS posterior.
     *
     * @param V Número de vértices del grafo original.
     * @return Nuevo grafo con solo las aristas del MST.
     */
    public Graph buildMSTGraph(int V) {
        Graph mst = new Graph(V);
        for (Edge e : mstEdges)
            mst.addEdge(e.getU(), e.getV(), e.getWeight());
        return mst;
    }

    @Override
    public String toString() {
        return "Kruskal MST: totalWeight=" + totalWeight
             + "m, time=" + String.format("%.3f", getElapsedMs()) + "ms";
    }
}
