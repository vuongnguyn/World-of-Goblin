package model.base;

public class MovableObject extends GameObject {
    protected double speed;
    protected double dx, dy; 
    // Hướng di chuyển:
    //  -1 cho rẽ trái, đi lên,
    //  0 cho không di chuyển,
    //  1 cho rẽ phải, đi xuống
    
    public MovableObject(double x, double y, double width, double height, String imagePath, double speed) {
        super(x, y, width, height, imagePath);
        this.speed = speed;
        this.dx = 0;
        this.dy = 0;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    @Override
    public void update() {
        if (dx != 0 && dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            dx /= length;
            dy /= length;
        }
        x += dx * speed;
        y += dy * speed;
    }

    @Override
    public void render() {
        
    }
}
