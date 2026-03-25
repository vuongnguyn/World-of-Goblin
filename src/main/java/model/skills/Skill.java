package model.skills;

import model.base.GameObject;
import model.characters.CharacterObject;

public abstract class Skill extends GameObject {
    protected double dx, dy;
    protected double speed;
    protected int damage;
    protected CharacterObject owner;
    protected boolean active = true;

    public Skill(double x, double y, double width, double height, String imagePath, double dx, double dy, double speed, int damage, CharacterObject owner) {
        super(x, y, width, height, imagePath);
        this.dx = dx;
        this.dy = dy;
        this.speed = speed;
        this.damage = damage;
        this.owner = owner;
    }

    @Override
    public void update() {
        if (!active) return;
        setPosition(getX() + dx * speed, getY() + dy * speed);
    }

    @Override
    public void render() {
        // Handled by view
    }

    public int getDamage() {
        return damage;
    }

    public CharacterObject getOwner() {
        return owner;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void deactivate() {
        this.active = false;
    }
    
    public boolean checkCollision(CharacterObject target) {
        if (!active) return false;
        return this.getX() < target.getX() + target.getWidth() &&
               this.getX() + this.getWidth() > target.getX() &&
               this.getY() < target.getY() + target.getHeight() &&
               this.getY() + this.getHeight() > target.getY();
    }
}
