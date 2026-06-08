package ucu.edu.aed.tda.grafo.implementaciones.noDirigido;

import ucu.edu.aed.tda.grafo.IUndirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.UndirectedEdge;

import java.util.*;

public class GrafoNoDirigidoLista<V,D>
        implements IUndirectedGraph<V,D> {

    private final Map<V, Set<Edge<V,D>>> adyacencias;

    public GrafoNoDirigidoLista() {
        this.adyacencias = new HashMap<>();
    }

    @Override
    public boolean agregarVertice(V vertex) {

        if (adyacencias.containsKey(vertex))
            return false;

        adyacencias.put(vertex, new HashSet<>());
        return true;
    }

    @Override
    public V buscarVertice(Comparable<V> criterio) {

        for (V v : adyacencias.keySet()) {
            if (criterio.compareTo(v) == 0)
                return v;
        }

        return null;
    }

    @Override
    public boolean agregarArista(V source, V target, D dato) {

        if (!adyacencias.containsKey(source)
                || !adyacencias.containsKey(target))
            return false;

        Edge<V,D> edge =
                new UndirectedEdge<>(source, target, dato);

        if (adyacencias.get(source).contains(edge))
            return false;

        adyacencias.get(source).add(edge);
        adyacencias.get(target).add(edge);

        return true;
    }

    @Override
    public boolean eliminarArista(
            Comparable<V> sourceCriteria,
            Comparable<V> targetCriteria) {

        Edge<V,D> edge =
                obtenerArista(
                        sourceCriteria,
                        targetCriteria);

        if (edge == null)
            return false;

        adyacencias.get(edge.source()).remove(edge);
        adyacencias.get(edge.target()).remove(edge);

        return true;
    }

    @Override
    public boolean removerVertice(
            Comparable<V> criteria) {

        V vertice = buscarVertice(criteria);

        if (vertice == null)
            return false;

        for (Set<Edge<V,D>> edges :
                adyacencias.values()) {

            edges.removeIf(edge ->
                    edge.source().equals(vertice)
                            || edge.target().equals(vertice));
        }

        adyacencias.remove(vertice);

        return true;
    }

    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(
                adyacencias.keySet());
    }

    @Override
    public Set<Edge<V, D>> aristas() {

        Set<Edge<V,D>> resultado =
                new HashSet<>();

        for (Set<Edge<V,D>> lista :
                adyacencias.values()) {

            resultado.addAll(lista);
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

        if (source == null)
            return null;

        for (Edge<V,D> edge :
                adyacencias.get(source)) {

            boolean coincide =
                    (edge.source().equals(
                            buscarVertice(sourceCriteria))
                            &&
                            edge.target().equals(
                                    buscarVertice(targetCriteria)))
                    ||
                    (edge.source().equals(
                            buscarVertice(targetCriteria))
                            &&
                            edge.target().equals(
                                    buscarVertice(sourceCriteria)));

            if (coincide)
                return edge;
        }

        return null;
    }

    @Override
    public List<Edge<V, D>> adyacencias(
            Comparable<V> verticeCriteria) {

        V vertice =
                buscarVertice(verticeCriteria);

        if (vertice == null)
            return Collections.emptyList();

        return new ArrayList<>(
                adyacencias.get(vertice));
    }

    @Override
    public boolean esConexo() {

        if (adyacencias.isEmpty())
            return true;

        Set<V> visitados =
                new HashSet<>();

        V inicio =
                vertices().iterator().next();

        dfs(inicio, visitados);

        return visitados.size()
                == vertices().size();
    }

    private void dfs(
            V actual,
            Set<V> visitados) {

        visitados.add(actual);

        for (Edge<V,D> edge :
                adyacencias.get(actual)) {

            V vecino =
                    edge.source().equals(actual)
                            ? edge.target()
                            : edge.source();

            if (!visitados.contains(vecino))
                dfs(vecino, visitados);
        }
    }

    @Override
    public void vaciar() {
        adyacencias.clear();
    }

    @Override
    public boolean tieneCiclos() {

        Set<V> visitados =
                new HashSet<>();

        for (V v : vertices()) {

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

        for (Edge<V,D> edge :
                adyacencias.get(actual)) {

            V vecino =
                    edge.source().equals(actual)
                            ? edge.target()
                            : edge.source();

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

        return false;
    }
}