package util;


public class UnionFind {

    private final int[] parent;
    private final int[] rank;
    private int components;

    /**
     * Create a Union-Find with n elements (0..n-1).
     */
    public UnionFind(int n) {
        parent = new int[n];
        rank   = new int[n];
        components = n;
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    /**
     * Find the representative of the set containing x.
     * Path compression applied. O(α(n)).
     */
    public int find(int x) {
        if (parent[x] != x)
            parent[x] = find(parent[x]); // path compression
        return parent[x];
    }

    /**
     * Unite the sets containing x and y.
     * Returns true if they were in different sets (i.e., a merge happened).
     * Union by rank. O(α(n)).
     */
    public boolean union(int x, int y) {
        int rx = find(x);
        int ry = find(y);
        if (rx == ry) return false; // already same set
        if (rank[rx] < rank[ry]) { int t = rx; rx = ry; ry = t; }
        parent[ry] = rx;
        if (rank[rx] == rank[ry]) rank[rx]++;
        components--;
        return true;
    }

    /**
     * True if x and y are in the same connected component.
     */
    public boolean connected(int x, int y) {
        return find(x) == find(y);
    }

    /**
     * Number of distinct components.
     */
    public int components() { return components; }
}
