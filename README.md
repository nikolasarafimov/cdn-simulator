# CDN Simulator Project

**Authors / Contributors:** Klaudija Stamenova, Nikola Sarafimov

## Overview
This project simulates a Content Delivery Network (CDN) system, including caching strategies, server interactions and the visual routing of resource requests. It allows users to observe how different caching algorithms (FIFO, LRU, LFU) affect cache behavior across multiple servers.

When a resource is selected, the simulator starts an animation that visually demonstrates how the request is routed through the CDN network. It shows how load balancing distributes traffic and how caching decisions are made along the way. Once the routing animation completes, the requested resource is displayed along with detailed information.

The system logs each server request, indicating whether it resulted in a cache hit or miss. It also summarizes key metrics, including total hits, misses, hit ratio and total requests, enabling users to effectively evaluate caching performance.

## Accessing the Simulator
The application runs on port 8080 and can be accessed via the /cdn endpoint: http://localhost:8080/cdn. Users are redirected to the login page where they can sign in using the credentials: username: user and password: user

