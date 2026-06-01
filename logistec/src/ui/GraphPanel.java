package ui;

import algorithms.Prim;
import graph.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import util.LinkedList;

/**
 * City graph visualization panel.
 *
 * <p>Each truck route follows the actual streets (expanded via Floyd-Warshall).
 * Routes are drawn as quadratic Bezier curves with a slight bow per truck,
 * so overlapping segments remain distinguishable.
 *
 * @author LogísTEC Team
 * @version 1.0
 */
public class GraphPanel extends JPanel {

    private static final int VERTEX_RADIUS = 15;

    private static final Color BG_COLOR    = new Color(14, 18, 28);
    private static final Color EDGE_COLOR  = new Color(48, 58, 78);
    private static final Color MST_COLOR   = new Color(52, 211, 153);
    private static final Color DEPOT_COLOR = new Color(239, 68, 68);
    private static final Color DELIV_COLOR = new Color(96, 165, 250);
    private static final Color INTER_COLOR = new Color(90, 105, 125);

    // Truck route colors — vivid and distinct
    private static final Color[] TRUCK_COLORS = {
        new Color(251, 146,  60),   // orange
        new Color(167,  86, 250),   // violet
        new Color(34,  211, 238),   // cyan
        new Color(250, 204,  21),   // yellow
        new Color(244,  63,  94),   // rose
    };

    // Bezier bow amount per truck (pixels perpendicular to segment midpoint)
    private static final int[] BOW = { 22, -22, 0, 36, -36 };

    private Graph             graph;
    private LinkedList<Truck> trucks;
    private Prim              prim;
    private LinkedList<Integer> highlightedPath;
    private static final Color PATH_COLOR = new Color(250, 159, 74);
    private final int         PAD = 55;

