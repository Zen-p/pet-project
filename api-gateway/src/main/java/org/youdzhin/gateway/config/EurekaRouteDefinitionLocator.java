package org.youdzhin.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class EurekaRouteDefinitionLocator implements RouteDefinitionLocator {

    private final EurekaDiscoveryClient discoveryClient;


    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> definitions = new ArrayList<>();

        discoveryClient.getServices().forEach(serviceId -> {
            RouteDefinition definition = new RouteDefinition();
            definition.setId(serviceId);

            try {
                definition.setUri(new URI("lb://" + serviceId.toUpperCase()));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URI for service: " + serviceId, e);
            }

            definition.setPredicates(List.of(
                    new PredicateDefinition("Path=/api/v1/" + serviceId + "/**")
            ));

            definition.setFilters(List.of(
                    new FilterDefinition("RewritePath=/api/v1" + serviceId + "/(?<remaining>.*), /${remaining}")
            ));

            definitions.add(definition);
        });

        return Flux.fromIterable(definitions);
    }
}