package com.workflowbuilder.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DiscoveryServiceApplicationTest {

    @Test
    void discoveryApplicationClassExists() {
        assertEquals("com.workflowbuilder.discovery.DiscoveryServiceApplication", DiscoveryServiceApplication.class.getName());
    }
}
