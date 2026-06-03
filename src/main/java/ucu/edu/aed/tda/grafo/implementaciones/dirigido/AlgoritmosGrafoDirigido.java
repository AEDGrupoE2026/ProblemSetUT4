package ucu.edu.aed.tda.grafo.implementaciones.dirigido;

import ucu.edu.aed.tda.grafo.IDirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.IGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.Path;

import java.util.*;
import java.util.function.Consumer;

public class AlgoritmosGrafoDirigido implements IDirectedGraphAlgorithms {

    // calcula el camino mínimo desde source a todos los vértices usando Dijkstra con cola de prioridad
    @Override
    public <V, D extends WeightedEdge> IDijkstraResult<V> dijkstra(Comparable<V> source, IDirectedIGraph<V, D> grafo) {
        Map<V, Double> costs = new HashMap<>();
        Map<V, V> prev = new HashMap<>();

        for (V v : grafo.vertices()) costs.put(v, Double.POSITIVE_INFINITY); // inicializa todos los costos como infinito

        V src = grafo.buscarVertice(source);
        costs.put(src, 0.0); // el costo al origen es 0

        // par (vértice, costo) para evitar reordenamientos en la PQ al actualizar costos
        PriorityQueue<Map.Entry<V, Double>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());
        pq.add(Map.entry(src, 0.0));
        Set<V> settled = new HashSet<>();

        while (!pq.isEmpty()) {
            Map.Entry<V, Double> min = pq.poll();
            V u = min.getKey();
            if (settled.contains(u)) continue; // si ya fue procesado, lo ignora (puede haber entradas duplicadas en la PQ)
            settled.add(u);

            for (Edge<V, D> edge : grafo.adyacencias(grafo.construirComparable(u))) {
                V v = edge.target();
                double newCost = costs.get(u) + edge.dato().getWeight();
                if (newCost < costs.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    costs.put(v, newCost); // encontró un camino más corto a v
                    prev.put(v, u); // registra u como predecesor de v en el camino mínimo
                    pq.add(Map.entry(v, newCost));
                }
            }
        }

