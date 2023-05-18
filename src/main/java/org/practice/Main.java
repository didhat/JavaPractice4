package org.practice;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите количество этажей");
        int floorNumber = scanner.nextInt();
        System.out.println("Введите вместимость лифтов");
        int capacity = scanner.nextInt();

        Elevator elevator1 = new Elevator(capacity, 0);
        Elevator elevator2 = new Elevator(capacity, 0);

        Building building = new Building(floorNumber, elevator1, elevator2);

        Executor executor = Executors.newCachedThreadPool();
        Timer t = new Timer();
        Random rand = new Random(Instant.now().getEpochSecond());
        Set<Integer> usedPersonIds = new HashSet<>();

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                executor.execute(building::lifStep);
            }
        }, 0, 1000);

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                var from = rand.nextInt(0, floorNumber + 1);
                var to = rand.nextInt(0, floorNumber + 1);
                while (from == to){
                    to = rand.nextInt(0, floorNumber + 1);
                }
                var personId = rand.nextInt(Integer.MAX_VALUE);
                while (usedPersonIds.contains(personId)){
                    personId = rand.nextInt(Integer.MAX_VALUE);
                }
                usedPersonIds.add(personId);

                int finalPersonId = personId;
                int finalTo = to;
                executor.execute(() -> building.NewLiftCall(finalPersonId, from, finalTo));

            }
        }, 0, 5000);

    }
}