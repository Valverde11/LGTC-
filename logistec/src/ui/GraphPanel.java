package ui;

import algorithms.Prim;
import graph.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import util.LinkedList;

/**
 * Panel de visualización del grafo de la ciudad para el sistema LogísTEC.
 *
 * <p>Dibuja el grafo completo, el árbol de expansión mínima (MST) y las rutas
 * de cada camión siguiendo las calles reales del grafo (ruta expandida calculada
 * con Floyd-Warshall). Las rutas se dibujan como curvas cuadráticas de Bézier
 * con un arco distinto por camión para que no se solapen visualmente.
 *
 * <p>Capas de dibujo (de inferior a superior):
 * <ol>
 *   <li>Aristas base del grafo (gris, delgadas)</li>
 *   <li>Aristas del MST (verde, gruesas)</li>
 *   <li>Rutas de camiones (colores distintos, curvas punteadas con flechas)</li>
 *   <li>Camino mínimo resaltado (naranja, si se calculó con Dijkstra)</li>
 *   <li>Vértices (encima de todo para que siempre sean visibles)</li>
 *   <li>Números de parada por camión</li>
 *   <li>Leyenda</li>
 * </ol>
 *
 * @author Andres Aguilar
 * @version 1.0
 */
public class GraphPanel extends JPanel {

    // ── Constantes de diseño ─────────────────────────────────────────────────

    /** Radio en píxeles de cada círculo que representa un vértice. */
    private static final int VERTEX_RADIUS = 15;

    /** Color de fondo del panel (azul muy oscuro). */
    private static final Color BG_COLOR    = new Color(14, 18, 28);

    /** Color de las aristas normales del grafo (gris azulado). */
    private static final Color EDGE_COLOR  = new Color(48, 58, 78);

    /** Color de las aristas del MST (verde esmeralda). */
    private static final Color MST_COLOR   = new Color(52, 211, 153);

    /** Color del vértice depósito (rojo). */
    private static final Color DEPOT_COLOR = new Color(239, 68, 68);

    /** Color de los vértices de entrega (azul claro). */
    private static final Color DELIV_COLOR = new Color(96, 165, 250);

    /** Color de las intersecciones normales (gris pizarra). */
    private static final Color INTER_COLOR = new Color(90, 105, 125);

    /**
     * Paleta de colores para las rutas de cada camión.
     * El índice del camión en la lista determina qué color se usa (módulo 5).
     */
    private static final Color[] TRUCK_COLORS = {
        new Color(251, 146,  60),   // naranja  — C01
        new Color(167,  86, 250),   // violeta  — C02
        new Color(34,  211, 238),   // cyan     — C03
        new Color(250, 204,  21),   // amarillo — C04
        new Color(244,  63,  94),   // rosa     — C05
    };

    /**
     * Arco en píxeles que se aplica perpendicularmente al segmento para separar
     * visualmente las rutas de distintos camiones en el mismo tramo de calle.
     * Valores positivos = arco hacia un lado, negativos = hacia el otro.
     */
    private static final int[] BOW = { 22, -22, 0, 36, -36 };

    /** Color del camino mínimo resaltado con Dijkstra (naranja brillante). */
    private static final Color PATH_COLOR = new Color(250, 159, 74);

    /** Margen en píxeles entre los bordes del panel y el área de dibujo del grafo. */
    private final int PAD = 55;

    // ── Estado del panel ─────────────────────────────────────────────────────

    /** Grafo de la ciudad a visualizar. Null si no se ha cargado ningún caso. */
    private Graph graph;

    /** Lista de camiones con sus rutas. Null antes de ejecutar la planificación. */
    private LinkedList<Truck> trucks;

    /** Resultado de Prim con las aristas del MST. Null antes de la planificación. */
    private Prim prim;