        return new DijkstraResult<>(costs, prev);
    }

    // calcula el camino mínimo entre todos los pares de vértices usando Floyd-Warshall con los pesos reales de las aristas
    @Override
    public <V, D extends WeightedEdge> IFloydWarshallResult<V> floyd(IDirectedIGraph<V, D> grafo) {
        List<V> vList = new ArrayList<>(grafo.vertices());

        Map<V, Map<V, Double>> dist = new HashMap<>();
        Map<V, Map<V, V>> next = new HashMap<>();

        for (V i : vList) {
            dist.put(i, new HashMap<>());
            next.put(i, new HashMap<>());
            for (V j : vList) {
                dist.get(i).put(j, i.equals(j) ? 0.0 : Double.POSITIVE_INFINITY); // distancia a sí mismo es 0, al resto infinito
            }
        }

        for (Edge<V, D> edge : grafo.aristas()) {
            V u = edge.source();
            V v = edge.target();
            double w = edge.dato().getWeight();
            if (w < dist.get(u).get(v)) {
                dist.get(u).put(v, w); // inicializa con el peso de la arista directa
                next.get(u).put(v, v);
            }
        }

        for (V k : vList) {
            for (V i : vList) {
                for (V j : vList) {
                    double ik = dist.get(i).get(k);
                    double kj = dist.get(k).get(j);
                    if (ik < Double.POSITIVE_INFINITY && kj < Double.POSITIVE_INFINITY) {
                        double throughK = ik + kj;
                        if (throughK < dist.get(i).get(j)) {
                            dist.get(i).put(j, throughK); // pasar por k es más corto que el camino actual
                            next.get(i).put(j, next.get(i).get(k)); // actualiza el siguiente paso en el camino
                        }
                    }
                }
            }
        }

        return new FloydWarshallResult<>(dist, next);
    }

    // calcula el cierre transitivo con Warshall: usa peso 1 por arista (cuenta saltos, no pesos reales)
    @Override
    public <V, D extends WeightedEdge> IFloydWarshallResult<V> warshall(IDirectedIGraph<V, D> grafo) {
        List<V> vList = new ArrayList<>(grafo.vertices());

        Map<V, Map<V, Double>> reach = new HashMap<>();
        Map<V, Map<V, V>> next = new HashMap<>();

        for (V i : vList) {
            reach.put(i, new HashMap<>());
            next.put(i, new HashMap<>());
            for (V j : vList) {
                reach.get(i).put(j, i.equals(j) ? 0.0 : Double.POSITIVE_INFINITY); // inicializa igual que Floyd
            }
        }

        for (Edge<V, D> edge : grafo.aristas()) {
            V u = edge.source();
            V v = edge.target();
            reach.get(u).put(v, 1.0); // toda arista directa tiene peso 1 (un salto)
            next.get(u).put(v, v);
        }

        for (V k : vList) {
            for (V i : vList) {
                for (V j : vList) {
                    if (reach.get(i).get(k) < Double.POSITIVE_INFINITY
                            && reach.get(k).get(j) < Double.POSITIVE_INFINITY) {
                        double through = reach.get(i).get(k) + reach.get(k).get(j);
                        if (through < reach.get(i).get(j)) {
                            reach.get(i).put(j, through); // existe camino de i a j pasando por k
                            next.get(i).put(j, next.get(i).get(k));
                        }
                    }
                }
            }
        }

        return new FloydWarshallResult<>(reach, next);
    }

    // devuelve el centro del grafo: el vértice con menor excentricidad (más "central")
    @Override
    public <V, D extends WeightedEdge> V obtenerCentroGrafo(IDirectedIGraph<V, D> grafo) {
        IFloydWarshallResult<V> result = floyd(grafo); // reutiliza Floyd para no calcularlo varias veces
        V center = null;
        double minEcc = Double.POSITIVE_INFINITY;
        for (V v : grafo.vertices()) {
            double ecc = eccentricity(grafo, v, result);
            if (ecc < minEcc) {
                minEcc = ecc;
                center = v;
            }
        }
        return center;
    }

    // devuelve la excentricidad del vértice indicado: distancia máxima a cualquier otro vértice
    @Override
    public <V, D extends WeightedEdge> double obtenerExcentricidad(IDirectedIGraph<V, D> grafo, Comparable<V> vertexCriteria) {
        V vertex = grafo.buscarVertice(vertexCriteria);
        return eccentricity(grafo, vertex, floyd(grafo));
    }

    // calcula la excentricidad de un vértice como el máximo costo hacia los demás vértices del grafo
    private <V> double eccentricity(IDirectedIGraph<V, ?> grafo, V vertex, IFloydWarshallResult<V> floyd) {
        double max = 0.0;
        for (V other : grafo.vertices()) {
            if (!other.equals(vertex)) {
                double cost = floyd.getCost(vertex, other);
                if (cost == Double.POSITIVE_INFINITY) return Double.POSITIVE_INFINITY; // si hay algún vértice no alcanzable, la excentricidad es infinita
                if (cost > max) max = cost;
            }
        }
        return max;
    }

    // devuelve todos los caminos simples (sin ciclos) desde source hasta target usando DFS
    @Override
    public <V, D extends WeightedEdge> List<Path<V>> obtenerTodosLosCaminos(
            Comparable<V> source, Comparable<V> target, IGraph<V, D> grafo) {
        V src = grafo.buscarVertice(source);
        V tgt = grafo.buscarVertice(target);
        List<Path<V>> result = new ArrayList<>();
        if (src == null || tgt == null) return result; // alguno de los vértices no existe
        dfsAllPaths(src, tgt, grafo, new LinkedList<>(), new HashSet<>(), 0.0, result);
        return result;
    }

    // DFS que explora todos los caminos simples hasta el destino, acumulando el costo de cada uno
    private <V, D extends WeightedEdge> void dfsAllPaths(
            V current, V target, IGraph<V, D> grafo,
            LinkedList<V> path, Set<V> visited, double cost, List<Path<V>> result) {
        visited.add(current);
        path.addLast(current);
        if (current.equals(target)) {
            result.add(new Path<>(new ArrayList<>(path), cost)); // llegó al destino, guarda una copia del camino
        } else {
            for (Edge<V, D> edge : grafo.adyacencias(grafo.construirComparable(current))) {
                V neighbor = edge.target();
                if (!visited.contains(neighbor)) {
                    dfsAllPaths(neighbor, target, grafo, path, visited,
                            cost + edge.dato().getWeight(), result);
                }
            }
        }
        path.removeLast(); // retrocede para explorar otros caminos (backtracking)
        visited.remove(current);
    }

    // recorre el grafo en profundidad desde sourceCriteria aplicando consumer a cada vértice visitado
    @Override
    public <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {
        V source = grafo.buscarVertice(sourceCriteria);
        if (source == null) return;
        dfs(source, grafo, new HashSet<>(), consumer);
    }

    // DFS recursivo que visita cada vértice una sola vez y aplica el consumer
    private <V, D> void dfs(V vertex, IGraph<V, D> grafo, Set<V> visited, Consumer<V> consumer) {
        visited.add(vertex);
        consumer.accept(vertex);
        for (Edge<V, D> edge : grafo.adyacencias(grafo.construirComparable(vertex))) {
            V neighbor = edge.target();
            if (!visited.contains(neighbor)) {
                dfs(neighbor, grafo, visited, consumer); // solo visita vecinos no visitados
            }
        }
    }

    // recorre el grafo en amplitud (BFS) desde sourceCriteria aplicando consumer a cada vértice en orden de nivel
    @Override
    public <V, D> void recorridoEnAmplitud(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {
        V source = grafo.buscarVertice(sourceCriteria);
        if (source == null) return;
        Set<V> visited = new HashSet<>();
        Queue<V> queue = new LinkedList<>();
        visited.add(source);
        queue.add(source);
        while (!queue.isEmpty()) {
            V current = queue.poll();
            consumer.accept(current);
            for (Edge<V, D> edge : grafo.adyacencias(grafo.construirComparable(current))) {
                V neighbor = edge.target();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor); // marca antes de encolar para evitar duplicados en la cola
                    queue.add(neighbor);
                }
            }
        }
    }

    // ordenamiento topológico por algoritmo de Kahn: BFS sobre los vértices con grado de entrada 0
    @Override
    public <V, D> List<V> calcularClasificacionTopologica(IDirectedIGraph<V, D> grafo) {
        Map<V, Integer> inDegree = new HashMap<>();
        for (V v : grafo.vertices()) {
            inDegree.put(v, grafo.gradoDeEntrada(grafo.construirComparable(v)));
        }
        Queue<V> queue = new LinkedList<>();
        for (Map.Entry<V, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) queue.add(e.getKey()); // los vértices sin predecesores son el punto de partida
        }
        List<V> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            V u = queue.poll();
            result.add(u);
            for (Edge<V, D> edge : grafo.adyacencias(grafo.construirComparable(u))) {
                V neighbor = edge.target();
                int deg = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, deg);
                if (deg == 0) queue.add(neighbor); // al reducirse el grado a 0, el vértice ya puede ser procesado
            }
        }
        return result;
    }
}
