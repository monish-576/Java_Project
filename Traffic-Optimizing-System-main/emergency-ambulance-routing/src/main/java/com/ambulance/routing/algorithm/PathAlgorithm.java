package com.ambulance.routing.algorithm;

import com.ambulance.routing.model.AlgorithmResult;
import com.ambulance.routing.model.Graph;

public interface PathAlgorithm {

    AlgorithmResult findPath(Graph graph, String sourceId, String destinationId);

    String getAlgorithmName();
}
