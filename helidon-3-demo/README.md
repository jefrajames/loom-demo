# Loom with Helidon 3

This is a demo project that illustrate how Helidon 3 works with Virtual Threads. Helidon 3 is production-ready and is compliant with the current Java 17 LTS.

In contrast with Helidon, Quarkus enables to mix Virtual and Platform Threads in the same code and at runtime.

## Building the project
```shell script
./mvnw clean package
```
## Endoints

### Running the server

There are in fact two servers in the project:
* the imperative one which can be run with Platform or Virtual Threads
* the reactive one.


### Imperative code and Platform Threads

First start the server with Platform Threads:

* go to server directory
* start the server with runServerPlatform.sh

* **noloom/quick**: immediate response "quick-Platform"
* **noloom/slow**: response "slow-Platform" after a configurable timeout (default to 5 sec)
* **noloom/heap**: current size of the heap in MB
* **noloom/maxheap**: maximum size of the heap in MB since the server startup. May not be very accurate in case of very high concurrency (use of a volatile variable) 
* **noloom/memory**: current memory use
* **noloom/gc**: force a Garbage collection
* **noloom/pid**: pid of the server
* **noloom/pinned**: force a pinned thread situation.

### Imperative code and Virtual Threads

First start the server with Virtual Threads:

* go to server directory
* start the server with runServerVirtual.sh

Same endpoints with "loom" prefix (instead of "noloom").

### Reactive code

First start the reactive server:

* go to server-reactive directory
* start the server with runServer.sh

Same endpoints with "reactive" prefix (instead of "noloom").
   