    /**
     * Ruta resaltada para mostrar el camino mínimo calculado con Dijkstra.
     * Null si no se ha calculado ningún camino mínimo o fue limpiado.
     */
    private LinkedList<Integer> highlightedPath;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Crea el panel de visualización con fondo oscuro y tamaño preferido.
     */
    public GraphPanel() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(1000, 680));
    }

    // ── Setters de datos ─────────────────────────────────────────────────────

    /**
     * Actualiza los datos del panel sin ruta resaltada y repinta.
     *
     * @param graph  Grafo de la ciudad a visualizar.
     * @param trucks Lista de camiones con rutas planificadas (puede ser null).
     * @param prim   Resultado del algoritmo de Prim con el MST (puede ser null).
     */
    public void setData(Graph graph, LinkedList<Truck> trucks, Prim prim) {
        setData(graph, trucks, prim, null);
    }

    /**
     * Actualiza todos los datos del panel y repinta, incluyendo una ruta resaltada.
     *
     * @param graph          Grafo de la ciudad a visualizar.
     * @param trucks         Lista de camiones con rutas planificadas (puede ser null).
     * @param prim           Resultado del algoritmo de Prim con el MST (puede ser null).
     * @param highlightedPath Lista de índices de vértices del camino a resaltar,
     *                       o null para no resaltar ninguno.
     */
    public void setData(Graph graph, LinkedList<Truck> trucks, Prim prim,
                        LinkedList<Integer> highlightedPath) {
        this.graph           = graph;
        this.trucks          = trucks;
        this.prim            = prim;
        this.highlightedPath = highlightedPath;
        repaint(); // solicitar redibujo en el hilo de eventos de Swing
    }

    // ── Método principal de pintura ──────────────────────────────────────────

    /**
     * Método de pintura principal de Swing, llamado automáticamente al redibujar.
     * Delega en sub-métodos especializados para cada capa visual.
     *
     * @param g0 Contexto gráfico original proporcionado por Swing.
     */
    @Override
    protected void paintComponent(Graphics g0) {
        // Limpia el panel con el color de fondo
        super.paintComponent(g0);

        // Si no hay grafo cargado, mostrar mensaje orientativo y terminar
        if (graph == null) {
            g0.setColor(new Color(90, 110, 135));
            g0.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g0.drawString("Cargue un archivo JSON y ejecute la planificacion.", 40, 50);
            return;
        }

        // Convertir a Graphics2D para acceder a funciones avanzadas (Bézier, antialias)
        Graphics2D g = (Graphics2D) g0;

        // Activar antialiasing para bordes suavizados en curvas y texto
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        // Calcular coordenadas en pantalla para cada vértice
        int V = graph.numVertices();
        int[] xs = new int[V], ys = new int[V];
        computeCoords(xs, ys);

        // ── Capa 1: aristas base ──────────────────────────────────────────────
        // Solo dibujamos u→v cuando target > u para evitar dibujar cada arista dos veces
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(EDGE_COLOR);
        for (int u = 0; u < V; u++)
            for (Graph.AdjEntry e : graph.adj(u))
                if (e.target > u) // evitar duplicados (grafo no dirigido)
                    g.drawLine(xs[u], ys[u], xs[e.target], ys[e.target]);

        // ── Capa 2: aristas del MST ───────────────────────────────────────────
        // Se dibujan más gruesas y en verde para destacarse sobre las aristas base
        if (prim != null) {
            g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(MST_COLOR);
            for (Edge e : prim.getMSTEdges())
                g.drawLine(xs[e.getU()], ys[e.getU()], xs[e.getV()], ys[e.getV()]);
        }

        // ── Capa 3: rutas de camiones ─────────────────────────────────────────
        // Cada camión usa un color y un arco (bow) distinto para separar rutas
        if (trucks != null) {
            int ci = 0; // índice de color/arco del camión actual
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

        // ── Capa 4: camino mínimo resaltado (Dijkstra) ────────────────────────
        if (highlightedPath != null && highlightedPath.size() > 1)
            drawHighlightedPath(g, highlightedPath, xs, ys);

        // ── Capa 5: vértices ─────────────────────────────────────────────────
        // Se dibujan al final para aparecer encima de todas las aristas y rutas
        for (int i = 0; i < V; i++) {
            Vertex v = graph.getVertex(i);
            if (v != null) drawVertex(g, v, xs[i], ys[i]);
        }

        // ── Capa 6: números de parada por camión ─────────────────────────────
        if (trucks != null) {
            int ci = 0;
            for (Truck t : trucks) {
                if (!t.getRoute().isEmpty())
                    drawStopNumbers(g, t.getRoute(), xs, ys,
                            TRUCK_COLORS[ci % TRUCK_COLORS.length]);
                ci++;
            }
        }

        // ── Capa 7: leyenda ───────────────────────────────────────────────────
        drawLegend(g);
    }

    // ── Métodos de dibujo de rutas ───────────────────────────────────────────

    /**
     * Dibuja la ruta expandida de un camión como una serie de curvas cuadráticas
     * de Bézier punteadas. El punto de control de cada curva se desplaza
     * perpendicularmente al segmento según {@code bow}, logrando un arco
     * que visualmente separa rutas paralelas.
     *
     * @param g        Contexto gráfico 2D.
     * @param expanded Lista de índices de vértices de la ruta expandida.
     * @param xs       Arreglo de coordenadas X de los vértices en pantalla.
     * @param ys       Arreglo de coordenadas Y de los vértices en pantalla.
     * @param c        Color de la ruta del camión.
     * @param bow      Desplazamiento perpendicular del punto de control en píxeles.
     * @param idx      Índice del camión (0-based), usado para el desfase del dash.
     */
    private void drawTruckRoute(Graphics2D g, LinkedList<Integer> expanded,
                                int[] xs, int[] ys, Color c, int bow, int idx) {
        int[] rv = toArray(expanded);
        if (rv.length < 2) return; // nada que dibujar con menos de 2 puntos

        // Trazo punteado con fase distinta por camión para distinguir superposiciones
        float[] dash = { 12f, 7f };
        g.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, dash, idx * 9.0f)); // el offset del patrón varía por camión
        g.setColor(c);

        for (int i = 0; i < rv.length - 1; i++) {
            int x1 = xs[rv[i]],   y1 = ys[rv[i]];
            int x2 = xs[rv[i+1]], y2 = ys[rv[i+1]];

            // Punto medio del segmento
            double mx = (x1 + x2) / 2.0;
            double my = (y1 + y2) / 2.0;

            // Vector dirección del segmento y su longitud
            double dx = x2 - x1, dy = y2 - y1;
            double len = Math.sqrt(dx*dx + dy*dy);
            if (len < 1) continue; // segmento degenerado, saltar

            // Desplazar el punto de control perpendicularmente al segmento
            // El perpendicular de (dx,dy) es (-dy, dx) normalizado
            double cpx = mx + (-dy / len) * bow;
            double cpy = my + ( dx / len) * bow;

            // Dibujar la curva cuadrática: inicio → control → fin
            QuadCurve2D curve = new QuadCurve2D.Double(x1, y1, cpx, cpy, x2, y2);
            g.draw(curve);

            // Flecha en el 60% de la curva para indicar dirección de la ruta
            drawArrowOnCurve(g, x1, y1, cpx, cpy, x2, y2, c, 0.6);
        }
    }

    /**
     * Dibuja una punta de flecha rellena en el punto parametrizado {@code t}
     * de una curva cuadrática de Bézier, orientada según la tangente en ese punto.
     *
     * <p>Fórmulas usadas:
     * <ul>
     *   <li>Punto en t: P(t) = (1-t)²·P0 + 2(1-t)t·PC + t²·P1</li>
     *   <li>Tangente en t: P'(t) = 2(1-t)·(PC-P0) + 2t·(P1-PC)</li>
     * </ul>
     *
     * @param g   Contexto gráfico 2D.
     * @param x1  Coordenada X del punto inicial de la curva.
     * @param y1  Coordenada Y del punto inicial de la curva.
     * @param cpx Coordenada X del punto de control.
     * @param cpy Coordenada Y del punto de control.
     * @param x2  Coordenada X del punto final de la curva.
     * @param y2  Coordenada Y del punto final de la curva.
     * @param c   Color de la flecha.
     * @param t   Parámetro en [0,1] donde se coloca la flecha.
     */
    private void drawArrowOnCurve(Graphics2D g, double x1, double y1,
                                  double cpx, double cpy,
                                  double x2, double y2,
                                  Color c, double t) {
        // Calcular posición del punto en la curva usando la fórmula cuadrática de Bézier
        double px = (1-t)*(1-t)*x1 + 2*(1-t)*t*cpx + t*t*x2;
        double py = (1-t)*(1-t)*y1 + 2*(1-t)*t*cpy + t*t*y2;

        // Calcular la tangente (derivada de la curva en t)
        double tx = 2*(1-t)*(cpx-x1) + 2*t*(x2-cpx);
        double ty = 2*(1-t)*(cpy-y1) + 2*t*(y2-cpy);

        // Ángulo de la tangente respecto al eje X
        double angle = Math.atan2(ty, tx);

        // Construir triángulo de flecha alrededor del punto
        int ar = 10; // longitud de los lados de la flecha en píxeles
        int[] apx = {
            (int) px,
            (int)(px - ar * Math.cos(angle - 0.42)),
            (int)(px - ar * Math.cos(angle + 0.42))
        };
        int[] apy = {
            (int) py,
            (int)(py - ar * Math.sin(angle - 0.42)),
            (int)(py - ar * Math.sin(angle + 0.42))
        };

        g.setStroke(new BasicStroke(1f));
        g.setColor(c);
        g.fillPolygon(apx, apy, 3); // rellenar el triángulo de flecha
    }

    /**
     * Dibuja el camino mínimo resaltado (resultado de Dijkstra) como líneas
     * gruesas naranjas con círculos en cada vértice del camino.
     *
     * @param g    Contexto gráfico 2D.
     * @param path Lista de índices de vértices del camino.
     * @param xs   Coordenadas X de los vértices en pantalla.
     * @param ys   Coordenadas Y de los vértices en pantalla.
     */
    private void drawHighlightedPath(Graphics2D g, LinkedList<Integer> path,
                                     int[] xs, int[] ys) {
        int[] rv = toArray(path);
        if (rv.length < 2) return;

        // Dibujar las aristas del camino en naranja, gruesas y sólidas
        g.setStroke(new BasicStroke(4.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(PATH_COLOR);
        for (int i = 0; i < rv.length - 1; i++)
            g.drawLine(xs[rv[i]], ys[rv[i]], xs[rv[i+1]], ys[rv[i+1]]);

        // Marcar cada vértice del camino con un círculo de glow y uno sólido
        for (int v : rv) {
            int x = xs[v], y = ys[v];
            // Círculo de halo exterior semitransparente
            g.setColor(new Color(PATH_COLOR.getRed(), PATH_COLOR.getGreen(),
                                 PATH_COLOR.getBlue(), 90));
            g.fillOval(x - 16, y - 16, 32, 32);
            // Punto central sólido
            g.setColor(PATH_COLOR);
            g.fillOval(x - 8, y - 8, 16, 16);
        }
    }

    // ── Números de parada ────────────────────────────────────────────────────

    /**
     * Dibuja badges numéricos (1, 2, 3…) en cada parada de la ruta del camión,
     * con el color del camión correspondiente, indicando el orden de visita.
     * No dibuja badge en el depósito (primer y último elemento de la ruta).
     *
     * @param g     Contexto gráfico 2D.
     * @param route Ruta comprimida del camión (solo las paradas clave).
     * @param xs    Coordenadas X de los vértices en pantalla.
     * @param ys    Coordenadas Y de los vértices en pantalla.
     * @param c     Color del camión propietario de la ruta.
     */
    private void drawStopNumbers(Graphics2D g, LinkedList<Integer> route,
                                 int[] xs, int[] ys, Color c) {
        int[] rv = toArray(route);

        // Saltar el primer (depósito salida) y el último (depósito retorno)
        for (int i = 1; i < rv.length - 1; i++) {
            // Posicionar el badge ligeramente arriba-derecha del vértice
            int vx = xs[rv[i]] + 18;
            int vy = ys[rv[i]] - 14;

            // Fondo del badge con color del camión, semitransparente
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 200));
            g.fillRoundRect(vx - 2, vy - 12, 18, 16, 6, 6);

            // Número de orden en blanco
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.drawString(String.valueOf(i), vx + 2, vy);
        }
    }

    // ── Dibujo de vértices ───────────────────────────────────────────────────

    /**
     * Dibuja un vértice como un círculo con sombra, relleno de color según su tipo,
     * borde iluminado y etiqueta con el identificador del vértice.
     * Los vértices DEPOT y DELIVERY tienen además un halo de glow exterior.
     *
     * @param g Contexto gráfico 2D.
     * @param v Objeto {@link Vertex} con el tipo e id del vértice.
     * @param x Coordenada X del centro del círculo en pantalla.
     * @param y Coordenada Y del centro del círculo en pantalla.
     */
    private void drawVertex(Graphics2D g, Vertex v, int x, int y) {
        // Seleccionar color según tipo de vértice
        Color fill = v.isDepot()    ? DEPOT_COLOR
                   : v.isDelivery() ? DELIV_COLOR
                                    : INTER_COLOR;
        int r = VERTEX_RADIUS;

        // Halo de glow para depósito y entregas (hace que destaquen visualmente)
        if (v.isDepot() || v.isDelivery()) {
            g.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 35));
            g.fillOval(x - r - 8, y - r - 8, (r + 8) * 2, (r + 8) * 2);
        }

        // Sombra sutil desplazada 2px hacia abajo-derecha
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x - r + 2, y - r + 2, r * 2, r * 2);

        // Círculo principal relleno
        g.setColor(fill);
        g.fillOval(x - r, y - r, r * 2, r * 2);

        // Borde iluminado (versión más brillante del color base)
        g.setStroke(new BasicStroke(1.6f));
        g.setColor(fill.brighter().brighter());
        g.drawOval(x - r, y - r, r * 2, r * 2);

        // Etiqueta con el ID del vértice centrada dentro del círculo
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        String lbl = v.getId();
        g.drawString(lbl, x - fm.stringWidth(lbl) / 2, y + fm.getAscent() / 2 - 1);
    }

    // ── Leyenda ──────────────────────────────────────────────────────────────

    /**
     * Dibuja la leyenda en la esquina inferior izquierda del panel.
     * Incluye íconos para cada tipo de vértice, las aristas MST y
     * una entrada de color por cada camión con ruta asignada.
     *
     * @param g Contexto gráfico 2D.
     */
    private void drawLegend(Graphics2D g) {
        int lx = 14, itemH = 20;

        // Contar camiones con ruta para dimensionar la leyenda correctamente
        int truckCount = 0;
        if (trucks != null)
            for (Truck t : trucks)
                if (!t.getExpandedRoute().isEmpty()) truckCount++;

        // Altura total: 4 ítems fijos + uno por camión + margen superior
        int totalH = (4 + truckCount) * itemH + 30;
        int ly = getHeight() - totalH - 14; // posición Y de la leyenda

        // Fondo semitransparente de la leyenda
        g.setColor(new Color(10, 14, 24, 215));
        g.fillRoundRect(lx - 10, ly - 22, 172, totalH + 14, 12, 12);

        // Borde sutil de la leyenda
        g.setColor(new Color(50, 70, 100, 140));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(lx - 10, ly - 22, 172, totalH + 14, 12, 12);

        // Título "Leyenda"
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(210, 230, 255));
        g.drawString("Leyenda", lx, ly - 5);
        ly += 10;

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));

        // Ítems fijos: tipos de vértice y MST
        legendDot(g,  lx, ly, DEPOT_COLOR, "Deposito");     ly += itemH;
        legendDot(g,  lx, ly, DELIV_COLOR, "Entrega");      ly += itemH;
        legendDot(g,  lx, ly, INTER_COLOR, "Interseccion"); ly += itemH;
        legendLine(g, lx, ly, MST_COLOR,   "MST");          ly += itemH;

        // Ítem por cada camión con ruta
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

    /**
     * Dibuja un ítem de leyenda con un círculo de color y una etiqueta de texto.
     * Usado para representar tipos de vértices.
     *
     * @param g     Contexto gráfico 2D.
     * @param x     Coordenada X izquierda del ítem.
     * @param y     Coordenada Y base del ítem.
     * @param c     Color del círculo indicador.
     * @param label Texto descriptivo del ítem.
     */
    private void legendDot(Graphics2D g, int x, int y, Color c, String label) {
        g.setColor(c);
        g.fillOval(x, y - 8, 13, 13);           // círculo de color
        g.setColor(new Color(200, 220, 245));
        g.drawString(label, x + 20, y + 3);      // etiqueta a la derecha
    }

    /**
     * Dibuja un ítem de leyenda con una línea horizontal de color y una etiqueta.
     * Usado para representar aristas (MST o ruta de camión).
     *
     * @param g     Contexto gráfico 2D.
     * @param x     Coordenada X izquierda del ítem.
     * @param y     Coordenada Y base del ítem.
     * @param c     Color de la línea indicadora.
     * @param label Texto descriptivo del ítem.
     */
    private void legendLine(Graphics2D g, int x, int y, Color c, String label) {
        g.setColor(c);
        g.setStroke(new BasicStroke(2.8f));
        g.drawLine(x, y, x + 15, y);            // línea de muestra
        g.setColor(new Color(200, 220, 245));
        g.drawString(label, x + 20, y + 4);     // etiqueta a la derecha
    }

    // ── Cálculo de coordenadas ───────────────────────────────────────────────

    /**
     * Convierte las coordenadas lógicas de los vértices (x,y del JSON) a
     * coordenadas de pantalla, escalando y centrando el grafo dentro del área
     * disponible (descontando el margen {@code PAD} en todos los lados).
     *
     * <p>El mapeo es lineal: el vértice con menor x mapea al borde izquierdo
     * y el de mayor x al borde derecho, análogo para Y.
     *
     * @param xs Arreglo de salida con las coordenadas X en pantalla de cada vértice.
     * @param ys Arreglo de salida con las coordenadas Y en pantalla de cada vértice.
     */
    private void computeCoords(int[] xs, int[] ys) {
        // Área disponible de dibujo (descontando margen)
        int W = getWidth()  - 2 * PAD;
        int H = getHeight() - 2 * PAD;
        int V = graph.numVertices();

        // Calcular bounding box de las coordenadas lógicas
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int i = 0; i < V; i++) {
            Vertex v = graph.getVertex(i);
            if (v == null) continue;
            minX = Math.min(minX, v.getX()); maxX = Math.max(maxX, v.getX());
            minY = Math.min(minY, v.getY()); maxY = Math.max(maxY, v.getY());
        }

        // Rangos para el escalado (mínimo 1 para evitar división por cero)
        int rx = Math.max(1, maxX - minX);
        int ry = Math.max(1, maxY - minY);

        // Mapear cada vértice al espacio de pantalla
        for (int i = 0; i < V; i++) {
            Vertex v = graph.getVertex(i);
            if (v == null) { xs[i] = PAD; ys[i] = PAD; continue; }
            xs[i] = PAD + (int)((double)(v.getX() - minX) / rx * W);
            ys[i] = PAD + (int)((double)(v.getY() - minY) / ry * H);
        }
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    /**
     * Convierte una lista enlazada de enteros en un arreglo primitivo {@code int[]}.
     * Necesario porque Java2D trabaja con arreglos nativos.
     *
     * @param list Lista de índices de vértices.
     * @return Arreglo {@code int[]} con los mismos elementos en el mismo orden.
     */
    private int[] toArray(LinkedList<Integer> list) {
        int[] arr = new int[list.size()];
        int i = 0;
        for (int v : list) arr[i++] = v;
        return arr;
    }
}
