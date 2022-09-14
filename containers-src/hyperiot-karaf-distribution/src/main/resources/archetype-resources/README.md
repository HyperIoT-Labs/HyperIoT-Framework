# Karaf-HyperIoT-Microservice with Storm dist

Custom docker image with Karaf-HyperIoT microservices and Storm binaries required for managing Storm topologies.

## Building
```
mvn install
docker build . -f Dockerfile-hyperiot-runtime -t nexus.acsoftware.it:18079/terranova/terranova-microservices:1.0.0
```

## Publishing
```
docker push nexus.acsoftware.it:18079/hyperiot/karaf-microservices:1.0.0
```

