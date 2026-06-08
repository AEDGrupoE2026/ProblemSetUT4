package ucu.edu.aed.tda.grafo.implementaciones.noDirigido;

import ucu.edu.aed.tda.grafo.IUndirectedGraph;
import ucu.edu.aed.tda.grafo.IUndirectedGraphAlgorithm;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.*;
import java.util.function.Consumer;

public class GrafoNoDirigidoAlgoritmos implements IUndirectedGraphAlgorithm {

    @Override
    public <V, D extends WeightedEdge>
    IUndirectedGraph<V, D> kruskal(
            IUndirectedGraph<V, D> graph) {

        IUndirectedGraph<V,D> mst =new GrafoNoDirigidoLista<>();

        for (V v : graph.vertices()) {
            mst.agregarVertice(v);
        }

        List<Edge<V,D>> edges =
                new ArrayList<>(graph.aristas());

        edges.sort(
                Comparator.comparingDouble(
                        e -> e.dato().getWeight()));

        for (Edge<V,D> edge : edges) {

            mst.agregarArista(
                    edge.source(),
                    edge.target(),
                    edge.dato());

            if (mst.tieneCiclos()) {

                mst.eliminarArista(
                        mst.construirComparable(
                                edge.source()),
                        mst.construirComparable(
                                edge.target()));
            }
        }

        return mst;
    }

    @Override
    public <V, D extends WeightedEdge>
    IUndirectedGraph<V, D> prim(
            IUndirectedGraph<V, D> graph,
            Comparable<V> source) {

        IUndirectedGraph<V,D> mst =new GrafoNoDirigidoLista<>();

        for (V v : graph.vertices()) {
            mst.agregarVertice(v);
        }

        Set<V> U = new HashSet<>();
        Set<V> restantes =
                new HashSet<>(graph.vertices());

        V inicio = graph.buscarVertice(source);

        U.add(inicio);
        restantes.remove(inicio);

        while (!restantes.isEmpty()) {

            Edge<V,D> min =
                    searchMinEdge(
                            graph,
                            U,
                            restantes);

            if (min == null)
                break;

            mst.agregarArista(
                    min.source(),
                    min.target(),
                    min.dato());

            if (U.contains(min.source())) {
                U.add(min.target());
                restantes.remove(min.target());
            } else {
                U.add(min.source());
                restantes.remove(min.source());
            }
        }

        return mst;
    }

    @Override
    public <V, D extends WeightedEdge>
    Edge<V, D> searchMinEdge(
            IUndirectedGraph<V, D> graph,
            Collection<V> U,
            Collection<V> V) {

        Edge<V,D> min = null;

        double minCosto =
                Double.POSITIVE_INFINITY;

        for (Edge<V,D> edge :
                graph.aristas()) {

            boolean conectaConjuntos =
                    (U.contains(edge.source())
                            && V.contains(edge.target()))
                            ||
                    (U.contains(edge.target())
                            && V.contains(edge.source()));

            if (conectaConjuntos
                    &&
                    edge.dato().getWeight()
                            < minCosto) {

                minCosto =
                        edge.dato().getWeight();

                min = edge;
            }
        }

        return min;
    }

    @Override
    public <V, D> void bea(
            IUndirectedGraph<V, D> graph,
            Consumer<V> consumer) {

        if (graph.vertices().isEmpty())
            return;

        Set<V> visitados =
                new HashSet<>();

        Queue<V> cola =
                new LinkedList<>();

        V inicio =
                graph.vertices()
                        .iterator()
                        .next();

        cola.add(inicio);
        visitados.add(inicio);

        while (!cola.isEmpty()) {

            V actual = cola.poll();

            consumer.accept(actual);

            for (Edge<V,D> edge :
                    graph.adyacencias(
                            graph.construirComparable(actual))) {

                V vecino =
                        edge.source().equals(actual)
                                ? edge.target()
                                : edge.source();

                if (!visitados.contains(vecino)) {

                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }
    }
}