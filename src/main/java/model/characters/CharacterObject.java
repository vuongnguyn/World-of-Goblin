package model.characters;

import model.base.MovableObject;

public class CharacterObject extends MovableObject {
    protected int health;
    protected int damage;
    
    public CharacterObject(double x, double y, double width, double height, String imagePath, double speed, int health, int damage) {
        super(x, y, width, height, imagePath, speed);
        this.health = health;
        this.damage = damage;
    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health < 0) {
            this.health = 0;
        }
    }

    public boolean isAlive() {
        return health > 0;
    }

    public int getDamage() {
        return damage;
    }

    public void attack(CharacterObject target) {
        if (target.isAlive()) {
            target.takeDamage(this.damage);
        }
    }

    @Override
    public void update() {
        // Implement character-specific update logic here
        super.update();
        double nx = getX(), ny = getY(), w = getWidth(), h = getHeight();
        if (nx < 0) nx = 0;
        if (nx + w > 800) nx = 800 - w;
        if (ny < 0) ny = 0;
        if (ny + h > 600) ny = 600 - h;
        setPosition(nx, ny);
    }

    @Override
    public void render() {
        // Implement character-specific rendering logic here
    }
}
