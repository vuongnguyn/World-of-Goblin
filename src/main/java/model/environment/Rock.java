package model.environment;

import model.items.Item;

public class Rock extends ResourceNode {
    public Rock(double x, double y) {
        // Rock takes 5 hits and drops 1 Stone
        super(x, y, 35, 30, 5, Item.STONE, 1);
    }

    @Override
    public void render() {
        // Render handled in GameScene
    }
}
