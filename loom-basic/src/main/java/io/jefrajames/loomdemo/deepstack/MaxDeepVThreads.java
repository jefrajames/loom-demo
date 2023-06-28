package io.jefrajames.loomdemo.deepstack;

/**
 * Run MaxThreads with Virtual Threads
 */
public class MaxDeepVThreads {

    private static final int STACK_DEPTH = 1_000;

    // Java option: --enable-preview
    public static void main(String... args) throws Exception {
        MaxThreads.findMax(STACK_DEPTH, true);
    }
}
