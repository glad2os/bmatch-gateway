package io.bmatch.gateway.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataSourceUrlUpdater {
    private final DiscoveryClient discoveryClient;

    @Autowired
    public DataSourceUrlUpdater(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public String getHost() {
        List<ServiceInstance> instances = discoveryClient.getInstances("postgres");

        if (!instances.isEmpty()) {
            ServiceInstance instance = instances.get(0);
            return instance.getHost();
        } else {
            throw new RuntimeException("Database connection error");
        }
    }

    public int getPort() {
        List<ServiceInstance> instances = discoveryClient.getInstances("postgres");

        if (!instances.isEmpty()) {
            ServiceInstance instance = instances.get(0);
            return instance.getPort();
        } else {
            throw new RuntimeException("Database connection error");
        }
    }

}