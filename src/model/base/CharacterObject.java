package model.base;

public class CharacterObject extends MovableObject {
    protected int health;
    protected int damage;
    
    public CharacterObject(double x, double y, double width, double height, String imagePath, double speed, int health, int damage) {
        super(x, y, width, height, imagePath, speed);
        this.health = health;
        this.damage = damage;
    }
}
