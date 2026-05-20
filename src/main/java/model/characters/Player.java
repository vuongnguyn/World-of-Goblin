package model.characters;

public class Player extends CharacterObject {
    private int level = 1;
    private int exp = 0;
    private int mana = 100;
    private int maxMana = 100;
    private int maxHealth;
    
    // Survival specific
    private double hunger = 100.0;
    private double maxHunger = 100.0;
    private java.util.Map<model.items.Item, Integer> inventory = new java.util.HashMap<>();
    
    public Player(double x, double y, double width, double height, String imagePath, double speed, int health, int damage) {
        super(x, y, width, height, imagePath, speed, health, damage);
        this.maxHealth = health;
        for (model.items.Item i : model.items.Item.values()) {
            inventory.put(i, 0); // Initialize inventory
        }
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
    public double getHunger() { return hunger; }
    public double getMaxHunger() { return maxHunger; }
    public java.util.Map<model.items.Item, Integer> getInventory() { return inventory; }

    public void updateSurvivalStats() {
        hunger -= 0.02; // Hunger depletes over time
        if (hunger < 0) {
            hunger = 0;
            // Take starvation damage every few frames
            if (Math.random() < 0.05) {
                takeDamage(1);
            }
        }
    }

    public void eat(model.items.Item foodItem) {
        if (inventory.getOrDefault(foodItem, 0) > 0) {
            inventory.put(foodItem, inventory.get(foodItem) - 1);
            hunger = Math.min(hunger + 25, maxHunger);
            health = Math.min(health + 5, maxHealth); // Berries heal a tiny bit
        }
    }

    public void addItem(model.items.Item item, int amount) {
        inventory.put(item, inventory.getOrDefault(item, 0) + amount);
    }

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
