package org.labs;

public final class Main {

    public static void main(String[] args) throws InterruptedException {
        Simulation simulation = new Simulation(
                7,
                2,
                10_000,
                5,
                5,
                2
        );

        long startTime = System.currentTimeMillis();
        Simulation.Result result = simulation.run();
        long endTime = System.currentTimeMillis();

        System.out.println(
                "\n=== RESULTS ===" +
                "\nTiming: " + (endTime - startTime) / 1000d + "s\n"
        );

        result.programmers().forEach(p -> System.out.printf(
                "Programmer %d: %d portions%n",
                p.id(),
                p.eaten()
        ));

        System.out.println(
                "\nTotal eaten: " + result.totalEaten() +
                "\nMax difference: " + (result.maxEaten() - result.minEaten())
        );
    }
}