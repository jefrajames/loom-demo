package io.jefrajames.loomdemo.continuation;

// Copyright 2023 jefrajames
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

/**
 * Simulates how an IO could work with Virtual Threads.
 * 
 * Probably a bit naive but at least gives an idea.
 */

public class ContinuationIODemo {

    // Buffer that is shared between the processing and the IO thread
    private static StringBuffer ioBuffer = new StringBuffer();

    // Java options: --enable-preview --add-exports java.base/jdk.internal.vm=ALL-UNNAMED
    public static void main(String[] args) throws InterruptedException {

        var scope1 = new ContinuationScope("scope-1");

        var continuation = new Continuation(
                scope1,
                () -> {
                    System.out.println("Doing some processing with " + Thread.currentThread());
                    System.out.println("Needing an IO now!");

                    // scope1 makes the link with the Virtual Thread
                    Continuation.yield(scope1);

                    System.out.println("Resuming processing with ioBuffer=" + ioBuffer);
                    System.out.println("Ending process");
                });

        // Run the Continuation in a Virtual Thread until it is suspended
        Thread processingThread = Thread.ofVirtual().start(continuation::run);

        processingThread.join(); // wait for the processingThread to suspend

        // Do some IO
        Runnable doSomeIO = () -> {
            System.out.println("IO started with " + Thread.currentThread());
            ioBuffer.append("<io data here>");
        };

        // Run the IO in a Platform Thread until it is completed
        Thread ioThread = Thread.ofPlatform().start(doSomeIO);
        System.out.println("IO launched, joining ioThread... ");

        ioThread.join(); // wait for the ioThread to finish

        System.out.println("IO done, resuming continuation, ioBuffer=" + ioBuffer);

        continuation.run();

        System.out.println("Continuation done, joining processingThread... ");

        processingThread.join();

        System.out.println("processingThread joined, exiting...");

    }
}