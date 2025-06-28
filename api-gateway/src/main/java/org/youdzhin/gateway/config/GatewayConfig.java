package org.youdzhin.gateway.config;

import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GatewayConfig {



    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, RouteDefinitionLocator definitionLocator) {
        return builder.routes().build();
    }

    @Bean
    public EurekaRouteDefinitionLocator eurekaRouteDefinitionLocator(EurekaDiscoveryClient discoveryClient) {

        return new EurekaRouteDefinitionLocator(discoveryClient);
    }


}