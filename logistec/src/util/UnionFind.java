package util;

/**
 * Estructura Union-Find (conjuntos disjuntos) con compresión de caminos y unión por rango.
 * Usada por el algoritmo de Kruskal para detectar ciclos al construir el MST.
 *
 * <p>Complejidad amortizada: O(α(n)) por operación, donde α es la
 * función inversa de Ackermann (prácticamente O(1) para cualquier n real).
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class UnionFind {

    // =========================================================================
    // CAMPOS
    // parent[i] = padre de i en el árbol del conjunto
    //   Si parent[i] == i, entonces i es la raíz (representante) de su conjunto.
    // rank[i] = cota superior de la altura del árbol enraizado en i.
    //   Se usa para decidir cuál árbol queda como raíz en una unión (por rango).
    // components = número de conjuntos distintos actualmente.
    // =========================================================================
    private final int[] parent;     // árbol de conjuntos
    private final int[] rank;       // cota de altura de cada árbol
    private       int   components; // contador de componentes conexos

    /**
     * Crea la estructura con {@code n} elementos, cada uno en su propio conjunto.
     *
     * @param n Número de elementos (indexados de 0 a n-1).
     */
    public UnionFind(int n) {
        parent     = new int[n];
        rank       = new int[n];
        components = n;

        // Inicialmente cada elemento es su propio representante
        for (int i = 0; i < n; i++) {
            parent[i] = i;  // cada nodo apunta a sí mismo (raíz propia)
            rank[i]   = 0;  // altura inicial = 0 (árbol de un solo nodo)
        }
    }

    /**
     * Encuentra el representante (raíz) del conjunto que contiene a {@code x}.
     * Aplica compresión de caminos: todos los nodos en el camino hacia la raíz
     * quedan apuntando directamente a ella, acelerando futuras operaciones.
     *
     * @param x Elemento a consultar.
     * @return Índice del representante del conjunto de x.
     */
    public int find(int x) {
        if (parent[x] != x) {
            // x no es raíz → recursión con compresión de caminos:
            // asignamos parent[x] directamente a la raíz del conjunto
            parent[x] = find(parent[x]);
        }
        return parent[x]; // retornamos la raíz
    }

    /**
     * Une los conjuntos que contienen a {@code x} y {@code y}.
     * Usa unión por rango: el árbol de menor rango se conecta bajo el de mayor rango,
     * manteniendo los árboles balanceados y evitando listas enlazadas degeneradas.
     *
     * @param x Primer elemento.
     * @param y Segundo elemento.
     * @return {@code true} si estaban en conjuntos distintos y se realizó la unión;
     *         {@code false} si ya pertenecían al mismo conjunto (no se hace nada).
     */
    public boolean union(int x, int y) {
        int rx = find(x); // representante de x
        int ry = find(y); // representante de y

        if (rx == ry) return false; // mismo conjunto → no hay nada que unir

        // Unión por rango: la raíz de mayor rango absorbe a la de menor
        if (rank[rx] < rank[ry]) {
            // Intercambiamos para que rx sea siempre el de mayor o igual rango
            int tmp = rx; rx = ry; ry = tmp;
        }
        parent[ry] = rx; // ry queda bajo rx

        if (rank[rx] == rank[ry]) {
            // Solo cuando los rangos son iguales, la altura del árbol resultante
            // aumenta en 1 (de lo contrario permanece igual)
            rank[rx]++;
        }

        components--; // fusionamos dos componentes → hay una menos
        return true;
    }

    /**
     * Indica si dos elementos pertenecen al mismo conjunto.
     *
     * @param x Primer elemento.
     * @param y Segundo elemento.
     * @return {@code true} si ambos comparten el mismo representante.
     */
    public boolean connected(int x, int y) {
        return find(x) == find(y);
    }

    /**
     * Retorna el número actual de conjuntos (componentes conexos) distintos.
     *
     * @return Número de componentes.
     */
    public int components() { return components; }
}
