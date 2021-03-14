package model;

public class CarMessage {
    float x;
    float y;
    float angle;
    boolean status;
    int missionSuccessCount =0;
    float electricty=100;
    public CarMessage(float x, float y, float angle){
        this.x=x;
        this.y=y;
        this.angle=angle;
        this.status=false;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getAngle() {
        return angle;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getMissionSuccessCount() {
        return missionSuccessCount;
    }

    public void setMissionSuccessCount(int missionSuccessCount) {
        this.missionSuccessCount = missionSuccessCount;
    }

    public float getElectricty() {
        return electricty;
    }

    public void setElectricty(float electricty) {
        this.electricty = electricty;
    }

    @Override
    public String toString() {
        return "CarMessage{" +
                "x=" + x +
                ", y=" + y +
                ", angle=" + angle +
                ", status=" + status +
                ", missionSuccessCount=" + missionSuccessCount +
                ", electricty=" + electricty +
                '}';
    }
}
