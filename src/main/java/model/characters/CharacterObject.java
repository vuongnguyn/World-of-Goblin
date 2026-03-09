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
        if (x < 0) x = 0;
        if (x + width > 800) x = 800 - width;
        if (y < 0) y = 0;
        if (y + height > 600) y = 600 - height;
    }

    @Override
    public void render() {
        // Implement character-specific rendering logic here
    }
}
