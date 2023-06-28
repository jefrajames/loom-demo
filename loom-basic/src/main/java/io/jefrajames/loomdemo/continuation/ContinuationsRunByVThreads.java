package io.jefrajames.loomdemo.continuation;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * Illustrates how Continuations can be run with Virtual Threads
 * and Platform Threads
 * 
 */
public class ContinuationsRunByVThreads {

    // Java options: --enable-preview --add-exports java.base/jdk.internal.vm=ALL-UNNAMED
    public static void main(String[] args) throws InterruptedException {


        // Let's create some ContinuationScopes
        var continuationScopes =
              IntStream.range(10, 15)
                    .mapToObj(index -> new ContinuationScope("MyScope-" + index))
                    .toList();

        // Let's create one Continuation per ContinuationScope
        // A Continuation is not aware how it will be run
        var continuations =
              IntStream.range(10, 15)
                    .mapToObj(index -> new Continuation(continuationScopes.get(index - 10),
                          () -> {
                              System.out.println("Step 1-Continuation " + index + " running with [" + Thread.currentThread() + "]");
                              Continuation.yield(continuationScopes.get(index - 10));
                              System.out.println("Step 2-Continuation " + index + " running with [" + Thread.currentThread() + "]");
                              Continuation.yield(continuationScopes.get(index - 10));
                              System.out.println("Step 3-Continuation " + index + " running with [" + Thread.currentThread() + "]");
                          }))
                    .toList();

        System.out.println("Running step 1 with Virtual Threads...");
        var threads = new ArrayList<Thread>();
        for (Continuation continuation : continuations) {
            // Run each Continuation by a new VirtualThread
            threads.add(Thread.ofVirtual().start(continuation::run));
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("Step 1 done!");
        

        threads.clear();
        System.out.println("Running step 2 with Platform Threads...");
        for (Continuation continuation : continuations) {
            // ReRun Continuations with VirtualThreads
            threads.add(Thread.ofPlatform().start(continuation::run));
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("Step 2 done!");

        threads.clear();
        System.out.println("Running step 3 with Virtual Threads...");
        for (Continuation continuation : continuations) {
            // ReReRun Continuations with VirtualThreads
            threads.add(Thread.ofVirtual().start(continuation::run));
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("Step 3 done!");
    }
}
