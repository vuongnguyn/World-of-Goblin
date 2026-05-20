package model.environment;

import model.items.Item;

public class BerryBush extends ResourceNode {
    private long lastHarvestTime = 0;
    private static final long REGROW_TIME_FRAMES = 3600; // ~60 seconds at 60fps

    public BerryBush(double x, double y) {
        // Bush takes 1 hit (harvest) and drops 2 Berries
        super(x, y, 30, 30, 1, Item.BERRY, 2);
    }

    @Override
    public void hit() {
        if (!depleted) {
            super.hit();
        }
    }
    
    public void updateRegrowth() {
        if (depleted) {
            lastHarvestTime++;
            if (lastHarvestTime >= REGROW_TIME_FRAMES) {
                depleted = false;
                currentHits = 0;
                lastHarvestTime = 0;
            }
        }
    }

    @Override
    public void render() {
        // Render handled in GameScene
    }
}
