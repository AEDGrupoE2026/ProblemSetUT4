package ucu.edu.aed.tda.grafo.implementaciones.dirigido;

import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.DirectedEdge;
import ucu.edu.aed.tda.grafo.model.edge.Edge;

import java.util.*;

public class GrafoDirigidoLista<V, D> implements IDirectedIGraph<V, D> {

    // conjunto de vertices (no se pueden repetir)
    private final Set<V> vertices = new LinkedHashSet<>();
    // lista de adyacencia (guarda las aristas)
    private final Map<V, List<Edge<V, D>>> adyacencia = new LinkedHashMap<>();


    // agrega un vértice al conjunto, devuelve un booleano que indica si se pudo agregar
    @Override
    public boolean agregarVertice(V vertex) {
        if (vertices.contains(vertex)) return false; // si el vertice ya existe en el conjunto, devuelve false
        vertices.add(vertex); // agrega el vertice al conjunto
        adyacencia.put(vertex, new ArrayList<>()); // agrega el vertice al map, poniendo el vertice como key y un arraylist vacío como value
        return true;
    }

    // busca un vertice dentro del grafo usando un criterio de comparación, devuelve null si no lo encuentra
    @Override
    public V buscarVertice(Comparable<V> criterio) {
        return vertices.stream()
                .filter(v -> criterio.compareTo(v) == 0) // compara cada vértice con el criterio
                .findFirst()
                .orElse(null); // si no encuentra ninguno, devuelve null
    }

    // agrega una arista dirigida de source a target con un dato asociado
    @Override
    public boolean agregarArista(V source, V target, D dato) {
        if (!vertices.contains(source) || !vertices.contains(target)) return false; // verifica si los vértices existen en el conjunto
        if (existeArista(construirComparable(source), construirComparable(target))) return false; // verifica que no haya aristas duplicadas
        adyacencia.get(source).add(new DirectedEdge<>(source, target, dato)); // agrega la arista a la lista de adyacencia del vertice source
        return true;
    }

    // elimina la arista que va de source a target, devuelve false si no existe
    @Override
    public boolean eliminarArista(Comparable<V> source, Comparable<V> target) {
        V src = buscarVertice(source); // primero busca el vértice origen
        if (src == null) return false; // si no existe, no hay nada que eliminar
        return adyacencia.get(src).removeIf(e -> target.compareTo(e.target()) == 0); // elimina la arista cuyo destino coincida con target
    }

    // elimina un vértice del grafo y todas las aristas que lo involucran
    @Override
    public boolean removerVertice(Comparable<V> criteria) {
        V vertex = buscarVertice(criteria);
        if (vertex == null) return false; // si no existe, no hay nada que remover
        vertices.remove(vertex); // lo saca del conjunto de vértices
        adyacencia.remove(vertex); // elimina su lista de aristas salientes
        // eliminar aristas de otros vértices que apuntan a este vértice
        adyacencia.values().forEach(list -> list.removeIf(e -> e.target().equals(vertex)));
        return true;
    }

