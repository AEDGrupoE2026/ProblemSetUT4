package ucu.edu.aed.tda.grafo.implementaciones;

import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.DirectedEdge;
import ucu.edu.aed.tda.grafo.model.edge.Edge;

import java.util.*;

public class GrafoDirigidoMatriz<V, D> implements IDirectedIGraph<V, D> {

    private final List<V> vertices = new ArrayList<>();
    private final Map<V, Integer> indices = new HashMap<>();

    @SuppressWarnings("unchecked")
    private Edge<V, D>[][] matriz = (Edge<V, D>[][]) new Edge[0][0];

    @Override
    public boolean agregarVertice(V vertex) {
        if (indices.containsKey(vertex)) {
            return false;
        }
        vertices.add(vertex);
        indices.put(vertex, vertices.size() - 1);
        @SuppressWarnings("unchecked")
        Edge<V, D>[][] nueva =(Edge<V, D>[][]) new Edge[vertices.size()][vertices.size()];
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz.length; j++) {
                nueva[i][j] = matriz[i][j];
            }
        }
        matriz = nueva;
        return true;
    }
    @Override
    public V buscarVertice(Comparable<V> criterio) {
        for (V v : vertices) {
            if (criterio.compareTo(v) == 0) {
                return v;
            }
        }
        return null;
    }
    @Override
    public boolean agregarArista(V source, V target, D dato) {
        Integer i = indices.get(source);
        Integer j = indices.get(target);
        if (i == null || j == null) {
            return false;
        }
        if (matriz[i][j] != null) {
            return false;
        }
        matriz[i][j] = new DirectedEdge<>(source, target, dato);
        return true;
    }
    @Override
    public boolean eliminarArista(Comparable<V> source, Comparable<V> target) {
        V src = buscarVertice(source);
        V dst = buscarVertice(target);
        if (src == null || dst == null) {
            return false;
        }
        int i = indices.get(src);
        int j = indices.get(dst);
        if (matriz[i][j] == null) {
            return false;
        }
        matriz[i][j] = null;
        return true;
    }
    @Override
    public boolean removerVertice(Comparable<V> criteria) {
        V vertex = buscarVertice(criteria);
        if (vertex == null) {
            return false;
        }
        int eliminado = indices.get(vertex);
        int n = vertices.size();
        @SuppressWarnings("unchecked")
        Edge<V, D>[][] nueva =(Edge<V, D>[][]) new Edge[n - 1][n - 1];
        int nuevaFila = 0;
        for (int i = 0; i < n; i++) {
            if (i == eliminado) {
                continue;
            }
            int nuevaColumna = 0;
            for (int j = 0; j < n; j++) {
                if (j == eliminado) {
                    continue;
                }
                nueva[nuevaFila][nuevaColumna] = matriz[i][j];
                nuevaColumna++;
            }
            nuevaFila++;
        }
        vertices.remove(eliminado);
        indices.clear();
        for (int i = 0; i < vertices.size(); i++) {
            indices.put(vertices.get(i), i);
        }
        matriz = nueva;
        return true;
    }
    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(
                new LinkedHashSet<>(vertices)
        );
    }
    @Override
    public Set<Edge<V, D>> aristas() {
        Set<Edge<V, D>> resultado = new LinkedHashSet<>();
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = 0; j < vertices.size(); j++) {
                if (matriz[i][j] != null) {
                    resultado.add(matriz[i][j]);
                }
            }
        }
        return Collections.unmodifiableSet(resultado);
    }
    @Override
    public boolean existeArista(
            Comparable<V> sourceCriteria,
            Comparable<V> targetCriteria) {
        return obtenerArista(
                sourceCriteria,
                targetCriteria
        ) != null;
    }
    @Override
    public Edge<V, D> obtenerArista(Comparable<V> sourceCriteria,Comparable<V> targetCriteria) {
        V source = buscarVertice(sourceCriteria);
        V target = buscarVertice(targetCriteria);
        if (source == null || target == null) {
            return null;
        }
        return matriz[
                indices.get(source)
        ][
                indices.get(target)
        ];
    }
    @Override
    public List<Edge<V, D>> adyacencias(Comparable<V> verticeCriteria) {
        V vertex = buscarVertice(verticeCriteria);
        if (vertex == null) {
            return Collections.emptyList();
        }
        int fila = indices.get(vertex);
        List<Edge<V, D>> resultado = new ArrayList<>();
        for (int j = 0; j < vertices.size(); j++) {
            if (matriz[fila][j] != null) {
                resultado.add(matriz[fila][j]);
            }
        }
        return Collections.unmodifiableList(resultado);
    }
    @Override
    public Set<V> successors(Comparable<V> criteria) {
        Set<V> resultado = new LinkedHashSet<>();
        V vertex = buscarVertice(criteria);
        if (vertex == null) {
            return resultado;
        }
        int fila = indices.get(vertex);
        for (int j = 0; j < vertices.size(); j++) {
            if (matriz[fila][j] != null) {
                resultado.add(vertices.get(j));
            }
        }
        return resultado;
    }
    @Override
    public Set<V> predecessors(Comparable<V> criteria) {
        Set<V> resultado = new LinkedHashSet<>();
        V vertex = buscarVertice(criteria);
        if (vertex == null) {
            return resultado;
        }
        int columna = indices.get(vertex);
        for (int i = 0; i < vertices.size(); i++) {
            if (matriz[i][columna] != null) {
                resultado.add(vertices.get(i));
            }
        }
        return resultado;
    }
    @Override
    public boolean esConexo() {
        if (vertices.isEmpty()) {
            return true;
        }
        Set<V> visitados = new HashSet<>();
        dfsVisit(vertices.get(0), visitados);
        return visitados.size() == vertices.size();
    }
    @Override
    public void vaciar() {
        vertices.clear();
        indices.clear();
        @SuppressWarnings("unchecked")
        Edge<V, D>[][] nueva =(Edge<V, D>[][]) new Edge[0][0];
        matriz = nueva;
    }
    @Override
    public boolean tieneCiclos() {
        Set<V> visitados = new HashSet<>();
        Set<V> enPila = new HashSet<>();
        for (V v : vertices) {
            if (!visitados.contains(v)) {
                if (dfsCycle(v, visitados, enPila)) {
                    return true;
                }
            }
        }
        return false;
    }
    private void dfsVisit(V vertex, Set<V> visitados) {
        visitados.add(vertex);
        int fila = indices.get(vertex);
        for (int j = 0; j < vertices.size(); j++) {
            if (matriz[fila][j] != null) {
                V vecino = vertices.get(j);
                if (!visitados.contains(vecino)) {
                    dfsVisit(vecino, visitados);
                }
            }
        }
    }
    private boolean dfsCycle(V vertex,Set<V> visitados,Set<V> enPila) {
        visitados.add(vertex);
        enPila.add(vertex);
        int fila = indices.get(vertex);
        for (int j = 0; j < vertices.size(); j++) {
            if (matriz[fila][j] != null) {
                V vecino = vertices.get(j);
                if (!visitados.contains(vecino)) {
                    if (dfsCycle(
                            vecino,
                            visitados,
                            enPila)) {
                        return true;
                    }
                } else if (enPila.contains(vecino)) {
                    return true;
                }
            }
        }
        enPila.remove(vertex);
        return false;
    }
}