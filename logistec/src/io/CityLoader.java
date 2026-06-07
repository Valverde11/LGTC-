package io;

import algorithms.Parcel;
import graph.*;
import io.JsonParser.JsonArray;
import io.JsonParser.JsonObject;
import java.io.IOException;
import util.LinkedList;

/**
 * Loads a LogísTEC configuration JSON file and builds the domain objects.
 *
 * <p>Expected JSON schema:
 * <pre>
 * {
 *   "ciudad": {
 *     "vertices": [{"id":"A","tipo":"DEPOT","x":100,"y":100}, ...],
 *     "aristas":  [{"u":"A","v":"B","distancia":320}, ...]
 *   },
 *   "paquetes": [{"id":"P01","destino":"G","peso":5,"prioridad":1}, ...],
 *   "camiones": [{"id":"C01","capacidad":50}, ...]
 * }
 * </pre>
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class CityLoader {

    private Graph               graph;
    private LinkedList<Parcel> packages;
    private LinkedList<Truck>   trucks;

    /**
     * Load and parse the JSON configuration file at {@code filePath}.
     *
     * @param filePath path to the JSON file
     * @throws IOException if the file cannot be read
     */
    public void load(String filePath) throws IOException {
        String json = JsonParser.readFile(filePath);
        JsonParser parser = new JsonParser(json);
        JsonObject root = parser.parseObject();

        // ── Parse city ────────────────────────────────────────────────────
        JsonObject city = root.getObject("ciudad");
        JsonArray  vArr = city.getArray("vertices");
        JsonArray  eArr = city.getArray("aristas");

        int V = vArr.size();
        graph = new Graph(V);

        // Index mapping: vertex id → int index
        String[] idToIdx = new String[V]; // idToIdx[i] = id at index i
        for (int i = 0; i < V; i++) {
            JsonObject vo = vArr.getObject(i);
            String id   = vo.getString("id");
            String tipo = vo.getString("tipo");
            int    x    = vo.getInt("x", 0);
            int    y    = vo.getInt("y", 0);

            Vertex.Type type = switch (tipo.toUpperCase()) {
                case "DEPOT"        -> Vertex.Type.DEPOT;
                case "DELIVERY"     -> Vertex.Type.DELIVERY;
                default             -> Vertex.Type.INTERSECTION;
            };

            Vertex v = new Vertex(id, type, x, y);
            graph.addVertex(i, v);
            idToIdx[i] = id;
        }

        // ── Add edges ─────────────────────────────────────────────────────
        for (int i = 0; i < eArr.size(); i++) {
            JsonObject eo = eArr.getObject(i);
            String uId = eo.getString("u");
            String vId = eo.getString("v");
            int    dist = eo.getInt("distancia");

            int u = graph.indexOf(uId);
            int v = graph.indexOf(vId);
            if (u == -1) throw new RuntimeException("Unknown vertex: " + uId);
            if (v == -1) throw new RuntimeException("Unknown vertex: " + vId);
            graph.addEdge(u, v, dist);
        }

        // ── Parse packages ────────────────────────────────────────────────
        packages = new LinkedList<>();
        JsonArray pArr = root.getArray("paquetes");
        for (int i = 0; i < pArr.size(); i++) {
            JsonObject po   = pArr.getObject(i);
            String  pid     = po.getString("id");
            String  dest    = po.getString("destino");
            int     peso    = po.getInt("peso");
            int     prior   = po.getInt("prioridad");

            Parcel pkg = new Parcel(pid, dest, peso, prior);
            int destIdx = graph.indexOf(dest);
            if (destIdx == -1) throw new RuntimeException("Unknown destination vertex: " + dest);
            pkg.setDestinationIndex(destIdx);
            packages.addLast(pkg);
        }

        // ── Parse trucks ──────────────────────────────────────────────────
        trucks = new LinkedList<>();
        JsonArray tArr = root.getArray("camiones");
        for (int i = 0; i < tArr.size(); i++) {
            JsonObject to = tArr.getObject(i);
            String tid    = to.getString("id");
            int    cap    = to.getInt("capacidad");
            trucks.addLast(new Truck(tid, cap));
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────

    public Graph               getGraph()    { return graph; }
    public LinkedList<Parcel> getParcels() { return packages; }
    public LinkedList<Truck>   getTrucks()   { return trucks; }
}
