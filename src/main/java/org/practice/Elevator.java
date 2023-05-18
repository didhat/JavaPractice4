package org.practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Elevator {
    public List<Map.Entry<Integer, Integer>> passengers;
    public int capacity;
    public int currentFloor;
    public int targetFloor;
    public String status; // "moving" or "stop", i am to lazy for enum, sorry...

    public Elevator(int capacity, int startFloor){
        this.status = "stop";
        this.currentFloor = startFloor;
        this.capacity = capacity;
        this.passengers = new ArrayList<>();
    }

}
