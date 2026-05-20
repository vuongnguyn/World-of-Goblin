package model.environment;

import model.base.GameObject;
import model.items.Item;

public abstract class ResourceNode extends GameObject {
    protected int maxHits;
    protected int currentHits;
    protected Item dropItem;
    protected int dropAmount;
    protected boolean depleted = false;

    public ResourceNode(double x, double y, double width, double height, int maxHits, Item dropItem, int dropAmount) {
        super(x, y, width, height, "");
        this.maxHits = maxHits;
        this.currentHits = 0;
        this.dropItem = dropItem;
        this.dropAmount = dropAmount;
    }

    public void hit() {
        if (depleted) return;
        currentHits++;
        if (currentHits >= maxHits) {
            depleted = true;
        }
    }

    public boolean isDepleted() {
        return depleted;
    }

    public Item getDropItem() {
        return dropItem;
    }

    public int getDropAmount() {
        return dropAmount;
    }

    @Override
    public void update() {
        // Static objects don't update usually
    }
}
