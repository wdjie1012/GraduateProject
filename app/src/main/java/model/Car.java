package model;

import java.util.HashMap;
import java.util.Map;

import socket.TcpMultiRobots;

public class Car {
    private static Car mCar = null;

    int carCounts;
    Map<Integer, CarMessage> carPoints=new HashMap<>();

    public void setCarCounts(int carCounts){
        this.carCounts=carCounts;
    }

    public void addCar(int carNumber, CarMessage carMessage){
        this.carPoints.put(carNumber, carMessage);
    }

    public Map<Integer, CarMessage> getCarInfo(){
        return this.carPoints;
    }

    public int getCarCounts(){
        return this.carCounts;
    }

    public CarMessage getCarPoint(int num){
        return carPoints.get(num);
    }

    public static Car getInstance() {
        if (mCar == null) {
            synchronized (TcpMultiRobots.class) {
                if (mCar == null) {
                    mCar = new Car();
                }
            }
        }
        return mCar;
    }
}
