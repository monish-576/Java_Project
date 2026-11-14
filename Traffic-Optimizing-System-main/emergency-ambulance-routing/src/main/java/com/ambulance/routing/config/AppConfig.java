package com.ambulance.routing.config;

import com.ambulance.routing.algorithm.AStarAlgorithm;
import com.ambulance.routing.algorithm.BFSAlgorithm;
import com.ambulance.routing.algorithm.DFSAlgorithm;
import com.ambulance.routing.algorithm.DijkstraAlgorithm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public DijkstraAlgorithm dijkstraAlgorithm() {
        return new DijkstraAlgorithm();
    }

    @Bean
    public BFSAlgorithm bfsAlgorithm() {
        return new BFSAlgorithm();
    }

    @Bean
    public DFSAlgorithm dfsAlgorithm() {
        return new DFSAlgorithm();
    }

    @Bean
    public AStarAlgorithm aStarAlgorithm() {
        return new AStarAlgorithm();
    }
}
