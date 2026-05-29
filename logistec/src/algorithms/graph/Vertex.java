package graph;
public class Vertex {

    /** Possible types of vertex in the city. */
    public enum Type { DEPOT, INTERSECTION, DELIVERY }

    // ── Fields ───────────────────────────────────────────────────────────────

    private final String id;
    private final Type   type;
    private final int    x;   // screen coordinate (for visualization)
    private final int    y;

    // ── Constructor ──────────────────────────────────────────────────────────

    public Vertex(String id, Type type, int x, int y) {
        this.id   = id;
        this.type = type;
        this.x    = x;
        this.y    = y;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String getId()   { return id; }
    public Type   getType() { return type; }
    public int    getX()    { return x; }
    public int    getY()    { return y; }

    public boolean isDepot()    { return type == Type.DEPOT; }
    public boolean isDelivery() { return type == Type.DELIVERY; }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vertex v)) return false;
        return id.equals(v.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return "Vertex{id='" + id + "', type=" + type + ", (" + x + "," + y + ")}";
    }
}
