package model.characters;

public class Player extends CharacterObject {
    private int level = 1;
    private int exp = 0;
    private int mana = 100;
    private int maxMana = 100;
    private int maxHealth;
    
    public Player(double x, double y, double width, double height, String imagePath, double speed, int health, int damage) {
        super(x, y, width, height, imagePath, speed, health, damage);
        this.maxHealth = health;
    }

    public void restoreMana(int amount) {
        this.mana = Math.min(this.mana + amount, this.maxMana);
    }

    public boolean useMana(int amount) {
        if (this.mana >= amount) {
            this.mana -= amount;
            return true;
        }
        return false;
    }

    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }
    public int getMaxHealth() { return maxHealth; }
    public int getLevel() { return level; }
    public int getExp() { return exp; }

    public void gainExp(int exp) {
        this.exp += exp;
        checkLevelUp();
    }

    private void checkLevelUp() {
        while (exp >= 100) {
            level++;
            exp -= 100;
            maxMana += 20;
            mana = maxMana;
            maxHealth += 20;
            health = maxHealth;
            damage += 5;
        }
    }
}
