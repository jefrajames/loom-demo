# Loom with Quarkus 3

This is a demo project that illustrate how Quarkus 3 works with Virtual Threads.

## Building the project
```shell script
./mvnw clean package
```
## Running the server

./runServer.sh: start the server with appropriate Java options.

In contrast with Helidon, Quarkus enables to mix Virtual and Platform Threads in the same code and at runtime.

## Endoints

### Imperative code and Platform Threads

* **noloom/quick**: immediate response "quick-Platform"
* **noloom/slow**: response "slow-Platform" after a configurable timeout (default to 5 sec)
* **noloom/heap**: current size of the heap in MB
* **noloom/maxheap**: maximum size of the heap in MB since the server startup. May not be very accurate in case of very high concurrency (use of a volatile variable) 
* **noloom/memory**: current memory use
* **noloom/gc**: force a Garbage collection
* **noloom/pid**: pid of the server
* **noloom/pinned**: force a pinned thread situation.

### Imperative code and Virtual Threads

Same endpoints with "loom" prefix (instead of "noloom").

### Reactive code

Same endpoints with "reactive" prefix (instead of "noloom").

