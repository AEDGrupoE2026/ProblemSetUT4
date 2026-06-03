package ucu.edu.aed.tda.grafo.implementaciones.dirigido;

import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.DirectedEdge;
import ucu.edu.aed.tda.grafo.model.edge.Edge;

import java.util.*;

public class GrafoDirigidoMatriz<V, D> implements IDirectedIGraph<V, D> {

    // lista de vértices que mantiene el orden de inserción (el índice en la lista es la posición en la matriz)
    private final List<V> vertices = new ArrayList<>();
    // mapa de vértice a su índice en la lista, para acceso O(1)
    private final Map<V, Integer> indices = new HashMap<>();

    @SuppressWarnings("unchecked")
    // matriz de adyacencia: matriz[i][j] contiene la arista de i a j, o null si no existe
    private Edge<V, D>[][] matriz = (Edge<V, D>[][]) new Edge[0][0];

    // agrega un vértice al grafo, devuelve false si ya existía
    @Override
    public boolean agregarVertice(V vertex) {
        if (indices.containsKey(vertex)) {
            return false; // si el vértice ya existe, no lo agrega
        }
        vertices.add(vertex);
        indices.put(vertex, vertices.size() - 1); // el nuevo índice es el último
        @SuppressWarnings("unchecked")
        Edge<V, D>[][] nueva =(Edge<V, D>[][]) new Edge[vertices.size()][vertices.size()];
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz.length; j++) {
                nueva[i][j] = matriz[i][j]; // copia las aristas existentes a la nueva matriz ampliada
            }
        }
        matriz = nueva;
        return true;
    }

    // busca un vértice usando un criterio de comparación, devuelve null si no lo encuentra
    @Override
    public V buscarVertice(Comparable<V> criterio) {
        for (V v : vertices) {
            if (criterio.compareTo(v) == 0) { // compara cada vértice con el criterio
                return v;
            }
        }
        return null;
    }

    // agrega una arista dirigida de source a target con un dato asociado
    @Override
    public boolean agregarArista(V source, V target, D dato) {
        Integer i = indices.get(source);
        Integer j = indices.get(target);
        if (i == null || j == null) {
            return false; // alguno de los vértices no existe en el grafo
        }
        if (matriz[i][j] != null) {
            return false; // ya existe una arista entre esos vértices
        }
        matriz[i][j] = new DirectedEdge<>(source, target, dato);
        return true;
    }

    // elimina la arista que va de source a target, devuelve false si no existe
    @Override
    public boolean eliminarArista(Comparable<V> source, Comparable<V> target) {
        V src = buscarVertice(source);
        V dst = buscarVertice(target);
        if (src == null || dst == null) {
            return false; // alguno de los vértices no existe
        }
        int i = indices.get(src);
        int j = indices.get(dst);
        if (matriz[i][j] == null) {
            return false; // no hay arista entre esos vértices
        }
        matriz[i][j] = null;
        return true;
    }

    // elimina un vértice del grafo y todas las aristas que lo involucran
    @Override
    public boolean removerVertice(Comparable<V> criteria) {
        V vertex = buscarVertice(criteria);
        if (vertex == null) {
            return false; // el vértice no existe
        }
        int eliminado = indices.get(vertex);
        int n = vertices.size();
        @SuppressWarnings("unchecked")
        Edge<V, D>[][] nueva =(Edge<V, D>[][]) new Edge[n - 1][n - 1];
        int nuevaFila = 0;
        for (int i = 0; i < n; i++) {
            if (i == eliminado) {
                continue; // salta la fila del vértice eliminado
            }
            int nuevaColumna = 0;
            for (int j = 0; j < n; j++) {
                if (j == eliminado) {
                    continue; // salta la columna del vértice eliminado
                }
                nueva[nuevaFila][nuevaColumna] = matriz[i][j]; // copia las aristas que no involucran al vértice eliminado
                nuevaColumna++;
            }
            nuevaFila++;
        }
        vertices.remove(eliminado);
        indices.clear();
        for (int i = 0; i < vertices.size(); i++) {
            indices.put(vertices.get(i), i); // recalcula todos los índices tras la eliminación
        }
        matriz = nueva;
        return true;
    }

    // devuelve el conjunto de vértices del grafo (no modificable)
    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(
                new LinkedHashSet<>(vertices)
        );
    }

    // devuelve todas las aristas del grafo recorriendo la matriz de adyacencia
    @Override
    public Set<Edge<V, D>> aristas() {
        Set<Edge<V, D>> resultado = new LinkedHashSet<>();
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = 0; j < vertices.size(); j++) {
                if (matriz[i][j] != null) {
                    resultado.add(matriz[i][j]); // solo agrega las celdas no nulas
                }
            }
        }
        return Collections.unmodifiableSet(resultado);
    }

    // devuelve true si existe una arista de sourceCriteria a targetCriteria
    @Override
    public boolean existeArista(
            Comparable<V> sourceCriteria,
            Comparable<V> targetCriteria) {
        return obtenerArista(
                sourceCriteria,
                targetCriteria
        ) != null; // delega en obtenerArista, si no devuelve null es que existe
    }

    // devuelve la arista que va de sourceCriteria a targetCriteria, o null si no existe
    @Override
    public Edge<V, D> obtenerArista(Comparable<V> sourceCriteria,Comparable<V> targetCriteria) {
        V source = buscarVertice(sourceCriteria);
        V target = buscarVertice(targetCriteria);
        if (source == null || target == null) {
            return null; // alguno de los vértices no existe
        }
        return matriz[
                indices.get(source)
        ][
                indices.get(target) // accede directamente a la celda [i][j] de la matriz
        ];
    }

    // devuelve la lista de aristas que salen del vértice indicado
    @Override
    public List<Edge<V, D>> adyacencias(Comparable<V> verticeCriteria) {
        V vertex = buscarVertice(verticeCriteria);
        if (vertex == null) {
            return Collections.emptyList(); // si no existe el vértice, devuelve lista vacía
        }
        int fila = indices.get(vertex);
        List<Edge<V, D>> resultado = new ArrayList<>();
        for (int j = 0; j < vertices.size(); j++) {
            if (matriz[fila][j] != null) {
                resultado.add(matriz[fila][j]); // recorre la fila del vértice buscando aristas salientes
            }
        }
        return Collections.unmodifiableList(resultado);
    }

    // devuelve el conjunto de vértices a los que se puede llegar directamente desde el vértice indicado (vecinos salientes)
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
                resultado.add(vertices.get(j)); // cada columna no nula en la fila es un sucesor
            }
        }
        return resultado;
    }

    // devuelve el conjunto de vértices que tienen una arista que apunta al vértice indicado (vecinos entrantes)
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
                resultado.add(vertices.get(i)); // cada fila no nula en la columna es un predecesor
            }
        }
        return resultado;
    }

    // devuelve true si el grafo es conexo, es decir si desde cualquier vértice se puede alcanzar a todos los demás
    @Override
    public boolean esConexo() {
        if (vertices.isEmpty()) {
            return true;
        }
        Set<V> visitados = new HashSet<>();
        dfsVisit(vertices.get(0), visitados); // arranca el DFS desde el primer vértice
        return visitados.size() == vertices.size(); // si visitó todos los vértices, el grafo es conexo
    }

    // vacía el grafo eliminando todos los vértices y aristas
    @Override
    public void vaciar() {
        vertices.clear();
        indices.clear();
        @SuppressWarnings("unchecked")
        Edge<V, D>[][] nueva =(Edge<V, D>[][]) new Edge[0][0];
        matriz = nueva;
    }

    // devuelve true si el grafo tiene al menos un ciclo
    @Override
    public boolean tieneCiclos() {
        Set<V> visitados = new HashSet<>();
        Set<V> enPila = new HashSet<>(); // guarda los vértices en el camino actual del DFS
        for (V v : vertices) {
            if (!visitados.contains(v)) { // solo arranca DFS desde vértices no visitados
                if (dfsCycle(v, visitados, enPila)) {
                    return true;
                }
            }
        }
        return false;
    }

    // recorre el grafo en profundidad (DFS) marcando los vértices visitados
    private void dfsVisit(V vertex, Set<V> visitados) {
        visitados.add(vertex);
        int fila = indices.get(vertex);
        for (int j = 0; j < vertices.size(); j++) {
            if (matriz[fila][j] != null) {
                V vecino = vertices.get(j);
                if (!visitados.contains(vecino)) { // solo visita vértices que no fueron visitados todavía
                    dfsVisit(vecino, visitados);
                }
            }
        }
    }

    // DFS que detecta ciclos usando un conjunto "enPila" que representa el camino actual
    // si se llega a un vértice que ya está en el camino actual, hay un ciclo
    private boolean dfsCycle(V vertex,Set<V> visitados,Set<V> enPila) {
        visitados.add(vertex);
        enPila.add(vertex); // entra al camino actual
        int fila = indices.get(vertex);
        for (int j = 0; j < vertices.size(); j++) {
            if (matriz[fila][j] != null) {
                V vecino = vertices.get(j);
                if (!visitados.contains(vecino)) {
                    if (dfsCycle(
                            vecino,
                            visitados,
                            enPila)) {
                        return true; // sigue el DFS
                    }
                } else if (enPila.contains(vecino)) {
                    return true; // encontró un vértice ya en el camino actual: hay ciclo
                }
            }
        }
        enPila.remove(vertex); // sale del camino actual al terminar de procesar este vértice
        return false;
    }
}