    public GraphPanel() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(1000, 680));
    }

    public void setData(Graph graph, LinkedList<Truck> trucks, Prim prim) {
        setData(graph, trucks, prim, null);
    }

    public void setData(Graph graph, LinkedList<Truck> trucks, Prim prim,
                        LinkedList<Integer> highlightedPath) {
        this.graph         = graph;
        this.trucks        = trucks;
        this.prim          = prim;
        this.highlightedPath = highlightedPath;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (graph == null) {
            g0.setColor(new Color(90, 110, 135));
            g0.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g0.drawString("Cargue un archivo JSON y ejecute la planificacion.", 40, 50);
            return;
        }

        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        int V = graph.numVertices();
        int[] xs = new int[V], ys = new int[V];
        computeCoords(xs, ys);

        // 1. Base edges (gray)
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(EDGE_COLOR);
        for (int u = 0; u < V; u++)
            for (Graph.AdjEntry e : graph.adj(u))
                if (e.target > u) g.drawLine(xs[u], ys[u], xs[e.target], ys[e.target]);

        // 2. MST edges (green, thick, solid)
        if (prim != null) {
            g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(MST_COLOR);
            for (Edge e : prim.getMSTEdges())
                g.drawLine(xs[e.getU()], ys[e.getU()], xs[e.getV()], ys[e.getV()]);
        }

        // 3. Truck routes — curved, following real streets
        if (trucks != null) {
            int ci = 0;
            for (Truck t : trucks) {
                LinkedList<Integer> expanded = t.getExpandedRoute();
                if (!expanded.isEmpty()) {
                    drawTruckRoute(g, expanded, xs, ys,
                            TRUCK_COLORS[ci % TRUCK_COLORS.length],
                            BOW[ci % BOW.length], ci);
                }
                ci++;
            }
        }

        // 4. Highlighted shortest path (if present)
        if (highlightedPath != null && highlightedPath.size() > 1) {
            drawHighlightedPath(g, highlightedPath, xs, ys);
        }

        // 5. Vertices on top
        for (int i = 0; i < V; i++) {
            Vertex v = graph.getVertex(i);
            if (v != null) drawVertex(g, v, xs[i], ys[i]);
        }

        // 5. Stop labels (A→B→C order) per truck
        if (trucks != null) {
            int ci = 0;
            for (Truck t : trucks) {
                if (!t.getRoute().isEmpty())
                    drawStopNumbers(g, t.getRoute(), xs, ys,
                            TRUCK_COLORS[ci % TRUCK_COLORS.length]);
                ci++;
            }
        }

        // 6. Legend
        drawLegend(g);
    }

    // ── Truck route drawing ───────────────────────────────────────────────────

    /**
     * Draw a truck's expanded route as a series of quadratic Bezier curves.
     * Each segment gets a slight bow so multiple routes on the same street
     * are visually separated.
     */
    private void drawTruckRoute(Graphics2D g, LinkedList<Integer> expanded,
                                int[] xs, int[] ys, Color c, int bow, int idx) {
        int[] rv = toArray(expanded);
        if (rv.length < 2) return;

        // Dashed stroke per truck (different dash phase)
        float[] dash = { 12f, 7f };
        g.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, dash, idx * 9.0f));
        g.setColor(c);

        for (int i = 0; i < rv.length - 1; i++) {
            int x1 = xs[rv[i]], y1 = ys[rv[i]];
            int x2 = xs[rv[i+1]], y2 = ys[rv[i+1]];

            // Midpoint + perpendicular offset = Bezier control point
            double mx = (x1 + x2) / 2.0;
            double my = (y1 + y2) / 2.0;
            double dx = x2 - x1, dy = y2 - y1;
            double len = Math.sqrt(dx*dx + dy*dy);
            if (len < 1) continue;
            double cpx = mx + (-dy / len) * bow;
            double cpy = my + ( dx / len) * bow;

            QuadCurve2D curve = new QuadCurve2D.Double(x1, y1, cpx, cpy, x2, y2);
            g.draw(curve);

            // Arrowhead at 60% of the curve
            drawArrowOnCurve(g, x1, y1, cpx, cpy, x2, y2, c, 0.6);
        }
    }

    /** Draw a filled arrowhead at parameter t along a quadratic Bezier. */
    private void drawArrowOnCurve(Graphics2D g, double x1, double y1,
                                  double cpx, double cpy,
                                  double x2, double y2,
                                  Color c, double t) {
        // Point on curve at t
        double px = (1-t)*(1-t)*x1 + 2*(1-t)*t*cpx + t*t*x2;
        double py = (1-t)*(1-t)*y1 + 2*(1-t)*t*cpy + t*t*y2;
        // Tangent direction
        double tx = 2*(1-t)*(cpx-x1) + 2*t*(x2-cpx);
        double ty = 2*(1-t)*(cpy-y1) + 2*t*(y2-cpy);
        double angle = Math.atan2(ty, tx);

        int ar = 10;
        int[] apx = { (int)px,
            (int)(px - ar*Math.cos(angle-0.42)),
            (int)(px - ar*Math.cos(angle+0.42)) };
        int[] apy = { (int)py,
            (int)(py - ar*Math.sin(angle-0.42)),
            (int)(py - ar*Math.sin(angle+0.42)) };

        g.setStroke(new BasicStroke(1f));
        g.setColor(c);
        g.fillPolygon(apx, apy, 3);
    }

    // ── Stop order numbers ────────────────────────────────────────────────────

    /**
     * Draw a small numbered badge (1, 2, 3…) at each stop in the route,
     * slightly offset so multiple trucks on the same node don't overlap.
     */
    private void drawStopNumbers(Graphics2D g, LinkedList<Integer> route,
                                 int[] xs, int[] ys, Color c) {
        int[] rv = toArray(route);
        // Skip first and last (both are depot)
        for (int i = 1; i < rv.length - 1; i++) {
            int vx = xs[rv[i]] + 18;
            int vy = ys[rv[i]] - 14;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 200));
            g.fillRoundRect(vx - 2, vy - 12, 18, 16, 6, 6);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.drawString(String.valueOf(i), vx + 2, vy);
        }
    }

    // ── Vertex drawing ────────────────────────────────────────────────────────

    private void drawVertex(Graphics2D g, Vertex v, int x, int y) {
        Color fill = v.isDepot() ? DEPOT_COLOR : v.isDelivery() ? DELIV_COLOR : INTER_COLOR;
        int r = VERTEX_RADIUS;
        if (v.isDepot() || v.isDelivery()) {
            g.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 35));
            g.fillOval(x-r-8, y-r-8, (r+8)*2, (r+8)*2);
        }
        // Shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x-r+2, y-r+2, r*2, r*2);
        // Fill
        g.setColor(fill);
        g.fillOval(x-r, y-r, r*2, r*2);
        // Border
        g.setStroke(new BasicStroke(1.6f));
        g.setColor(fill.brighter().brighter());
        g.drawOval(x-r, y-r, r*2, r*2);
        // Label
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        String lbl = v.getId();
        g.drawString(lbl, x - fm.stringWidth(lbl)/2, y + fm.getAscent()/2 - 1);
    }

    // ── Legend ────────────────────────────────────────────────────────────────

    private void drawLegend(Graphics2D g) {
        int lx = 14, itemH = 20;
        int truckCount = 0;
        if (trucks != null) for (Truck t : trucks) if (!t.getExpandedRoute().isEmpty()) truckCount++;
        int totalH = (4 + truckCount) * itemH + 30;
        int ly = getHeight() - totalH - 14;

        g.setColor(new Color(10, 14, 24, 215));
        g.fillRoundRect(lx-10, ly-22, 172, totalH+14, 12, 12);
        g.setColor(new Color(50, 70, 100, 140));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(lx-10, ly-22, 172, totalH+14, 12, 12);

        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(210, 230, 255));
        g.drawString("Leyenda", lx, ly-5);
        ly += 10;

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        legendDot(g,  lx, ly, DEPOT_COLOR,  "Deposito");     ly += itemH;
        legendDot(g,  lx, ly, DELIV_COLOR,  "Entrega");      ly += itemH;
        legendDot(g,  lx, ly, INTER_COLOR,  "Interseccion"); ly += itemH;
        legendLine(g, lx, ly, MST_COLOR,    "MST");          ly += itemH;
        if (trucks != null) {
            int ci = 0;
            for (Truck t : trucks) {
                if (!t.getExpandedRoute().isEmpty()) {
                    legendLine(g, lx, ly, TRUCK_COLORS[ci % TRUCK_COLORS.length],
                            "Ruta " + t.getId());
                    ly += itemH;
                }
                ci++;
            }
        }
    }

    private void legendDot(Graphics2D g, int x, int y, Color c, String label) {
        g.setColor(c);
        g.fillOval(x, y-8, 13, 13);
        g.setColor(new Color(200, 220, 245));
        g.drawString(label, x+20, y+3);
    }

    private void legendLine(Graphics2D g, int x, int y, Color c, String label) {
        g.setColor(c);
        g.setStroke(new BasicStroke(2.8f));
        g.drawLine(x, y, x+15, y);
        g.setColor(new Color(200, 220, 245));
        g.drawString(label, x+20, y+4);
    }

    private void drawHighlightedPath(Graphics2D g, LinkedList<Integer> path,
                                     int[] xs, int[] ys) {
        int[] rv = toArray(path);
        if (rv.length < 2) return;

        g.setStroke(new BasicStroke(4.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(PATH_COLOR);
        for (int i = 0; i < rv.length - 1; i++) {
            g.drawLine(xs[rv[i]], ys[rv[i]], xs[rv[i+1]], ys[rv[i+1]]);
        }

        for (int v : rv) {
            int x = xs[v], y = ys[v];
            g.setColor(new Color(PATH_COLOR.getRed(), PATH_COLOR.getGreen(), PATH_COLOR.getBlue(), 90));
            g.fillOval(x - 16, y - 16, 32, 32);
            g.setColor(PATH_COLOR);
            g.fillOval(x - 8, y - 8, 16, 16);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void computeCoords(int[] xs, int[] ys) {
        int W = getWidth() - 2*PAD, H = getHeight() - 2*PAD;
        int V = graph.numVertices();
        int minX=Integer.MAX_VALUE, maxX=Integer.MIN_VALUE;
        int minY=Integer.MAX_VALUE, maxY=Integer.MIN_VALUE;
        for (int i = 0; i < V; i++) {
            Vertex v = graph.getVertex(i);
            if (v == null) continue;
            minX=Math.min(minX,v.getX()); maxX=Math.max(maxX,v.getX());
            minY=Math.min(minY,v.getY()); maxY=Math.max(maxY,v.getY());
        }
        int rx=Math.max(1,maxX-minX), ry=Math.max(1,maxY-minY);
        for (int i = 0; i < V; i++) {
            Vertex v = graph.getVertex(i);
            if (v==null){xs[i]=PAD;ys[i]=PAD;continue;}
            xs[i] = PAD + (int)((double)(v.getX()-minX)/rx*W);
            ys[i] = PAD + (int)((double)(v.getY()-minY)/ry*H);
        }
    }

    private int[] toArray(LinkedList<Integer> list) {
        int[] arr = new int[list.size()]; int i=0;
        for (int v : list) arr[i++]=v;
        return arr;
    }
}
