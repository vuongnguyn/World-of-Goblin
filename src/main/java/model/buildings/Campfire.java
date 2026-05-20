package model.buildings;

public class Campfire extends Building {
    private int fuel = 3600; // ~60 seconds of light

    public Campfire(double x, double y) {
        super(x, y, 30, 30, 100);
    }

    @Override
    public void update() {
        if (fuel > 0) {
            fuel--;
        }
    }

    public boolean isLit() {
        return fuel > 0 && !destroyed;
    }

    public void addFuel(int amount) {
        this.fuel = Math.min(this.fuel + amount, 7200); // Max 2 minutes
    }

    @Override
    public void render() {
        // Rendered in GameScene
    }
}
