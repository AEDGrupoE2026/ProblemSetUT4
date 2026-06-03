package ucu.edu.aed.tda.grafo.implementaciones.dirigido;

import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

import java.util.*;

public class FloydWarshallResult<V> implements IFloydWarshallResult<V> {

    // costo mínimo entre cada par de vértices (i, j), calculado por Floyd-Warshall o Warshall
    private final Map<V, Map<V, Double>> costs;
    // next[u][v] = siguiente vértice en el camino mínimo de u a v, permite reconstruir el camino
    private final Map<V, Map<V, V>> next;

    public FloydWarshallResult(Map<V, Map<V, Double>> costs, Map<V, Map<V, V>> next) {
        this.costs = costs;
        this.next = next;
    }

    // devuelve el costo mínimo entre source y target, o infinito si no hay camino
    @Override
    public double getCost(V source, V target) {
        return costs.getOrDefault(source, Collections.emptyMap())
                    .getOrDefault(target, Double.POSITIVE_INFINITY);
    }

    // devuelve true si existe algún camino de source a target
    @Override
    public boolean connected(V source, V target) {
        return getCost(source, target) < Double.POSITIVE_INFINITY;
    }

    // reconstruye el camino mínimo de source a target siguiendo el mapa next
    @Override
    public List<V> getPath(V source, V target) {
        if (!connected(source, target)) return Collections.emptyList(); // no hay camino
        List<V> path = new ArrayList<>();
        V current = source;
        path.add(current);
        while (!current.equals(target)) {
            current = next.get(current).get(target); // avanza al siguiente vértice en el camino
            if (current == null) return Collections.emptyList(); // camino roto, no debería ocurrir si el grafo es consistente
            path.add(current);
        }
        return path;
    }
}
