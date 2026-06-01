package ucu.edu.aed.tda.grafo.implementaciones.dirigido;

import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.DirectedEdge;
import ucu.edu.aed.tda.grafo.model.edge.Edge;

import java.util.*;

public class GrafoDirigido<V, D> implements IDirectedIGraph<V, D> {

    // conjunto de vertices (no se pueden repetir)
    private final Set<V> vertices = new LinkedHashSet<>();
    //
    private final Map<V, List<Edge<V, D>>> adjacency = new LinkedHashMap<>();

    @Override
    public boolean agregarVertice(V vertex) {
        if (vertices.contains(vertex)) return false;
        vertices.add(vertex);
        adjacency.put(vertex, new ArrayList<>());
        return true;
    }

    @Override
    public V buscarVertice(Comparable<V> criterio) {
        return vertices.stream()
                .filter(v -> criterio.compareTo(v) == 0)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean agregarArista(V source, V target, D dato) {
        if (!vertices.contains(source) || !vertices.contains(target)) return false;
        if (existeArista(construirComparable(source), construirComparable(target))) return false;
        adjacency.get(source).add(new DirectedEdge<>(source, target, dato));
        return true;
    }

    @Override
    public boolean eliminarArista(Comparable<V> source, Comparable<V> target) {
        V src = buscarVertice(source);
        if (src == null) return false;
        return adjacency.get(src).removeIf(e -> target.compareTo(e.target()) == 0);
    }

    @Override
    public boolean removerVertice(Comparable<V> criteria) {
        V vertex = buscarVertice(criteria);
        if (vertex == null) return false;
        vertices.remove(vertex);
        adjacency.remove(vertex);
        // eliminar aristas que apuntan a este vértice
        adjacency.values().forEach(list -> list.removeIf(e -> e.target().equals(vertex)));
        return true;
    }

    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(vertices);
    }

    @Override
    public Set<Edge<V, D>> aristas() {
        Set<Edge<V, D>> result = new LinkedHashSet<>();
        adjacency.values().forEach(result::addAll);
        return Collections.unmodifiableSet(result);
    }

    @Override
    public boolean existeArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria) {
        return obtenerArista(sourceCriteria, targetCriteria) != null;
    }

    @Override
    public Edge<V, D> obtenerArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria) {
        V src = buscarVertice(sourceCriteria);
        if (src == null) return null;
        return adjacency.get(src).stream()
                .filter(e -> targetCriteria.compareTo(e.target()) == 0)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Edge<V, D>> adyacencias(Comparable<V> verticeCriteria) {
        V vertex = buscarVertice(verticeCriteria);
        if (vertex == null) return Collections.emptyList();
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    @Override
    public Set<V> successors(Comparable<V> criteria) {
        V vertex = buscarVertice(criteria);
        if (vertex == null) return Collections.emptySet();
        Set<V> result = new LinkedHashSet<>();
        adjacency.get(vertex).forEach(e -> result.add(e.target()));
        return result;
    }

    @Override
    public Set<V> predecessors(Comparable<V> criteria) {
        Set<V> result = new LinkedHashSet<>();
        V target = buscarVertice(criteria);
        if (target == null) return result;
        for (Map.Entry<V, List<Edge<V, D>>> entry : adjacency.entrySet()) {
            entry.getValue().stream()
                    .filter(e -> e.target().equals(target))
                    .findAny()
                    .ifPresent(e -> result.add(entry.getKey()));
        }
        return result;
    }

    @Override
    public boolean esConexo() {
        if (vertices.isEmpty()) return true;
        V start = vertices.iterator().next();
        Set<V> visited = new HashSet<>();
        dfsVisit(start, visited);
        return visited.size() == vertices.size();
    }

    @Override
    public void vaciar() {
        vertices.clear();
        adjacency.clear();
    }

    @Override
    public boolean tieneCiclos() {
        Set<V> visited = new HashSet<>();
        Set<V> inStack = new HashSet<>();
        for (V v : vertices) {
            if (!visited.contains(v)) {
                if (dfsCycle(v, visited, inStack)) return true;
            }
        }
        return false;
    }

    private void dfsVisit(V vertex, Set<V> visited) {
        visited.add(vertex);
        for (Edge<V, D> edge : adjacency.get(vertex)) {
            if (!visited.contains(edge.target())) {
                dfsVisit(edge.target(), visited);
            }
        }
    }

    private boolean dfsCycle(V vertex, Set<V> visited, Set<V> inStack) {
        visited.add(vertex);
        inStack.add(vertex);
        for (Edge<V, D> edge : adjacency.get(vertex)) {
            V neighbor = edge.target();
            if (!visited.contains(neighbor)) {
                if (dfsCycle(neighbor, visited, inStack)) return true;
            } else if (inStack.contains(neighbor)) {
                return true;
            }
        }
        inStack.remove(vertex);
        return false;
    }
}
