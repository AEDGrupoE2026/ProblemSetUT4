package ucu.edu.aed.tda.grafo.implementaciones;

import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;

import java.util.*;

public class DijkstraResult<V> implements IDijkstraResult<V> {

    private final Map<V, Double> costs;
    // prev[v] = vértice anterior en el camino mínimo hacia v
    private final Map<V, V> prev;

    public DijkstraResult(Map<V, Double> costs, Map<V, V> prev) {
        this.costs = costs;
        this.prev = prev;
    }

    @Override
    public double getCost(V otherVertex) {
        return costs.getOrDefault(otherVertex, Double.POSITIVE_INFINITY);
    }

    @Override
    public List<V> getPath(V otherVertex) {
        if (!costs.containsKey(otherVertex) || costs.get(otherVertex) == Double.POSITIVE_INFINITY) {
            return Collections.emptyList();
        }
        LinkedList<V> path = new LinkedList<>();
        for (V current = otherVertex; current != null; current = prev.get(current)) {
            path.addFirst(current);
        }
        return path;
    }
}
