package org.labs;

public final class Simulation {

    private final int programmerCount;
    private final int waiterCount;
    private final int foodAmount;

    private final long discussionDelay;
    private final long eatingDelay;
    private final long waiterDelay;

    public Simulation(
            int programmerCount,
            int waiterCount,
            int foodAmount,
            long discussionDelay,
            long eatingDelay,
            long waiterDelay
    ) {
        if (programmerCount < 2) {
            throw new IllegalArgumentException(
                    "At least 2 programmers required"
            );
        }

        if (waiterCount < 1) {
            throw new IllegalArgumentException(
                    "At least 1 waiter required"
            );
        }

        this.programmerCount = programmerCount;
        this.waiterCount = waiterCount;
        this.foodAmount = foodAmount;
        this.discussionDelay = discussionDelay;
        this.eatingDelay = eatingDelay;
        this.waiterDelay = waiterDelay;
    }

    public Result run() {
        FoodStorage storage = new FoodStorage(foodAmount);
        DiningTable table = new DiningTable(programmerCount);

        try (
                var waiters = new Waiters(storage, waiterDelay, waiterCount);
                var programmers = new Programmers(table, waiters, programmerCount, eatingDelay, discussionDelay)
        ) {
            return new Result(programmers);
        }
    }

    public record Result(Programmers programmers) {
        public void print() {
            programmers.list().forEach(p -> System.out.printf(
                    "Programmer %d: %d portions%n",
                    p.id(),
                    p.eaten()
            ));

            System.out.println(
                    "\nTotal eaten: " + totalEaten() +
                            "\nMax difference: " + (maxEaten() - minEaten())
            );
        }

        public int totalEaten() {
            return programmers.list().stream()
                    .mapToInt(Programmers.Programmer::eaten)
                    .sum();
        }

        public int minEaten() {
            return programmers.list().stream()
                    .mapToInt(Programmers.Programmer::eaten)
                    .min()
                    .orElse(0);
        }

        public int maxEaten() {
            return programmers.list().stream()
                    .mapToInt(Programmers.Programmer::eaten)
                    .max()
                    .orElse(0);
        }
    }
}
