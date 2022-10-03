# Modulith Principles [](id=modulith-approach)

The term "Modulith" (as opposed to the term "Monolith") is intended to highlight how the framework pushes by construction to the creation of individual modules , classic OSGI bundles, which can then be grouped inside one or more OSGI containers.

This approach also optimizes resource consumption by being able to aggregate multiple modules onto a single container. Today, unfortunately, we know that in classic microservice applications the trend is to create single containers running the micro-service (micro-monolith rather than micro-services). This approach has led in different contexts to waste in terms of resource occupancy.

A hybrid approach between Monolith and Module is possible with this configuration. In fact, an OSGI container (such as karaf for example) can be containerized and host more than a single module, making the solution more sustainable in terms of resource demands.

Another important aspect is that <b>HyperIoT</b> framework differs from frameworks such as <b>Quarkus</b> that were born with the same purpose but are verticalized on a specific technology namely <b>Kubernates</b>.

<b>HyperIoT</b> is agnostic with respect to both the OSGI runtime (karaf, felix ,etc...) and toward containerization technology making it more independent and evolvable over time.

Over time it was realized that the approach to Monolite, demonized for a long time, actually has some positive aspects that should be retained, from this view was born the hybrid approach called <b>µServices</b> as opposed to the more classic <b>micro-services</b>.


![Shipping Containers](../images/container-shipping.jpeg)