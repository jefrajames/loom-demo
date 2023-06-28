package io.jefrajames.loomdemo.continuation;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/**
 * Illustrates how Continuation can be run with Virtual Threads from Callables
 * 
 */
public class ContinuationsRunInCallables {

    // Java options: --enable-preview --add-exports java.base/jdk.internal.vm=ALL-UNNAMED
    public static void main(String[] args) throws InterruptedException, ExecutionException {

        var continuationScopes = IntStream.range(10, 15)
                .mapToObj(index -> new ContinuationScope("MyScope-" + index))
                .toList();

        var continuations = IntStream.range(10, 15)
                .mapToObj(index -> new Continuation(continuationScopes.get(index - 10),
                
                        () -> {
                            System.out.println(
                                    "Step 1-Continuation " + index + " run by [" + Thread.currentThread() + "]");
                            Continuation.yield(continuationScopes.get(index - 10));
                            System.out.println(
                                    "Step 2-Continuation " + index + " run by [" + Thread.currentThread() + "]");
                            Continuation.yield(continuationScopes.get(index - 10));
                            System.out.println(
                                    "Step 3-Continuation " + index + " run by [" + Thread.currentThread() + "]");
                        }))
                .toList();

        // Each continuation called by a callable returning true
        var callables = continuations.stream()
                .<Callable<Boolean>>map(continuation -> () -> {
                    // The whole continutation is run until it is completed
                    while (!continuation.isDone())
                        continuation.run();
                    return true;
                })
                .toList();

        // Execute Callables by Virtual Threads
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = executor.invokeAll(callables);
            for (Future<Boolean> future : futures) {
                future.get();
            }
            System.out.println("Done, all callable run!");
        }
    }
}
