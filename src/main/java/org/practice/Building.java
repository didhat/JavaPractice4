package org.practice;

import java.util.*;
import java.util.stream.IntStream;

public class Building {
    private int floorNumber;
    private final Queue<Integer> StopsQueue;
    private final List<Boolean> floorIsActive;
    private final List<Queue<Map.Entry<Integer, Integer>>> floorQueues;
    private final Elevator elevator1;
    private final Elevator elevator2;

    public Building(int floorsNumber, Elevator elevator1, Elevator elevator2){
        this.elevator1 = elevator1;
        this.elevator2 = elevator2;
        this.floorNumber = floorsNumber;

        this.StopsQueue = new ArrayDeque<>();
        this.floorIsActive = new ArrayList<>(IntStream.range(0, floorsNumber + 1).mapToObj(i -> false).toList());
        this.floorQueues = new ArrayList<>(IntStream.range(0, floorsNumber + 1).mapToObj(i -> new ArrayDeque<Map.Entry<Integer, Integer>>()).toList());
    }

    public synchronized void NewLiftCall(int personId, int startFloor, int targetFloor){
        System.out.printf("Пассажир %d вызвал лифт на %d этаже на %d этаж\n", personId, startFloor, targetFloor);

        if (!floorIsActive.get(startFloor)) {
            floorIsActive.set(startFloor, true);
            StopsQueue.offer(startFloor);
        }
        this.floorQueues.get(startFloor).offer(Map.entry(personId, targetFloor));
    }

    public synchronized void lifStep() {
        if (Objects.equals(elevator1.status, "stop")){
            if (StopsQueue.isEmpty()){
                System.out.println("Лифт 1 стоит");
            } else {
                elevator1.targetFloor = StopsQueue.poll();
                if (elevator1.targetFloor == elevator1.currentFloor){
                    processGetPerson(elevator1, 2);
                } else {
                    elevator1.status = "move";
                }
            }
        }
        if (Objects.equals(elevator2.status, "stop")){
            if (StopsQueue.isEmpty()){
                System.out.println("Лифт 2 стоит");
            } else {
                elevator2.targetFloor = StopsQueue.poll();
                if (elevator2.targetFloor == elevator2.currentFloor){
                    processGetPerson(elevator2, 2);
                } else {
                    elevator2.status = "move";
                }
            }
        }

        if (Objects.equals(elevator1.status, "move")){
            processLift(elevator1, 1);
            processLift(elevator2, 2);
        }


    }

    private void processLift(Elevator lift, int liftNumber){
        if (lift.currentFloor < lift.targetFloor){
            lift.currentFloor++;
        } else if (lift.currentFloor > lift.targetFloor){
            lift.currentFloor--;
        }
        if (lift.currentFloor == lift.targetFloor){
            processGetPerson(lift, liftNumber);
        } else {
            processWay(lift, liftNumber);
        }
    }

    private synchronized void processGetPerson(Elevator lift, int liftNumber){
        floorIsActive.set(lift.currentFloor, false);
        var personsForExit = new ArrayList<Integer>();
        for (var person : lift.passengers){
            if (person.getValue() == lift.currentFloor){
                personsForExit.add(person.getKey());
            }
        }

        for (var person : personsForExit) {
            int i = -1;
            for (int k = 0; k < lift.passengers.size(); k++){
                if (Objects.equals(lift.passengers.get(k).getKey(), person)){
                    i = k;
                    break;
                }
            }
            lift.passengers.remove(i);
            System.out.printf("Пассажир %d вышел из лифта %d на этаже %d\n", i, liftNumber, lift.currentFloor);
        }

        while (lift.passengers.size() < lift.capacity && !floorQueues.get(lift.currentFloor).isEmpty()) {
            var passenger = floorQueues.get(lift.currentFloor).poll();
            lift.passengers.add(passenger);
            assert passenger != null;
            System.out.printf("Пассажир %d зашёл в лифт на этаже %d в лифт %d\n", passenger.getKey(), lift.currentFloor, liftNumber);
        }
        if (!floorQueues.get(lift.currentFloor).isEmpty()) {
            floorIsActive.set(lift.currentFloor, true);
            StopsQueue.offer(lift.currentFloor);
        }

        if (!lift.passengers.isEmpty()) {
            lift.targetFloor = lift.passengers.get(0).getValue();
            lift.status = "move";
        } else {
            System.out.printf("Лифт %d остановился на этаже %d\n", liftNumber, lift.currentFloor);
            lift.status = "stop";
        }


    }

    private synchronized void processWay(Elevator lift, int liftNumber){
        boolean toUp = lift.currentFloor < lift.targetFloor;
        floorIsActive.set(lift.currentFloor, false);
        var personsToExit = new ArrayList<Integer>();
        for (var passenger : lift.passengers) {
            if (passenger.getValue() == lift.currentFloor) {
                personsToExit.add(passenger.getKey());
            }
        }
        for (var passengerId : personsToExit) {
            int index = -1;
            for (int i = 0; i < lift.passengers.size(); ++i) {
                if (Objects.equals(lift.passengers.get(i).getKey(), passengerId)) {
                    index = i;
                    break;
                }
            }
            lift.passengers.remove(index);
            System.out.printf("Пассажир %d вышел из лифта на этаже %d из лифта %d%n ", passengerId, lift.currentFloor, liftNumber);
        }

        var queueCopy = new ArrayDeque<>(floorQueues.get(lift.currentFloor).stream().filter(entry -> {
            if (toUp) {
                return entry.getValue() > lift.currentFloor;
            } else {
                return entry.getValue() < lift.currentFloor;
            }
        }).toList());

        while (lift.passengers.size() < lift.capacity && !queueCopy.isEmpty()) {
            var passenger = queueCopy.poll();
            lift.passengers.add(passenger);
            floorQueues.get(lift.currentFloor).removeIf(entry -> Objects.equals(entry.getKey(), passenger.getKey()));
            System.out.printf("Пассажир %d зашёл в лифт %d на этаже %d%n", passenger.getKey(), liftNumber, lift.currentFloor);
        }
        if (!floorQueues.get(lift.currentFloor).isEmpty()) {
            floorIsActive.set(lift.currentFloor, true);
            StopsQueue.offer(lift.currentFloor);
        }

        lift.status = "move";
        System.out.printf("Лифт %d едет от этажа %s к этажу %d%n", liftNumber, toUp ? "вверх" : "вниз", lift.targetFloor);
    }

}
