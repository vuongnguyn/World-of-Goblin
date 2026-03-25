package controller;

import javafx.scene.input.KeyCode;
import model.characters.CharacterObject;
import model.characters.Enemy;
import model.characters.Monster;
import model.characters.Player;
import model.characters.Robber;
import model.characters.RobotBoss;
import model.characters.Terrorist;
import model.skills.Fireball;
import model.skills.Skill;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GameManager {
    public static final int TILE_SIZE = 32;
    public static final int MAP_COLS = 30;
    public static final int MAP_ROWS = 20;

    private Player player;
    private GameState gameState;
    private List<Enemy> enemies;
    private List<Skill> activeSkills;
    private int[][] tileMap;
    private boolean bossSpawned = false;
    private RobotBoss boss;

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    // Skill cooldown (frames)
    private int skillCooldown = 0;
    private static final int SKILL_COOLDOWN_FRAMES = 30;
    private static final int SKILL_MANA_COST = 20;

    public enum GameState {
        MENU,
        PLAYING,
        PAUSED,
        GAME_OVER,
        WIN
    }

    public void initializeGame() {
        player = new Player(96, 96, 40, 40, "" /* TODO: "assets/images/player.png" */, 3.0, 150, 20);
        enemies = new ArrayList<>();
        activeSkills = new ArrayList<>();
        tileMap = loadMap("assets/map/map_1.txt");
        spawnEnemiesFromMap();
        gameState = GameState.PLAYING;
    }

    private int[][] loadMap(String relativePath) {
        int[][] map = new int[MAP_ROWS][MAP_COLS];
        try {
            // Try 1: relative to this class file (controller/../assets/map/map_1.txt)
            java.net.URL url = getClass().getResource("../" + relativePath);
            // Try 2: from classpath root (for IDEs that put assets at root)
            if (url == null) url = getClass().getClassLoader().getResource(relativePath);
            if (url == null) {
                System.err.println("[GameManager] Could not find map resource: " + relativePath);
                return map;
            }
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(url.openStream()));
            int row = 0;
            String line;
            while ((line = reader.readLine()) != null && row < MAP_ROWS) {
                for (int col = 0; col < Math.min(line.length(), MAP_COLS); col++) {
                    map[row][col] = Character.getNumericValue(line.charAt(col));
                }
                row++;
            }
            reader.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void spawnEnemiesFromMap() {
        for (int row = 0; row < MAP_ROWS; row++) {
            for (int col = 0; col < MAP_COLS; col++) {
                double ex = col * TILE_SIZE;
                double ey = row * TILE_SIZE;
                switch (tileMap[row][col]) {
                    case 2 -> enemies.add(new Robber(ex, ey));
                    case 3 -> enemies.add(new Monster(ex, ey));
                    case 4 -> enemies.add(new Terrorist(ex, ey));
                }
            }
        }
    }

    public void handleInput(KeyCode keyCode, boolean isPressed) {
        if (isPressed) {
            pressedKeys.add(keyCode);
        } else {
            pressedKeys.remove(keyCode);
        }
    }

    public void update() {
        if (gameState != GameState.PLAYING) return;

        // ------ Player movement ------
        double pdx = 0, pdy = 0;
        if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT)) pdx = -1;
        if (pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT)) pdx = 1;
        if (pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP)) pdy = -1;
        if (pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN)) pdy = 1;
        player.setDx(pdx);
        player.setDy(pdy);
        player.update();

        // ------ Skill cast ------
        if (skillCooldown > 0) skillCooldown--;
        if (pressedKeys.contains(KeyCode.J) && skillCooldown == 0 && player.useMana(SKILL_MANA_COST)) {
            fireballToward(findNearestEnemy());
            skillCooldown = SKILL_COOLDOWN_FRAMES;
        }

        // ------ Update & move enemies ------
        for (Enemy e : enemies) {
            e.chase(player);
            e.update();
            // Melee damage to player
            if (intersects(player, e)) {
                player.takeDamage(e.getDamage() / 60); // per-frame approximation
            }
        }

        // ------ Update skills & collision ------
        Iterator<Skill> it = activeSkills.iterator();
        while (it.hasNext()) {
            Skill s = it.next();
            s.update();
            if (!s.isActive()) { it.remove(); continue; }
            // Out of screen
            if (s.getX() < 0 || s.getX() > MAP_COLS * TILE_SIZE || s.getY() < 0 || s.getY() > MAP_ROWS * TILE_SIZE) {
                s.deactivate(); it.remove(); continue;
            }
            // Hit enemies
            boolean hit = false;
            for (Enemy e : enemies) {
                if (s.checkCollision(e)) {
                    e.takeDamage(s.getDamage());
                    s.deactivate();
                    hit = true;
                    break;
                }
            }
            if (hit) { it.remove(); continue; }
            // Hit boss
            if (boss != null && s.checkCollision(boss)) {
                boss.takeDamage(s.getDamage());
                s.deactivate();
                it.remove();
            }
        }

        // ------ Remove dead enemies & give exp ------
        Iterator<Enemy> ei = enemies.iterator();
        while (ei.hasNext()) {
            Enemy e = ei.next();
            if (!e.isAlive()) {
                player.gainExp(30);
                ei.remove();
            }
        }

        // ------ Spawn boss when all enemies dead and not spawned ------
        if (enemies.isEmpty() && !bossSpawned) {
            boss = new RobotBoss(MAP_COLS * TILE_SIZE / 2.0, MAP_ROWS * TILE_SIZE / 2.0);
            enemies.add(boss);
            bossSpawned = true;
        }

        // ------ Check win / lose ------
        if (!player.isAlive()) {
            gameState = GameState.GAME_OVER;
        }
        if (bossSpawned && !boss.isAlive()) {
            gameState = GameState.WIN;
        }

        // ------ Mana regen ------
        player.restoreMana(1);
    }

    private void fireballToward(Enemy target) {
        double dx = 1, dy = 0;
        if (target != null) {
            double diffX = target.getX() - player.getX();
            double diffY = target.getY() - player.getY();
            double dist = Math.sqrt(diffX * diffX + diffY * diffY);
            if (dist > 0) { dx = diffX / dist; dy = diffY / dist; }
        }
        activeSkills.add(new Fireball(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2, dx, dy, player));
    }

    private Enemy findNearestEnemy() {
        Enemy nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            double dist = Math.hypot(e.getX() - player.getX(), e.getY() - player.getY());
            if (dist < minDist) { minDist = dist; nearest = e; }
        }
        return nearest;
    }

    private boolean intersects(CharacterObject a, CharacterObject b) {
        return a.getX() < b.getX() + b.getWidth() &&
               a.getX() + a.getWidth() > b.getX() &&
               a.getY() < b.getY() + b.getHeight() &&
               a.getY() + a.getHeight() > b.getY();
    }

    // ---- Getters for view ----
    public Player getPlayer() { return player; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Skill> getActiveSkills() { return activeSkills; }
    public int[][] getTileMap() { return tileMap; }
    public GameState getGameState() { return gameState; }
    public int getSkillCooldown() { return skillCooldown; }
    public int getSkillCooldownMax() { return SKILL_COOLDOWN_FRAMES; }
    public void setGameState(GameState state) { this.gameState = state; }
}
