package org.labs;

import java.util.Arrays;

public class ApplicationConfig {

    private final int programmersCount;
    private final int waitersCount;
    private final long foodCount;
    private final long[] portionsPerProgrammer;

    public ApplicationConfig(int programmersCount, int waitersCount, long foodCount) {
        this.programmersCount = programmersCount;
        this.waitersCount = waitersCount;
        this.foodCount = foodCount;
        this.portionsPerProgrammer = calculatePortionsPerProgrammer();

        if (programmersCount <= 0) {
            throw new IllegalArgumentException("programmersCount must be positive");
        }
        if (waitersCount <= 0) {
            throw new IllegalArgumentException("waitersCount must be positive");
        }
        if (foodCount < 0 || portionsPerProgrammer.length != programmersCount) {
            throw new IllegalArgumentException("invalid food configuration");
        }
        for (long portions : portionsPerProgrammer) {
            if (portions < 0) {
                throw new IllegalArgumentException("portions must be positive");
            }
        }
    }

    public int programmersCount() {
        return programmersCount;
    }

    public int waitersCount() {
        return waitersCount;
    }

    public long foodCount() {
        return foodCount;
    }

    public long[] portionsPerProgrammer() {
        return Arrays.copyOf(portionsPerProgrammer, portionsPerProgrammer.length);
    }

    private long[] calculatePortionsPerProgrammer() {
        if (programmersCount < 2 || foodCount < 0) {
            throw new IllegalArgumentException("invalid food configuration");
        }

        long[] portions = new long[programmersCount];
        long basePortion = foodCount / programmersCount;
        long remainder = foodCount % programmersCount;
        Arrays.fill(portions, basePortion);

        for (int i = 0; i < remainder; i++) {
            portions[i]++;
        }

        return portions;
    }
}
