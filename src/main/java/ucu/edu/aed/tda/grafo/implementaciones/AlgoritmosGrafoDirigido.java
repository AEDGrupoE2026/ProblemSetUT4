package ucu.edu.aed.tda.grafo.implementaciones;

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

    @Override
    public <V, D extends WeightedEdge> IDijkstraResult<V> dijkstra(Comparable<V> source, IDirectedIGraph<V, D> grafo) {
        Map<V, Double> costs = new HashMap<>();
        Map<V, V> prev = new HashMap<>();

        for (V v : grafo.vertices()) costs.put(v, Double.POSITIVE_INFINITY);

        V src = grafo.buscarVertice(source);
        costs.put(src, 0.0);

        // Par (vértice, costo) para evitar reordenamientos en la PQ al actualizar costos
        PriorityQueue<Map.Entry<V, Double>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());
        pq.add(Map.entry(src, 0.0));
        Set<V> settled = new HashSet<>();

        while (!pq.isEmpty()) {
            Map.Entry<V, Double> min = pq.poll();
            V u = min.getKey();
            if (settled.contains(u)) continue;
            settled.add(u);

            for (Edge<V, D> edge : grafo.adyacencias(grafo.construirComparable(u))) {
                V v = edge.target();
                double newCost = costs.get(u) + edge.dato().getWeight();
                if (newCost < costs.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    costs.put(v, newCost);
                    prev.put(v, u);
                    pq.add(Map.entry(v, newCost));
                }
            }
        }

        return new DijkstraResult<>(costs, prev);
    }

    @Override
    public <V, D extends WeightedEdge> IFloydWarshallResult<V> floyd(IDirectedIGraph<V, D> grafo) {
        List<V> vList = new ArrayList<>(grafo.vertices());

        Map<V, Map<V, Double>> dist = new HashMap<>();
        Map<V, Map<V, V>> next = new HashMap<>();

        for (V i : vList) {
            dist.put(i, new HashMap<>());
            next.put(i, new HashMap<>());
            for (V j : vList) {
                dist.get(i).put(j, i.equals(j) ? 0.0 : Double.POSITIVE_INFINITY);
            }
        }

        for (Edge<V, D> edge : grafo.aristas()) {
            V u = edge.source();
            V v = edge.target();
            double w = edge.dato().getWeight();
            if (w < dist.get(u).get(v)) {
                dist.get(u).put(v, w);
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
                            dist.get(i).put(j, throughK);
                            next.get(i).put(j, next.get(i).get(k));
                        }
                    }
                }
            }
        }

        return new FloydWarshallResult<>(dist, next);
    }

    // Warshall: cierre transitivo. Usa peso 1 por arista (cuenta saltos, no pesos reales)
    @Override
    public <V, D extends WeightedEdge> IFloydWarshallResult<V> warshall(IDirectedIGraph<V, D> grafo) {
        List<V> vList = new ArrayList<>(grafo.vertices());

        Map<V, Map<V, Double>> reach = new HashMap<>();
        Map<V, Map<V, V>> next = new HashMap<>();

        for (V i : vList) {
            reach.put(i, new HashMap<>());
            next.put(i, new HashMap<>());
            for (V j : vList) {
                reach.get(i).put(j, i.equals(j) ? 0.0 : Double.POSITIVE_INFINITY);
            }
        }

        for (Edge<V, D> edge : grafo.aristas()) {
            V u = edge.source();
            V v = edge.target();
            reach.get(u).put(v, 1.0);
            next.get(u).put(v, v);
        }

        for (V k : vList) {
            for (V i : vList) {
                for (V j : vList) {
                    if (reach.get(i).get(k) < Double.POSITIVE_INFINITY
                            && reach.get(k).get(j) < Double.POSITIVE_INFINITY) {
                        double through = reach.get(i).get(k) + reach.get(k).get(j);
                        if (through < reach.get(i).get(j)) {
                            reach.get(i).put(j, through);
                            next.get(i).put(j, next.get(i).get(k));
                        }
                    }
                }
            }
        }

        return new FloydWarshallResult<>(reach, next);
    }

    @Override
    public <V, D extends WeightedEdge> V obtenerCentroGrafo(IDirectedIGraph<V, D> grafo) {
        IFloydWarshallResult<V> result = floyd(grafo);
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

    @Override
    public <V, D extends WeightedEdge> double obtenerExcentricidad(IDirectedIGraph<V, D> grafo, Comparable<V> vertexCriteria) {
        V vertex = grafo.buscarVertice(vertexCriteria);
        return eccentricity(grafo, vertex, floyd(grafo));
    }

    private <V> double eccentricity(IDirectedIGraph<V, ?> grafo, V vertex, IFloydWarshallResult<V> floyd) {
        double max = 0.0;
        for (V other : grafo.vertices()) {
            if (!other.equals(vertex)) {
                double cost = floyd.getCost(vertex, other);
                if (cost == Double.POSITIVE_INFINITY) return Double.POSITIVE_INFINITY;
                if (cost > max) max = cost;
            }
        }
        return max;
    }

    @Override
    public <V, D extends WeightedEdge> List<Path<V>> obtenerTodosLosCaminos(
            Comparable<V> source, Comparable<V> target, IGraph<V, D> grafo) {
        V src = grafo.buscarVertice(source);
        V tgt = grafo.buscarVertice(target);
        List<Path<V>> result = new ArrayList<>();
        if (src == null || tgt == null) return result;
        dfsAllPaths(src, tgt, grafo, new LinkedList<>(), new HashSet<>(), 0.0, result);
        return result;
    }

    private <V, D extends WeightedEdge> void dfsAllPaths(
            V current, V target, IGraph<V, D> grafo,
            LinkedList<V> path, Set<V> visited, double cost, List<Path<V>> result) {
        visited.add(current);
        path.addLast(current);
        if (current.equals(target)) {
            result.add(new Path<>(new ArrayList<>(path), cost));
        } else {
            for (Edge<V, D> edge : grafo.adyacencias(grafo.construirComparable(current))) {
                V neighbor = edge.target();
                if (!visited.contains(neighbor)) {
                    dfsAllPaths(neighbor, target, grafo, path, visited,
                            cost + edge.dato().getWeight(), result);
                }
            }
        }
        path.removeLast();
        visited.remove(current);
    }

    @Override
    public <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {
        V source = grafo.buscarVertice(sourceCriteria);
        if (source == null) return;
        dfs(source, grafo, new HashSet<>(), consumer);
    }

    private <V, D> void dfs(V vertex, IGraph<V, D> grafo, Set<V> visited, Consumer<V> consumer) {
        visited.add(vertex);
        consumer.accept(vertex);
        for (Edge<V, D> edge : grafo.adyacencias(grafo.construirComparable(vertex))) {
            V neighbor = edge.target();
            if (!visited.contains(neighbor)) {
                dfs(neighbor, grafo, visited, consumer);
            }
        }
    }

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
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
    }

    // Ordenamiento topológico por algoritmo de Kahn (BFS sobre grados de entrada)
    @Override
    public <V, D> List<V> calcularClasificacionTopologica(IDirectedIGraph<V, D> grafo) {
        Map<V, Integer> inDegree = new HashMap<>();
        for (V v : grafo.vertices()) {
            inDegree.put(v, grafo.gradoDeEntrada(grafo.construirComparable(v)));
        }
        Queue<V> queue = new LinkedList<>();
        for (Map.Entry<V, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) queue.add(e.getKey());
        }
        List<V> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            V u = queue.poll();
            result.add(u);
            for (Edge<V, D> edge : grafo.adyacencias(grafo.construirComparable(u))) {
                V neighbor = edge.target();
                int deg = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, deg);
                if (deg == 0) queue.add(neighbor);
            }
        }
        return result;
    }
}
