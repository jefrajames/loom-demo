# Loom demo
Examples of Virtual Threads (JEP 444).

### Technical Context

* **OpenJDK 20** (Temurin distribution)
* No JVM fine tuning: just trends!
* Developped and tested on MacBook/MacOS/M1 (8 cores, 16 GB RAM)
* Also tested on Linux/Intel.

### References

* Virtual Threads [JEP 444](https://openjdk.org/jeps/444)
* [Loom Getting Started](https://wiki.openjdk.org/display/loom/Getting+started)
* [Do Loom’s Claims Stack Up?](https://webtide.com/do-looms-claims-stack-up-part-1/): while a bit old, these tests remain relevant to understand the fundamentals of Virtual Threads
* [José Paumard Loom Demo](https://github.com/JosePaumard/Loom_demo/tree/main/src/main/java/org/paumard/loom/threads): the reference when it comes to speak about Loom
* [Virtual Threads power with Helidon](https://www.youtube.com/watch?v=aP-BGITYtxE): a presentation from Dmitry Aleksandrov made at DevoXX Paris 2023

This project is made of 5 independant modules:

* **loom-basic**: some basic examples using just Java SE
. **web-injector**: a REST/HTTP injector used to test Helidon, Nima and Quarkus
. **helidon-3-demo**: a demo using Helidon 3
. **nima-demo**: a demo using Nima (Helidon 4) nativelly based on Virtual Threads
. **quarkus-demo**: a demo using Quarkus 3.