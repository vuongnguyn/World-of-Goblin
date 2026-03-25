package model.characters;

public class Enemy extends CharacterObject {
    public Enemy(double x, double y, double width, double height, String imagePath, double speed, int health, int damage) {
        super(x, y, width, height, imagePath, speed, health, damage);
    }
    
    public void chase(CharacterObject target) {
        if (target != null && target.isAlive()) {
            double diffX = target.getX() - this.getX();
            double diffY = target.getY() - this.getY();
            double distance = Math.sqrt(diffX * diffX + diffY * diffY);
            if (distance > 0) {
                setDx(diffX / distance);
                setDy(diffY / distance);
            } else {
                setDx(0); setDy(0);
            }
        } else {
            setDx(0); setDy(0);
        }
    }
}
