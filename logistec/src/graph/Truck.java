package graph;

import algorithms.Parcel;
import util.LinkedList;

/**
 * Represents a delivery truck in the LogísTEC fleet.
 *
 * <p>Stores both the stop-level route (waypoints) and the fully expanded
 * route that follows actual streets via Floyd-Warshall paths.
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class Truck {

    private final String id;
    private final int    capacity;
    private int          currentLoad;

    private final LinkedList<Parcel>  packages         = new LinkedList<>();
    private       LinkedList<Integer> route            = new LinkedList<>(); // stop-level
    private       LinkedList<Integer> expandedRoute    = new LinkedList<>(); // full street path
    private       int                 routeDistanceNN  = 0;
    private       int                 routeDistanceMST = 0;

    public Truck(String id, int capacity) {
        this.id       = id;
        this.capacity = capacity;
    }

    // ── Accessors ──────────────────────────────────────────────────────────

    public String  getId()           { return id; }
    public int     getCapacity()     { return capacity; }
    public int     getCurrentLoad()  { return currentLoad; }
    public int     getFreeCapacity() { return capacity - currentLoad; }

    public LinkedList<Parcel>  getPackages()      { return packages; }
    public LinkedList<Integer> getRoute()          { return route; }
    public LinkedList<Integer> getExpandedRoute()  { return expandedRoute; }

    public int  getRouteDistanceNN()               { return routeDistanceNN; }
    public int  getRouteDistanceMST()              { return routeDistanceMST; }
    public void setRouteDistanceNN(int d)          { routeDistanceNN  = d; }
    public void setRouteDistanceMST(int d)         { routeDistanceMST = d; }
    public void setRoute(LinkedList<Integer> r)    { route = r; }
    public void setExpandedRoute(LinkedList<Integer> r) { expandedRoute = r; }

    // ── Operations ─────────────────────────────────────────────────────────

    public boolean addPackage(Parcel p) {
        if (currentLoad + p.getWeight() > capacity) return false;
        packages.addLast(p);
        currentLoad += p.getWeight();
        p.assign(id);
        return true;
    }

    public boolean canFit(Parcel p) {
        return currentLoad + p.getWeight() <= capacity;
    }

    public double occupancyPercent() {
        return 100.0 * currentLoad / capacity;
    }

    @Override
    public String toString() {
        return "Truck{id='" + id + "', capacity=" + capacity + "kg, load=" + currentLoad + "kg}";
    }
}
