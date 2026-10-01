package org.labs;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int coderCount = 7;
        int officiantCount = 2;
        int totalFood = 10000;

        Spoon[] spoons = new Spoon[coderCount];
        for(int i = 0; i < coderCount; i++) {
            spoons[i] = new Spoon(i);
        }

        FoodSupply foodSupply = new FoodSupply(totalFood);
        OfficiantPool officiantPool = new OfficiantPool(officiantCount, foodSupply);

        Coder[] coders = new Coder[coderCount];
        Thread[] threads = new Thread[coderCount];

        for(int i = 0; i < coderCount; i++){
            Spoon left = spoons[i];
            Spoon right = spoons[(i + 1) % coderCount];
            coders[i] = new Coder(i, left, right, officiantPool);
            threads[i] = new Thread(coders[i], "Coder - " + i);
        }

        for(Thread t : threads){
            t.start();
        }

        for(Thread t : threads){
            t.join();
        }

        int total = 0;
        for(Coder c : coders){
            System.out.println("coder " + c.getMealsEaten() + " eat");
            total += c.getMealsEaten();
        }
        System.out.println("total" + total);



    }
}