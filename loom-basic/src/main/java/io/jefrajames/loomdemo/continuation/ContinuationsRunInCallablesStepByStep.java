package io.jefrajames.loomdemo.continuation;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/** 
 * Illustrates how Continuation can be run step by step from Callable by Virtual Threads
 */
public class ContinuationsRunInCallablesStepByStep {

    // Java options: --enable-preview --add-exports java.base/jdk.internal.vm=ALL-UNNAMED
    public static void main(String[] args) throws InterruptedException, ExecutionException {

        var continuationScopes = IntStream.range(10, 15)
                .mapToObj(index -> new ContinuationScope("myScope-" + index))
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

        // A true is returned at each step
        var callables = continuations.stream()
                .<Callable<Boolean>>map(continuation -> () -> {
                    continuation.run();
                    return true;
                })
                .toList();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            System.out.println("=== Step 1");
            var futures = executor.invokeAll(callables);
            for (Future<Boolean> future : futures) {
                future.get();
            }

            System.out.println("=== Step 2");
            futures = executor.invokeAll(callables);
            for (Future<Boolean> future : futures) {
                future.get();
            }
            
            System.out.println("=== Step 3");
            futures = executor.invokeAll(callables);
            for (Future<Boolean> future : futures) {
                future.get();
            }
            
        }
    }
}
