package ucu.edu.aed.tda.grafo.implementaciones.noDirigido;

import ucu.edu.aed.tda.grafo.IUndirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.UndirectedEdge;
import java.util.*;

public class GrafoNoDirigidoMatriz<V,D>
        implements IUndirectedGraph<V,D> {

    private final List<V> vertices;
    private final Edge<V,D>[][] matriz;
    private final int maxVertices;

    @SuppressWarnings("unchecked")
    public GrafoNoDirigidoMatriz(int maxVertices) {

        this.maxVertices = maxVertices;
        this.vertices = new ArrayList<>();

        this.matriz =
                (Edge<V,D>[][])
                        new Edge[maxVertices][maxVertices];
    }

    private int indice(V vertice) {
        return vertices.indexOf(vertice);
    }

    @Override
    public boolean agregarVertice(V vertex) {

        if (vertices.contains(vertex))
            return false;

        if (vertices.size() >= maxVertices)
            return false;

        vertices.add(vertex);
        return true;
    }

    @Override
    public V buscarVertice(Comparable<V> criterio) {

        for (V v : vertices) {
            if (criterio.compareTo(v) == 0)
                return v;
        }

        return null;
    }

    @Override
    public boolean agregarArista(
            V source,
            V target,
            D dato) {

        int i = indice(source);
        int j = indice(target);

        if (i == -1 || j == -1)
            return false;

        if (matriz[i][j] != null)
            return false;

        Edge<V,D> edge =
                new UndirectedEdge<>(source,target,dato);

        matriz[i][j] = edge;
        matriz[j][i] = edge;

        return true;
    }

    @Override
    public boolean eliminarArista(
            Comparable<V> sourceCriteria,
            Comparable<V> targetCriteria) {

        V source = buscarVertice(sourceCriteria);
        V target = buscarVertice(targetCriteria);

        if (source == null || target == null)
            return false;

        int i = indice(source);
        int j = indice(target);

        if (matriz[i][j] == null)
            return false;

        matriz[i][j] = null;
        matriz[j][i] = null;

        return true;
    }

    @Override
    public boolean removerVertice(
            Comparable<V> criteria) {

        V vertice = buscarVertice(criteria);

        if (vertice == null)
            return false;

        int idx = indice(vertice);

        vertices.remove(idx);

        for (int i = idx; i < vertices.size(); i++) {
            for (int j = 0; j < maxVertices; j++) {
                matriz[i][j] = matriz[i + 1][j];
            }
        }

        for (int j = idx; j < vertices.size(); j++) {
            for (int i = 0; i < maxVertices; i++) {
                matriz[i][j] = matriz[i][j + 1];
            }
        }

        return true;
    }

    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(
                new HashSet<>(vertices));
    }

    @Override
    public Set<Edge<V, D>> aristas() {

        Set<Edge<V,D>> resultado =
                new HashSet<>();

        for (int i = 0; i < vertices.size(); i++) {

            for (int j = i; j < vertices.size(); j++) {

                if (matriz[i][j] != null)
                    resultado.add(matriz[i][j]);
            }
        }

        return resultado;
    }

    @Override
    public boolean existeArista(
            Comparable<V> sourceCriteria,
            Comparable<V> targetCriteria) {

        return obtenerArista(
                sourceCriteria,
                targetCriteria) != null;
    }

    @Override
    public Edge<V, D> obtenerArista(
            Comparable<V> sourceCriteria,
            Comparable<V> targetCriteria) {

        V source = buscarVertice(sourceCriteria);
        V target = buscarVertice(targetCriteria);

        if (source == null || target == null)
            return null;

        return matriz[indice(source)]
                     [indice(target)];
    }

    @Override
    public List<Edge<V, D>> adyacencias(
            Comparable<V> verticeCriteria) {

        V vertice = buscarVertice(verticeCriteria);

        if (vertice == null)
            return Collections.emptyList();

        int i = indice(vertice);

        List<Edge<V,D>> resultado =
                new LinkedList<>();

        for (int j = 0; j < vertices.size(); j++) {

            if (matriz[i][j] != null)
                resultado.add(matriz[i][j]);
        }

        return resultado;
    }

    @Override
    public boolean esConexo() {

        if (vertices.isEmpty())
            return true;

        Set<V> visitados =
                new HashSet<>();

        dfs(vertices.get(0), visitados);

        return visitados.size()
                == vertices.size();
    }

    private void dfs(
            V actual,
            Set<V> visitados) {

        visitados.add(actual);

        int i = indice(actual);

        for (int j = 0; j < vertices.size(); j++) {

            if (matriz[i][j] != null) {

                V vecino = vertices.get(j);

                if (!visitados.contains(vecino))
                    dfs(vecino, visitados);
            }
        }
    }

    @Override
    public void vaciar() {

        vertices.clear();

        for (int i = 0; i < maxVertices; i++) {
            for (int j = 0; j < maxVertices; j++) {
                matriz[i][j] = null;
            }
        }
    }

    @Override
    public boolean tieneCiclos() {

        Set<V> visitados =
                new HashSet<>();

        for (V v : vertices) {

            if (!visitados.contains(v)) {

                if (dfsCiclo(
                        v,
                        null,
                        visitados))
                    return true;
            }
        }

        return false;
    }

    private boolean dfsCiclo(
            V actual,
            V padre,
            Set<V> visitados) {

        visitados.add(actual);

        int i = indice(actual);

        for (int j = 0; j < vertices.size(); j++) {

            if (matriz[i][j] != null) {

                V vecino = vertices.get(j);

                if (!visitados.contains(vecino)) {

                    if (dfsCiclo(
                            vecino,
                            actual,
                            visitados))
                        return true;
                }
                else if (!vecino.equals(padre)) {
                    return true;
                }
            }
        }

        return false;
    }
}