    // devuelve el conjunto de vértices del grafo (no modificable)
    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(vertices);
    }

    // devuelve todas las aristas del grafo recorriendo cada lista de adyacencia
    @Override
    public Set<Edge<V, D>> aristas() {
        Set<Edge<V, D>> result = new LinkedHashSet<>();
        adyacencia.values().forEach(result::addAll); // agrega todas las aristas de cada vértice al resultado
        return Collections.unmodifiableSet(result);
    }

    // devuelve true si existe una arista de sourceCriteria a targetCriteria
    @Override
    public boolean existeArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria) {
        return obtenerArista(sourceCriteria, targetCriteria) != null; // delega en obtenerArista, si no devuelve null es que existe
    }

    // devuelve la arista que va de sourceCriteria a targetCriteria, o null si no existe
    @Override
    public Edge<V, D> obtenerArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria) {
        V src = buscarVertice(sourceCriteria);
        if (src == null) return null; // si el origen no existe, no puede haber arista
        return adyacencia.get(src).stream()
                .filter(e -> targetCriteria.compareTo(e.target()) == 0) // busca en la lista de adyacencia la arista cuyo destino coincida
                .findFirst()
                .orElse(null);
    }

    // devuelve la lista de aristas que salen del vértice indicado
    @Override
    public List<Edge<V, D>> adyacencias(Comparable<V> verticeCriteria) {
        V vertex = buscarVertice(verticeCriteria);
        if (vertex == null) return Collections.emptyList(); // si no existe el vértice, devuelve lista vacía
        return Collections.unmodifiableList(adyacencia.get(vertex)); // devuelve su lista de adyacencia
    }

    // devuelve el conjunto de vértices a los que se puede llegar directamente desde el vértice indicado (vecinos salientes)
    @Override
    public Set<V> successors(Comparable<V> criteria) {
        V vertex = buscarVertice(criteria);
        if (vertex == null) return Collections.emptySet();
        Set<V> result = new LinkedHashSet<>();
        adyacencia.get(vertex).forEach(e -> result.add(e.target())); // extrae el destino de cada arista saliente
        return result;
    }

    // devuelve el conjunto de vértices que tienen una arista que apunta al vértice indicado (vecinos entrantes)
    @Override
    public Set<V> predecessors(Comparable<V> criteria) {
        Set<V> result = new LinkedHashSet<>();
        V target = buscarVertice(criteria);
        if (target == null) return result;
        for (Map.Entry<V, List<Edge<V, D>>> entry : adyacencia.entrySet()) {
            entry.getValue().stream()
                    .filter(e -> e.target().equals(target)) // busca aristas cuyo destino sea el vértice buscado
                    .findAny()
                    .ifPresent(e -> result.add(entry.getKey())); // si la tiene, el origen es un predecesor
        }
        return result;
    }

    // devuelve true si el grafo es conexo, es decir si desde cualquier vértice se puede alcanzar a todos los demás
    @Override
    public boolean esConexo() {
        if (vertices.isEmpty()) return true;
        V start = vertices.iterator().next(); // arranca el DFS desde el primer vértice
        Set<V> visited = new HashSet<>();
        dfsVisit(start, visited);
        return visited.size() == vertices.size(); // si visitó todos los vértices, el grafo es conexo
    }

    // vacía el grafo eliminando todos los vértices y aristas
    @Override
    public void vaciar() {
        vertices.clear();
        adyacencia.clear();
    }

    // devuelve true si el grafo tiene al menos un ciclo
    @Override
    public boolean tieneCiclos() {
        Set<V> visited = new HashSet<>();
        Set<V> inStack = new HashSet<>(); // guarda los vértices en el camino actual del DFS
        for (V v : vertices) {
            if (!visited.contains(v)) { // solo arranca DFS desde vértices no visitados
                if (dfsCycle(v, visited, inStack)) return true;
            }
        }
        return false;
    }

    // recorre el grafo en profundidad (DFS) marcando los vértices visitados
    private void dfsVisit(V vertex, Set<V> visited) {
        visited.add(vertex);
        for (Edge<V, D> edge : adyacencia.get(vertex)) {
            if (!visited.contains(edge.target())) { // solo visita vértices que no fueron visitados todavía
                dfsVisit(edge.target(), visited);
            }
        }
    }

    // DFS que detecta ciclos usando un conjunto "inStack" que representa el camino actual
    // si se llega a un vértice que ya está en el camino actual, hay un ciclo
    private boolean dfsCycle(V vertex, Set<V> visited, Set<V> inStack) {
        visited.add(vertex);
        inStack.add(vertex); // entra al camino actual
        for (Edge<V, D> edge : adyacencia.get(vertex)) {
            V neighbor = edge.target();
            if (!visited.contains(neighbor)) {
                if (dfsCycle(neighbor, visited, inStack)) return true; // sigue el DFS
            } else if (inStack.contains(neighbor)) {
                return true; // encontró un vértice ya en el camino actual: hay ciclo
            }
        }
        inStack.remove(vertex); // sale del camino actual al terminar de procesar este vértice
        return false;
    }
}
