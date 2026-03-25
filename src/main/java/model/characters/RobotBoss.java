package model.characters;

public class RobotBoss extends Enemy {
    private boolean isPhaseTwo = false;
    
    public RobotBoss(double x, double y) {
        super(x, y, 80, 80, "" /* TODO: "assets/images/robotboss.png" */, 1.0, 500, 30);
    }

    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        if (this.health <= 250 && !isPhaseTwo) {
            isPhaseTwo = true;
            this.setSpeed(2.5); // moves faster when robot shell is broken
            // this.setPath("assets/images/human_boss.png"); // TODO: uncomment when image assets are ready
            this.damage = 40;
        }
    }
}
