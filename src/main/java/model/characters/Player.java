package model.characters;

public class Player extends CharacterObject {
    private int level = 1;
    private int exp = 0;
    
    public Player(double x, double y, double width, double height, String imagePath, double speed, int health, int damage) {
        super(x, y, width, height, imagePath, speed, health, damage);
    }

    public void gainExp(int exp) {
        this.exp += exp;
        checkLevelUp();
    }

    private void checkLevelUp() {
        while (exp >= 100) {
            level++;
            exp -= 100;
        }
    }
}
