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

    public void setDx(double dx) { this.dx = dx; }
    public void setDy(double dy) { this.dy = dy; }
    
    @Override
    public void update() {
        // Use local direction vars so dx/dy are not permanently normalized
        double dirX = dx, dirY = dy;
        if (dirX != 0 && dirY != 0) {
            double length = Math.sqrt(dirX * dirX + dirY * dirY);
            dirX /= length;
            dirY /= length;
        }
        // setPosition uses the public API — x and y are private in GameObject
        setPosition(getX() + dirX * speed, getY() + dirY * speed);
    }

    @Override
    public void render() {
        
    }
}
