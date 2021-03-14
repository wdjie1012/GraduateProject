package model;

import java.util.ArrayList;
import java.util.List;

import socket.TcpMultiRobots;

public class Map {

    private byte[] datas;
    private int width;
    private int height;
    private static Map mMap = null;
    private List<State>list=new ArrayList<>();

    public void addState(State state){
        list.add(state);
    }

    public List<State> getList(){
        return list;
    }

    public void clearList(){
        this.list.clear();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setMapDatas(byte[] datas){
        this.datas=datas;
    }
    public byte[] getMapDatas(){
        return datas;
    }

    public static Map getInstance() {
        if (mMap == null) {
            synchronized (TcpMultiRobots.class) {
                if (mMap == null) {
                    mMap = new Map();
                }
            }
        }
        return mMap;
    }
}
