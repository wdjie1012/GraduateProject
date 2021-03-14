package model;

public class State {

    private final String name;
    private final float x;
    private final float y;
    private final float angle;

    public State(String name, float x, float y, float angle) {
        this.name = name;
        this.x = x+(float)15.4;
        this.y = y+(float )15.4;
        this.angle = angle;
    }

    public String getName() {
        return name;
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

    @Override
    public String toString() {
        return "name=" + name
                + ",x=" + x
                + ",y=" + y
                + ",angle=" + angle;
    }
}
