package model.buildings;

import model.base.GameObject;

public abstract class Building extends GameObject {
    protected int maxHealth;
    protected int health;
    protected boolean destroyed = false;

    public Building(double x, double y, double width, double height, int maxHealth) {
        super(x, y, width, height, "");
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            this.destroyed = true;
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public void update() {
        // default static behavior
    }
}
