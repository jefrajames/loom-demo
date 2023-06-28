# Loom with Helidon 4

This is a demo project that illustrate how Helidon 4 works with Virtual Threads. Helidon 4 is still under construction and will be compliant with the upcoming current Java 21 LTS.

## Building the project
```shell script
./mvnw clean package
```

## Running the server

./runServer.sh: start the server with appropriate Java options. Helidon 4 is Virtual Threads native end enables to run your good old imperative code in non-blocking mode.


## Endoints

* **noloom/quick**: immediate response "quick-Platform"
* **noloom/slow**: response "slow-Platform" after a configurable timeout (default to 5 sec)
* **noloom/heap**: current size of the heap in MB
* **noloom/maxheap**: maximum size of the heap in MB since the server startup. May not be very accurate in case of very high concurrency (use of a volatile variable) 
* **noloom/memory**: current memory use
* **noloom/gc**: force a Garbage collection
* **noloom/pid**: pid of the server
* **noloom/pinned**: force a pinned thread situation.