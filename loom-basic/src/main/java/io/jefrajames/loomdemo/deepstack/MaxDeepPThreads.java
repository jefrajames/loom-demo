package io.jefrajames.loomdemo.deepstack;

/**
 * Run MaxThreads with Platform Threads
 */
public class MaxDeepPThreads {
    public static void main(String... args) {

        try {
            MaxThreads.findMax(1000, false);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
