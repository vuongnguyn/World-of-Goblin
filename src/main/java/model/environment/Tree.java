package model.environment;

import model.items.Item;

public class Tree extends ResourceNode {
    public Tree(double x, double y) {
        // Tree takes 3 hits and drops 2 Wood
        super(x, y, 40, 60, 3, Item.WOOD, 2);
    }

    @Override
    public void render() {
        // Render handled in GameScene
    }
}
