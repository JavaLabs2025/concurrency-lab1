package org.labs;

public class Coder implements Runnable {

    private final int id;
    private final Spoon left;
    private final Spoon right;
    private final OfficiantPool officiantPool;
    private int mealsEaten = 0;

    public Coder(int id, Spoon left, Spoon right, OfficiantPool officiantPool){
        this.id = id;
        this.left = left;
        this.right = right;
        this.officiantPool = officiantPool;
    }
    @Override
    public void run(){
        try {
            while(true){
                Thread.sleep(10);
                boolean served = officiantPool.requestFood();
                if(!served) {
                    break;
                }
                eat();
            }
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private void eat() throws InterruptedException {
        Spoon first = left.getId() < right.getId() ? left : right;
        Spoon second = left.getId() < right.getId() ? right : left;

        first.getLock().lock();
        try{
            second.getLock().lock();
            try {
                mealsEaten++;
                Thread.sleep(5);
            } finally{
                second.getLock().unlock();
            }
        } finally {
            first.getLock().unlock();
        }
    }

    public int getMealsEaten(){
        return mealsEaten;
    }
}
