package controller;

import javafx.scene.input.KeyCode;
import model.characters.*;
import model.environment.*;
import model.buildings.*;
import model.items.Item;
import model.skills.Skill;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GameManager {
    public static final int TILE_SIZE = 32;
    public static final int MAP_COLS = 50; // Bigger map
    public static final int MAP_ROWS = 40;

    private Player player;
    private GameState gameState;
    private List<Enemy> enemies;
    private List<Skill> activeSkills; // Keep for future use or ranged enemies
    private List<ResourceNode> resources;
    private List<Building> buildings;

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    // Day/Night Cycle (60fps)
    // Day = 3600 frames (60s), Night = 1800 frames (30s)
    private long gameTime = 0;
    private static final int DAY_LENGTH = 3600;
    private static final int NIGHT_LENGTH = 1800;

    // Melee / Interaction
    private int actionCooldown = 0;
    
    // Hotbar selection (0: empty hand, 1: Campfire, 2: Wall, 3: Eat Berry)
    private int selectedSlot = 0;

    public enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, WIN
    }

    public void initializeGame() {
        // Spawn in center
        player = new Player(MAP_COLS * TILE_SIZE / 2.0, MAP_ROWS * TILE_SIZE / 2.0, 40, 40, "", 3.0, 150, 20);
        enemies = new ArrayList<>();
        activeSkills = new ArrayList<>();
        resources = new ArrayList<>();
        buildings = new ArrayList<>();
        
        generateWorld();
        
        gameTime = 0;
        gameState = GameState.PLAYING;
    }

    private void generateWorld() {
        // Procedural scatter
        for (int i = 0; i < 150; i++) {
            double rx = Math.random() * MAP_COLS * TILE_SIZE;
            double ry = Math.random() * MAP_ROWS * TILE_SIZE;
            double type = Math.random();
            if (type < 0.6) resources.add(new Tree(rx, ry));
            else if (type < 0.85) resources.add(new Rock(rx, ry));
            else resources.add(new BerryBush(rx, ry));
        }
    }

    public void handleInput(KeyCode keyCode, boolean isPressed) {
        if (isPressed) {
            pressedKeys.add(keyCode);
            
            // Hotbar selection
            if (keyCode == KeyCode.DIGIT1) selectedSlot = 1; // Campfire
            if (keyCode == KeyCode.DIGIT2) selectedSlot = 2; // Wooden Wall
            if (keyCode == KeyCode.DIGIT3) selectedSlot = 3; // Eat Berry
            if (keyCode == KeyCode.DIGIT4) selectedSlot = 0; // Empty Hand
            
            // Interact / Attack / Build
            if (keyCode == KeyCode.SPACE || keyCode == KeyCode.E) {
                performAction();
            }
        } else {
            pressedKeys.remove(keyCode);
        }
    }
    
    private void performAction() {
        if (actionCooldown > 0) return;
        
        // 1. Eat Berry
        if (selectedSlot == 3) {
            player.eat(Item.BERRY);
            actionCooldown = 15;
            return;
        }
        
        // 2. Build Campfire
        if (selectedSlot == 1) {
            if (player.getInventory().getOrDefault(Item.WOOD, 0) >= 5 &&
                player.getInventory().getOrDefault(Item.STONE, 0) >= 2) {
                player.addItem(Item.WOOD, -5);
                player.addItem(Item.STONE, -2);
                buildings.add(new Campfire(player.getX() + 50, player.getY()));
            }
            actionCooldown = 20;
            return;
        }
        
        // 3. Build Wall
        if (selectedSlot == 2) {
            if (player.getInventory().getOrDefault(Item.WOOD, 0) >= 2) {
                player.addItem(Item.WOOD, -2);
                buildings.add(new WoodenWall(player.getX() + 50, player.getY()));
            }
            actionCooldown = 20;
            return;
        }
        
        // 4. Melee hit (Gather resources or Attack enemies)
        actionCooldown = 20;
        
        // Check enemies first
        for (Enemy e : enemies) {
            if (distance(player, e) < 60) {
                e.takeDamage(player.getDamage());
                return; // hit only one thing
            }
        }
        
        // Check resources
        for (ResourceNode r : resources) {
            if (!r.isDepleted() && distance(player, r) < 60) {
                r.hit();
                if (r.isDepleted()) {
                    player.addItem(r.getDropItem(), r.getDropAmount());
                }
                return;
            }
        }
    }

    public void update() {
        if (gameState != GameState.PLAYING) return;
        
        gameTime++;
        if (actionCooldown > 0) actionCooldown--;
        
        player.updateSurvivalStats();

        // ------ Player movement ------
        double pdx = 0, pdy = 0;
        if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT)) pdx = -1;
        if (pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT)) pdx = 1;
        if (pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP)) pdy = -1;
        if (pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN)) pdy = 1;
        player.setDx(pdx);
        player.setDy(pdy);
        
        // Basic wall collision for player
        double oldX = player.getX(), oldY = player.getY();
        player.update();
        for (Building b : buildings) {
            if (b instanceof WoodenWall && intersects(player, b)) {
                player.setPosition(oldX, oldY); // block movement
            }
        }

        // ------ Night Spawning ------
        if (isNight() && Math.random() < 0.02) {
            double sx = Math.random() < 0.5 ? player.getX() - 500 : player.getX() + 500;
            double sy = Math.random() < 0.5 ? player.getY() - 500 : player.getY() + 500;
            if (Math.random() < 0.3) enemies.add(new Monster(sx, sy));
            else enemies.add(new Robber(sx, sy));
        }

        // ------ Update enemies ------
        Iterator<Enemy> ei = enemies.iterator();
        while (ei.hasNext()) {
            Enemy e = ei.next();
            e.chase(player);
            
            double eOldX = e.getX(), eOldY = e.getY();
            e.update();
            
            // Enemy vs Wall collision
            for (Building b : buildings) {
                if (b instanceof WoodenWall && !b.isDestroyed() && intersects(e, b)) {
                    e.setPosition(eOldX, eOldY);
                    b.takeDamage(1); // Enemies slowly break walls
                }
            }

            if (intersects(player, e)) {
                player.takeDamage(e.getDamage() / 60); 
            }
            
            // Daylight burns some enemies
            if (!isNight() && e instanceof Monster) {
                e.takeDamage(1);
            }
            
            if (!e.isAlive()) {
                player.gainExp(30);
                ei.remove();
            }
        }
        
        // ------ Update Buildings & Resources ------
        Iterator<Building> bi = buildings.iterator();
        while (bi.hasNext()) {
            Building b = bi.next();
            b.update();
            if (b.isDestroyed()) {
                bi.remove();
            }
        }
        
        for (ResourceNode r : resources) {
            if (r instanceof BerryBush) {
                ((BerryBush) r).updateRegrowth();
            }
        }

        // ------ Check win / lose ------
        if (!player.isAlive()) {
            gameState = GameState.GAME_OVER;
        }
    }
    
    public boolean isNight() {
        long cycleTime = gameTime % (DAY_LENGTH + NIGHT_LENGTH);
        return cycleTime > DAY_LENGTH;
    }
    
    public double getDayProgress() {
        return (double) (gameTime % (DAY_LENGTH + NIGHT_LENGTH)) / (DAY_LENGTH + NIGHT_LENGTH);
    }

    private double distance(GameObject a, GameObject b) {
        double dx = (a.getX() + a.getWidth()/2) - (b.getX() + b.getWidth()/2);
        double dy = (a.getY() + a.getHeight()/2) - (b.getY() + b.getHeight()/2);
        return Math.sqrt(dx*dx + dy*dy);
    }

    private boolean intersects(GameObject a, GameObject b) {
        return a.getX() < b.getX() + b.getWidth() &&
               a.getX() + a.getWidth() > b.getX() &&
               a.getY() < b.getY() + b.getHeight() &&
               a.getY() + a.getHeight() > b.getY();
    }

    // ---- Getters ----
    public Player getPlayer() { return player; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Skill> getActiveSkills() { return activeSkills; }
    public List<ResourceNode> getResources() { return resources; }
    public List<Building> getBuildings() { return buildings; }
    public GameState getGameState() { return gameState; }
    public void setGameState(GameState state) { this.gameState = state; }
    public int getSelectedSlot() { return selectedSlot; }
    
    // We don't use tilemap directly anymore
    public int[][] getTileMap() { return new int[0][0]; }
}
