# Web Injector

A basic multi-threaded REST/HTTP injector to test Helidon 3, Nima and Quarkus.

While running the test, a monitor displays some counters:

* throughput measured by Req/sec,
* Success
* Timeout
* Error: other kind of errors
* Response: last response provided

Can be run in two modes:

* standard: just to send traffic to a target endpoint without any warmup and throughput computation
* bench: traffic is sent to "heap" endpoint, an optional warmup can be done, the return code of each request is the current size of the heap in MB, average throughput is displayed at the end of the test.

### Building the project
```shell script
./mvnw clean package
```


### Configuration
All parameters are defined in application.yaml:
```
loom:
  threads: 10
  repeats: 60_000
  monitor:
    period_seconds: 5
    print-header: true
    print-header-lines: 10
  bench:
    active: true
    warmup: true
  client:
    connect-timeout-millis: 1000
    read-timeout-millis: 2000
    uri: http://localhost:8080/loom/quick
```
The parameter names speak for themselves. They can be redefined using Java property.

### Scripts

3 scripts are provided to facilitate the use of web-injector.

### Calling quick endpoint

runQuick.sh enables to call the quick endpoint which respond immediately. The response indicate whether the endpoint has been served by a Platform or Virtual Thread.

### Calling slow endpoint

runSlow.sh enables to call the slow endpoint which waits for a timeout before responding (5 seconds by default). The response indicate whether the endpoint has been served by a Platform or Virtual Thread.

### Checking quick vs slow endpoints collisions

runQuick.sh and runSlow.sh can be run in parallel to check whether there are collisions.

Collisions occur with imperative code and PlatForm Threads which are blocked by the sleep operation. In that case, we can see the increase of Timeout counter when calling quick. 

In contrast there is no collision with Virtual Threads.


#### To bench Helidon 3 with imperative code and Platform Threads

. go to helidon-3-demo/server directory
. start helidon 3 with ./runServerPlatform.sh
. from web-injector directory ./runBench.sh

#### To bench Helidon 3 with imperative code and Virtual Threads

. go to helidon-3-demo/server directory
. start helidon 3 with ./runServerVirtual.sh
. from web-injector directory ./runBench.sh

#### To bench Helidon 3 with reactive code

. go to helidon-3-demo/server-reactive directory
. start helidon 3 with ./runServerVirtual.sh
. from web-injector directory ./runBench.sh reactive


### To bench Nima

. go to nima-demo directory
. start nima with ./runServer.sh
. from web-injector directory ./runBench.sh

### To bench Quarkus with imperative code and Platform Threads

. go to quarkus-demo/directory
. start quarkus with ./runServer.sh
. from web-injector directory ./runBench.sh noloom

### To bench Quarkus with imperative code and Virtual Threads

. go to quarkus-demo/directory
. start quarkus with ./runServer.sh
. from web-injector directory ./runBench.sh

### To bench Quarkus with reactive code

. go to quarkus-demo/directory
. start quarkus with ./runServer.sh
. from web-injector directory ./runBench.sh reactive
