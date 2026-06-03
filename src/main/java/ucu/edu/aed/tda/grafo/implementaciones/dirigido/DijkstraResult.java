package ucu.edu.aed.tda.grafo.implementaciones.dirigido;

import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;

import java.util.*;

public class DijkstraResult<V> implements IDijkstraResult<V> {

    // costo mínimo desde el origen hasta cada vértice
    private final Map<V, Double> costs;
    // prev[v] = vértice anterior en el camino mínimo hacia v, permite reconstruir el camino
    private final Map<V, V> prev;

    public DijkstraResult(Map<V, Double> costs, Map<V, V> prev) {
        this.costs = costs;
        this.prev = prev;
    }

    // devuelve el costo mínimo desde el origen hasta otherVertex, o infinito si no es alcanzable
    @Override
    public double getCost(V otherVertex) {
        return costs.getOrDefault(otherVertex, Double.POSITIVE_INFINITY);
    }

    // reconstruye el camino mínimo desde el origen hasta otherVertex siguiendo el mapa prev hacia atrás
    @Override
    public List<V> getPath(V otherVertex) {
        if (!costs.containsKey(otherVertex) || costs.get(otherVertex) == Double.POSITIVE_INFINITY) {
            return Collections.emptyList(); // el vértice no existe o no es alcanzable
        }
        LinkedList<V> path = new LinkedList<>();
        for (V current = otherVertex; current != null; current = prev.get(current)) {
            path.addFirst(current); // inserta al frente para obtener el camino en orden origen→destino
        }
        return path;
    }
}
