package io.jefrajames.loomdemo.continuation;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

/**
 * Illustrates what is a Continuation and how it can be run
 * with a Platform Thread
 * 
 */
public class ContinuationRunByPThread {

    // Java options: --enable-preview --add-exports java.base/jdk.internal.vm=ALL-UNNAMED
    public static void main(String[] args) {

        // A ContinuationScope is needed to further create Continuation
        // It makes the link with the Virtual Thread when yielding
        // It has nothing to do with StructuredTaskScope (Structured Concurrency)
        // The scope is the entity that is suspended by the running VT
        var scope = new ContinuationScope("myScope");

        // A Continuation enables to run a task linked to a scope
        // It makes no hypothesis abot how it is run
        var continuation =
              new Continuation(
                    scope,
                    () -> {
                        System.out.println("Step 1: Continuation run with " + Thread.currentThread());
                        Continuation.yield(scope); // Let's suspend the continuation scope
                        System.out.println("Step 2: Continuation run with " + Thread.currentThread());
                        Continuation.yield(scope); // Let's suspend again the continuation scope
                        System.out.println("Step 3: Continuation run with " + Thread.currentThread());
                    });

        // Run the Continuation in the main Thread until it is completed
        while ( !continuation.isDone() ) {
            continuation.run();
        }
        
    }
    
}
