package ucu.edu.aed.tda.grafo.implementaciones.dirigido;

import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

import java.util.*;

public class FloydWarshallResult<V> implements IFloydWarshallResult<V> {

    private final Map<V, Map<V, Double>> costs;
    // next[u][v] = siguiente vértice en el camino mínimo de u a v
    private final Map<V, Map<V, V>> next;

    public FloydWarshallResult(Map<V, Map<V, Double>> costs, Map<V, Map<V, V>> next) {
        this.costs = costs;
        this.next = next;
    }

    @Override
    public double getCost(V source, V target) {
        return costs.getOrDefault(source, Collections.emptyMap())
                    .getOrDefault(target, Double.POSITIVE_INFINITY);
    }

    @Override
    public boolean connected(V source, V target) {
        return getCost(source, target) < Double.POSITIVE_INFINITY;
    }

    @Override
    public List<V> getPath(V source, V target) {
        if (!connected(source, target)) return Collections.emptyList();
        List<V> path = new ArrayList<>();
        V current = source;
        path.add(current);
        while (!current.equals(target)) {
            current = next.get(current).get(target);
            if (current == null) return Collections.emptyList();
            path.add(current);
        }
        return path;
    }
}
