package planner;

import algorithms.*;
import graph.*;
import util.LinkedList;

/**
 * LogísTEC Planner — orchestrates package assignment and route planning.
 *
 * <h2>Parcel assignment (Best-Fit heuristic)</h2>
 * <ol>
 *   <li>Sort packages by priority ASC, then weight DESC.</li>
 *   <li>For each package, assign to the truck with maximum free capacity that
 *       can still accommodate it.</li>
 *   <li>If no truck can fit it, mark the package as rejected.</li>
 * </ol>
 *
 * <h2>Route planning</h2>
 * <p>Two heuristics are applied per truck and the better result is selected:
 * <ul>
 *   <li><b>Nearest Neighbor (NN)</b> – greedy, O(n²).</li>
 *   <li><b>MST-based</b> – build MST on stop sub-graph, DFS pre-order, O(n² + n log n).</li>
 * </ul>
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class Planner {

    private final Graph         graph;
    private final FloydWarshall fw;
    private final Warshall      warshall;
    private final int           depotIdx;

    /**
     * @param graph    the city graph
     * @param fw       pre-computed Floyd-Warshall distance matrix
     * @param warshall pre-computed transitive closure
     */
    public Planner(Graph graph, FloydWarshall fw, Warshall warshall) {
        this.graph    = graph;
        this.fw       = fw;
        this.warshall = warshall;
        this.depotIdx = graph.getDepotIndex();
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 1. VALIDATE PACKAGES
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Mark packages whose destination is unreachable from the depot.
     * Returns a list of rejected (unreachable) packages.
     */
    public LinkedList<Parcel> validateReachability(LinkedList<Parcel> packages) {
        LinkedList<Parcel> unreachable = new LinkedList<>();
        for (Parcel p : packages) {
            if (!warshall.canReach(depotIdx, p.getDestinationIndex())) {
                p.reject();
                unreachable.addLast(p);
            }
        }
        return unreachable;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 2. ASSIGN PACKAGES (Best-Fit)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Assign pending packages to trucks using Best-Fit heuristic.
     * Only considers packages in PENDING status (unreachable ones are already REJECTED).
     *
     * @param packages all packages
     * @param trucks   all trucks
     * @return list of packages that could not be assigned (capacity exceeded)
     */
    public LinkedList<Parcel> assignPackages(LinkedList<Parcel> packages, LinkedList<Truck> trucks) {
        // Step 1: sort packages — priority ASC, weight DESC
        Parcel[] arr = toSortedArray(packages);

        LinkedList<Parcel> rejected = new LinkedList<>();

        // Step 2: best-fit for each package
        for (Parcel p : arr) {
            if (p.getStatus() != Parcel.Status.PENDING) continue;

            Truck best = null;
            int   bestFree = -1;

            for (Truck t : trucks) {
                if (t.canFit(p) && t.getFreeCapacity() > bestFree) {
                    bestFree = t.getFreeCapacity();
                    best     = t;
                }
            }

            if (best != null) {
                best.addPackage(p);
            } else {
                p.reject();
                rejected.addLast(p);
            }
        }
        return rejected;
    }

    // Sort: priority ASC first, weight DESC second — simple insertion sort (small n)
    private Parcel[] toSortedArray(LinkedList<Parcel> packages) {
        int n = packages.size();
        Parcel[] arr = new Parcel[n];
        int i = 0;
        for (Parcel p : packages) arr[i++] = p;

        // Insertion sort
        for (int j = 1; j < n; j++) {
            Parcel key = arr[j];
            int k = j - 1;
            while (k >= 0 && comparePackages(arr[k], key) > 0) {
                arr[k + 1] = arr[k];
                k--;
            }
            arr[k + 1] = key;
        }
        return arr;
    }

    /** Compare by priority ASC, then weight DESC. */
    private int comparePackages(Parcel a, Parcel b) {
        if (a.getPriority() != b.getPriority()) return a.getPriority() - b.getPriority();
        return b.getWeight() - a.getWeight(); // DESC weight
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3. PLAN ROUTES
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Compute routes for all trucks using both heuristics. The truck's route
     * is set to the one with smaller total distance.
     */
    public void planRoutes(LinkedList<Truck> trucks) {
        for (Truck t : trucks) {
            if (t.getPackages().isEmpty()) continue;
            planRoutesForTruck(t);
        }
    }

    private void planRoutesForTruck(Truck truck) {
        // Collect unique stop indices
        LinkedList<Integer> stopsRaw = new LinkedList<>();
        boolean[] seen = new boolean[graph.numVertices()];
        for (Parcel p : truck.getPackages()) {
            int idx = p.getDestinationIndex();
            if (!seen[idx]) { seen[idx] = true; stopsRaw.addLast(idx); }
        }

        int[] stops = new int[stopsRaw.size()];
        int i = 0;
        for (int s : stopsRaw) stops[i++] = s;

        // NN heuristic
        int[] routeNN = nearestNeighbor(stops);
        int   distNN  = routeDistance(routeNN);

        // MST-based heuristic
        int[] routeMST = mstBased(stops);
        int   distMST  = routeDistance(routeMST);

        truck.setRouteDistanceNN(distNN);
        truck.setRouteDistanceMST(distMST);

        // Use the shorter route
        int[] best = distNN <= distMST ? routeNN : routeMST;
        LinkedList<Integer> bestList = new LinkedList<>();
        for (int v : best) bestList.addLast(v);
        truck.setRoute(bestList);

        // Build expanded route: replace each hop with the actual street path
        LinkedList<Integer> expanded = new LinkedList<>();
        for (int s = 0; s < best.length - 1; s++) {
            int[] path = fw.reconstructPath(best[s], best[s + 1]);
            if (path == null) continue;
            for (int pi = 0; pi < path.length - 1; pi++) // skip last to avoid duplicates
                expanded.addLast(path[pi]);
        }
        expanded.addLast(best[best.length - 1]); // add final depot
        truck.setExpandedRoute(expanded);
    }

    // ── Nearest Neighbor ─────────────────────────────────────────────────────

    /**
     * Nearest Neighbor heuristic.
     * Returns route as array: [depot, stop1, stop2, ..., stopN, depot].
     * Complexity: O(n²) where n = number of stops.
     */
    public int[] nearestNeighbor(int[] stops) {
        int n = stops.length;
        boolean[] visited = new boolean[n];
        int[] route = new int[n + 2]; // depot + stops + depot
        route[0] = depotIdx;
        int cur = depotIdx;

        for (int step = 0; step < n; step++) {
            int best = -1;
            int bestDist = Integer.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (!visited[j]) {
                    int d = fw.dist(cur, stops[j]);
                    if (d < bestDist) { bestDist = d; best = j; }
                }
            }
            visited[best] = true;
            cur = stops[best];
            route[step + 1] = cur;
        }
        route[n + 1] = depotIdx;
        return route;
    }

    // ── MST-based heuristic ──────────────────────────────────────────────────

    /**
     * MST-based 2-approximation heuristic.
     * <ol>
     *   <li>Build a complete sub-graph on {depot} ∪ stops with FW distances.</li>
     *   <li>Compute MST with Kruskal (on the sub-graph).</li>
     *   <li>DFS pre-order from depot on MST gives the visit order.</li>
     * </ol>
     * Returns route as array: [depot, stop1, ..., stopN, depot].
     */
    public int[] mstBased(int[] stops) {
        // Build sub-graph: vertices = {depot} ∪ stops, indexed 0..m
        int m = stops.length + 1;
        int[] subV = new int[m]; // subV[i] = actual vertex index in main graph
        subV[0] = depotIdx;
        for (int i = 0; i < stops.length; i++) subV[i + 1] = stops[i];

        Graph sub = new Graph(m);
        // Add vertices (lightweight, no metadata needed for MST)
        for (int i = 0; i < m; i++) sub.addVertex(i, graph.getVertex(subV[i]));

        // Add all pairs as edges (complete graph with FW distances)
        for (int i = 0; i < m; i++) {
            for (int j = i + 1; j < m; j++) {
                int d = fw.dist(subV[i], subV[j]);
                if (d < Integer.MAX_VALUE / 2)
                    sub.addEdge(i, j, d);
            }
        }

        // MST of sub-graph
        Kruskal kruskal = new Kruskal(sub);
        Graph mstGraph  = kruskal.buildMSTGraph(m);

        // DFS pre-order on MST from vertex 0 (depot in sub-graph)
        boolean[] vis = new boolean[m];
        LinkedList<Integer> preorder = new LinkedList<>();
        GraphTraversal.dfsPreorder(mstGraph, 0, vis, preorder);

        // Build route: map sub-indices back to main graph indices
        int[] route = new int[m + 1]; // depot + all stops + return to depot
        int ri = 0;
        for (int subIdx : preorder) {
            route[ri++] = subV[subIdx];
        }
        route[ri] = depotIdx; // return
        return route;
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    /**
     * Total route distance using the FW matrix.
     * Route array: [v0, v1, v2, ..., vn] — sum of consecutive pairs.
     */
    public int routeDistance(int[] route) {
        int total = 0;
        for (int i = 0; i < route.length - 1; i++) {
            int d = fw.dist(route[i], route[i + 1]);
            if (d >= Integer.MAX_VALUE / 2) return Integer.MAX_VALUE / 2; // unreachable
            total += d;
        }
        return total;
    }
}
