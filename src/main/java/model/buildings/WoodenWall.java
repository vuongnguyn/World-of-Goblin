package model.buildings;

public class WoodenWall extends Building {
    public WoodenWall(double x, double y) {
        super(x, y, 40, 40, 200); // Walls have 200 health
    }

    @Override
    public void render() {
        // Rendered in GameScene
    }
}
