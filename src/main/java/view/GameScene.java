package view;

import controller.GameManager;
import controller.GameManager.GameState;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import model.characters.Enemy;
import model.characters.Player;
import model.skills.Skill;

public class GameScene {

    private final GameManager gameManager;
    private Scene scene;
    private Canvas canvas;
    private AnimationTimer gameLoop;
    private long frameCount = 0;

    private static final int TILE_SIZE = GameManager.TILE_SIZE;
    private static final int WIDTH  = GameManager.MAP_COLS * TILE_SIZE;
    private static final int HEIGHT = GameManager.MAP_ROWS * TILE_SIZE;

    public GameScene(GameManager gm) {
        this.gameManager = gm;
    }

    public Scene buildScene() {
        canvas = new Canvas(WIDTH, HEIGHT);
        Pane root = new Pane(canvas);
        // Prevent white background showing through transparent canvas areas
        root.setStyle("-fx-background-color: #1a1a2e;");
        scene = new Scene(root, WIDTH, HEIGHT);
        // NOTE: key handlers are attached by Main.java — do NOT set them here

        // Game loop ~60 fps
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    gameManager.update();
                    frameCount++;
                    render(canvas.getGraphicsContext2D());
                } catch (Throwable t) {
                    stop(); // Prevent spamming console
                    t.printStackTrace();
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.setFill(Color.BLACK);
                    gc.fillRect(0, 0, WIDTH, HEIGHT);
                    gc.setFill(Color.RED);
                    gc.setFont(Font.font("Menlo", 14));
                    gc.fillText("GAME CRASHED:\n" + t.toString(), 20, 30);
                    int y = 60;
                    for (int i = 0; i < Math.min(15, t.getStackTrace().length); i++) {
                        gc.fillText(t.getStackTrace()[i].toString(), 20, y);
                        y += 20;
                    }
                }
            }
        };
        gameLoop.start();
        return scene;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  MAIN RENDER
    // ═══════════════════════════════════════════════════════════════════════
    private void render(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        drawBackground(gc);
        drawMap(gc);
        drawSkills(gc);
        drawEnemies(gc);
        drawPlayer(gc);
        drawHUD(gc);

        GameState state = gameManager.getGameState();
        if (state == GameState.GAME_OVER) drawEndOverlay(gc, false);
        if (state == GameState.WIN)       drawEndOverlay(gc, true);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  BACKGROUND
    // ═══════════════════════════════════════════════════════════════════════
    private void drawBackground(GraphicsContext gc) {
        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  MAP / TILES
    // ═══════════════════════════════════════════════════════════════════════
    private void drawMap(GraphicsContext gc) {
        int[][] map = gameManager.getTileMap();
        for (int row = 0; row < GameManager.MAP_ROWS; row++) {
            for (int col = 0; col < GameManager.MAP_COLS; col++) {
                double x = col * TILE_SIZE, y = row * TILE_SIZE;
                int tile = map[row][col];
                switch (tile) {
                    case 1 -> drawWallTile(gc, x, y);
                    case 2, 3, 4 -> drawFloorTile(gc, x, y); // enemies spawn on floor
                    default -> drawFloorTile(gc, x, y);
                }
            }
        }
    }

    private void drawFloorTile(GraphicsContext gc, double x, double y) {
        // Base grass/ground colour
        gc.setFill(Color.web("#2d5a27"));
        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        // Subtle checker darkening
        if ((((int) x / TILE_SIZE) + ((int) y / TILE_SIZE)) % 2 == 0) {
            gc.setFill(Color.color(0, 0, 0, 0.06));
            gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        }
        
        // Procedural decorations (seeded by coordinates)
        int seed = (int)(x * 73 + y * 31);
        if (seed % 7 == 0) {
            // Draws a small bush
            gc.setFill(Color.web("#145a32"));
            gc.fillOval(x + 4, y + 8, 16, 12);
            gc.fillOval(x + 12, y + 10, 14, 10);
            gc.setFill(Color.web("#1e8449"));
            gc.fillOval(x + 6, y + 4, 18, 14);
        } else if (seed % 19 == 0) {
            // Draws a tree stump
            gc.setFill(Color.web("#5d4037"));
            gc.fillOval(x + 6, y + 12, 16, 10);
            gc.setFill(Color.web("#795548"));
            gc.fillOval(x + 6, y + 8, 16, 10);
            gc.setStroke(Color.web("#3e2723"));
            gc.setLineWidth(1);
            gc.strokeArc(x + 8, y + 10, 12, 6, 0, 180, javafx.scene.shape.ArcType.OPEN);
        } else if (seed % 5 == 0) {
            // Little rocks
            gc.setFill(Color.web("#7f8c8d"));
            gc.fillOval(x + 18, y + 20, 6, 4);
            gc.fillOval(x + 22, y + 18, 4, 3);
        } else {
            // Standard Dirt/texture dots
            gc.setFill(Color.color(0.1, 0.35, 0.08, 0.4));
            gc.fillOval(x + 6, y + 6, 4, 3);
            gc.fillOval(x + 18, y + 20, 3, 2);
            gc.fillOval(x + 24, y + 10, 3, 3);
        }
        
        // Thin border
        gc.setStroke(Color.color(0, 0, 0, 0.12));
        gc.setLineWidth(0.5);
        gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    private void drawWallTile(GraphicsContext gc, double x, double y) {
        // Stone wall base
        gc.setFill(Color.web("#3a3a4a"));
        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        // Brick highlight (top-left lit edge)
        gc.setFill(Color.color(1, 1, 1, 0.08));
        gc.fillRect(x, y, TILE_SIZE, 3);
        gc.fillRect(x, y, 3, TILE_SIZE);
        // Brick shadow (bottom-right)
        gc.setFill(Color.color(0, 0, 0, 0.3));
        gc.fillRect(x, y + TILE_SIZE - 3, TILE_SIZE, 3);
        gc.fillRect(x + TILE_SIZE - 3, y, 3, TILE_SIZE);
        // Mortar lines
        gc.setStroke(Color.web("#23232f"));
        gc.setLineWidth(1.0);
        gc.strokeRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);
        // Inner stone crack detail
        gc.setStroke(Color.color(1, 1, 1, 0.05));
        gc.setLineWidth(0.5);
        gc.strokeLine(x + 8, y + 5, x + 14, y + 11);
        gc.strokeLine(x + 20, y + 18, x + 25, y + 22);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PLAYER (GOBLIN)
    // ═══════════════════════════════════════════════════════════════════════
    private void drawPlayer(GraphicsContext gc) {
        Player p = gameManager.getPlayer();
        double px = p.getX(), py = p.getY();
        double pw = p.getWidth(), ph = p.getHeight();

        // Bounce animation (bob up/down)
        double bob = Math.sin(frameCount * 0.18) * 2.0;
        py += bob;

        // Shadow beneath goblin
        gc.setFill(Color.color(0, 0, 0, 0.30));
        gc.fillOval(px + 4, py + ph - 4, pw - 8, 8);

        // ── Body ──
        gc.setFill(Color.web("#27ae60"));
        gc.fillOval(px + 4, py + 12, pw - 8, ph - 10);

        // ── Head ──
        gc.setFill(Color.web("#2ecc71"));
        gc.fillOval(px + 5, py + 2, pw - 10, ph * 0.55);

        // ── Pointy ears ──
        gc.setFill(Color.web("#27ae60"));
        // Left ear
        double[] exL = {px + 5, px - 4, px + 10};
        double[] eyL = {py + 10, py + 3, py + 3};
        gc.fillPolygon(exL, eyL, 3);
        // Right ear
        double[] exR = {px + pw - 5, px + pw + 4, px + pw - 10};
        double[] eyR = {py + 10, py + 3, py + 3};
        gc.fillPolygon(exR, eyR, 3);
        // Ear inner highlight
        gc.setFill(Color.web("#1a8a44"));
        gc.fillOval(px - 2, py + 5, 6, 4);
        gc.fillOval(px + pw - 4, py + 5, 6, 4);

        // ── Eyes ──
        gc.setFill(Color.web("#ffff00")); // Glowing yellow scler
        gc.fillOval(px + 9, py + 10, 9, 9);
        gc.fillOval(px + pw - 18, py + 10, 9, 9);
        gc.setFill(Color.web("#cc0000")); // Red pupils
        gc.fillOval(px + 11, py + 12, 5, 5);
        gc.fillOval(px + pw - 16, py + 12, 5, 5);
        gc.setFill(Color.WHITE); // Shine
        gc.fillOval(px + 13, py + 12, 2, 2);
        gc.fillOval(px + pw - 14, py + 12, 2, 2);

        // ── Nose ──
        gc.setFill(Color.web("#1e8449"));
        gc.fillOval(px + pw / 2 - 3, py + 19, 6, 4);

        // ── Mouth (grin) ──
        gc.setStroke(Color.web("#145a32"));
        gc.setLineWidth(1.5);
        gc.strokeArc(px + 9, py + 22, pw - 18, 8, 200, 140, javafx.scene.shape.ArcType.OPEN);
        // Fang teeth
        gc.setFill(Color.WHITE);
        gc.fillRect(px + pw / 2 - 5, py + 26, 3, 5);
        gc.fillRect(px + pw / 2 + 2, py + 26, 3, 5);

        // ── Outline ──
        gc.setStroke(Color.web("#145a32"));
        gc.setLineWidth(2.0);
        gc.strokeOval(px + 5, py + 2, pw - 10, ph * 0.55);

        // ── Staff / weapon hint ──
        gc.setStroke(Color.web("#6d4c41"));
        gc.setLineWidth(3);
        gc.strokeLine(px + pw - 4, py + ph - 5, px + pw + 8, py - 4);
        gc.setFill(Color.web("#9b59b6"));
        gc.fillOval(px + pw + 5, py - 9, 8, 8);
        gc.setFill(Color.color(0.6, 0.3, 0.9, 0.5));
        gc.fillOval(px + pw + 2, py - 12, 14, 14);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ENEMIES
    // ═══════════════════════════════════════════════════════════════════════
    private void drawEnemies(GraphicsContext gc) {
        for (Enemy e : gameManager.getEnemies()) {
            String type = e.getClass().getSimpleName();
            double ex = e.getX(), ey = e.getY(), ew = e.getWidth(), eh = e.getHeight();

            switch (type) {
                case "Robber"    -> drawRobber(gc, ex, ey, ew, eh);
                case "Terrorist" -> drawTerrorist(gc, ex, ey, ew, eh);
                case "Monster"   -> drawMonster(gc, ex, ey, ew, eh);
                case "RobotBoss" -> drawRobotBoss(gc, ex, ey, ew, eh);
                default          -> drawGenericEnemy(gc, ex, ey, ew, eh);
            }

            // Health bar above all enemies
            int maxHp = switch (type) {
                case "Robber" -> 50; case "Terrorist" -> 60;
                case "Monster" -> 100; case "RobotBoss" -> 500;
                default -> 50;
            };
            drawEnemyHealthBar(gc, ex, ey - 8, ew, 5, (double) e.getHealth() / maxHp);
        }
    }

    /** Red-jacketed robber with cap */
    private void drawRobber(GraphicsContext gc, double x, double y, double w, double h) {
        // Shadow
        gc.setFill(Color.color(0, 0, 0, 0.25));
        gc.fillOval(x + 4, y + h - 4, w - 8, 7);
        // Body (red jacket)
        gc.setFill(Color.web("#c0392b"));
        gc.fillRoundRect(x + 4, y + h * 0.45, w - 8, h * 0.55, 6, 6);
        // Jacket lapels
        gc.setFill(Color.web("#922b21"));
        gc.fillPolygon(new double[]{x+w/2, x+5, x+w/2-4}, new double[]{y+h*0.5, y+h*0.45, y+h*0.75}, 3);
        gc.fillPolygon(new double[]{x+w/2, x+w-5, x+w/2+4}, new double[]{y+h*0.5, y+h*0.45, y+h*0.75}, 3);
        // Head
        gc.setFill(Color.web("#f0c080"));
        gc.fillOval(x + 6, y + 3, w - 12, h * 0.50);
        // Cap (dark blue)
        gc.setFill(Color.web("#1a252f"));
        gc.fillArc(x + 4, y - 2, w - 8, h * 0.32, 0, 180, javafx.scene.shape.ArcType.CHORD);
        gc.fillRect(x + 2, y + 6, w - 4, 5);
        // Eyes (angry)
        gc.setFill(Color.BLACK);
        gc.fillOval(x + 10, y + 13, 5, 5);
        gc.fillOval(x + w - 15, y + 13, 5, 5);
        gc.setFill(Color.WHITE);
        gc.fillOval(x + 11, y + 13, 2, 2);
        gc.fillOval(x + w - 14, y + 13, 2, 2);
        // Knife
        gc.setStroke(Color.web("#aab7b8"));
        gc.setLineWidth(2);
        gc.strokeLine(x + w - 3, y + h * 0.6, x + w + 6, y + h * 0.4);
        gc.setStroke(Color.web("#784212"));
        gc.setLineWidth(3);
        gc.strokeLine(x + w - 3, y + h * 0.65, x + w - 1, y + h * 0.85);
        // Outline
        gc.setStroke(Color.web("#7b241c"));
        gc.setLineWidth(1.5);
        gc.strokeOval(x + 6, y + 3, w - 12, h * 0.50);
    }

    /** Purple-hooded terrorist with bomb */
    private void drawTerrorist(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(Color.color(0, 0, 0, 0.25));
        gc.fillOval(x + 4, y + h - 4, w - 8, 7);
        // Body (dark robe)
        gc.setFill(Color.web("#4a235a"));
        gc.fillRoundRect(x + 3, y + h * 0.42, w - 6, h * 0.58, 8, 8);
        // Hood
        gc.setFill(Color.web("#6c3483"));
        gc.fillArc(x + 2, y, w - 4, h * 0.55, 0, 180, javafx.scene.shape.ArcType.CHORD);
        // Head
        gc.setFill(Color.web("#c39bd3"));
        gc.fillOval(x + 7, y + 6, w - 14, h * 0.42);
        // Glowing eyes (orange menacing)
        gc.setFill(Color.ORANGE);
        gc.fillOval(x + 10, y + 14, 7, 6);
        gc.fillOval(x + w - 17, y + 14, 7, 6);
        gc.setFill(Color.web("#cc0000"));
        gc.fillOval(x + 12, y + 15, 4, 4);
        gc.fillOval(x + w - 15, y + 15, 4, 4);
        // Bomb in hand
        gc.setFill(Color.web("#1c2833"));
        gc.fillOval(x - 5, y + h * 0.5, 12, 12);
        gc.setStroke(Color.web("#e74c3c"));
        gc.setLineWidth(1.5);
        gc.strokeLine(x + 1, y + h * 0.5, x + 1, y + h * 0.35);
        // Fuse spark
        gc.setFill(Color.YELLOW);
        gc.fillOval(x + 0, y + h * 0.33, 4, 4);
        gc.setFill(Color.color(1, 0.6, 0, 0.7));
        gc.fillOval(x - 1, y + h * 0.30, 6, 6);
        // Outline
        gc.setStroke(Color.web("#4a235a"));
        gc.setLineWidth(1.5);
        gc.strokeOval(x + 7, y + 6, w - 14, h * 0.42);
    }

    /** Teal troll-like monster with claws */
    private void drawMonster(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(Color.color(0, 0, 0, 0.3));
        gc.fillOval(x + 3, y + h - 4, w - 6, 9);
        // Stocky body
        gc.setFill(Color.web("#148f77"));
        gc.fillRoundRect(x + 2, y + h * 0.38, w - 4, h * 0.62, 10, 10);
        // Big head
        gc.setFill(Color.web("#1abc9c"));
        gc.fillOval(x + 2, y + 2, w - 4, h * 0.52);
        // Horns
        gc.setFill(Color.web("#0e6655"));
        gc.fillPolygon(new double[]{x+9, x+5, x+14}, new double[]{y+8, y-6, y-5}, 3);
        gc.fillPolygon(new double[]{x+w-9, x+w-5, x+w-14}, new double[]{y+8, y-6, y-5}, 3);
        // Eyes (wide, angry)
        gc.setFill(Color.web("#f8c471"));
        gc.fillOval(x + 8, y + 12, 11, 10);
        gc.fillOval(x + w - 19, y + 12, 11, 10);
        gc.setFill(Color.BLACK);
        gc.fillOval(x + 11, y + 14, 6, 6);
        gc.fillOval(x + w - 16, y + 14, 6, 6);
        gc.setFill(Color.WHITE);
        gc.fillOval(x + 13, y + 15, 2, 2);
        gc.fillOval(x + w - 14, y + 15, 2, 2);
        // Wide gaping mouth
        gc.setFill(Color.web("#0a3d2b"));
        gc.fillArc(x + 8, y + 26, w - 16, 10, 200, 140, javafx.scene.shape.ArcType.CHORD);
        // Fangs
        gc.setFill(Color.WHITE);
        gc.fillRect(x + 14, y + 28, 3, 6);
        gc.fillRect(x + w - 17, y + 28, 3, 6);
        // Claws on sides
        gc.setFill(Color.web("#117a65"));
        gc.fillOval(x - 4, y + h * 0.5, 10, 14);
        gc.fillOval(x + w - 6, y + h * 0.5, 10, 14);
        gc.setFill(Color.web("#d5dbdb"));
        // Left 3 claws
        for (int i = 0; i < 3; i++) {
            gc.fillPolygon(new double[]{x-4+i*3, x-3+i*3, x-1+i*3},
                           new double[]{y+h*0.5, y+h*0.5-7, y+h*0.5}, 3);
        }
        // Outline
        gc.setStroke(Color.web("#0e6655"));
        gc.setLineWidth(1.5);
        gc.strokeOval(x + 2, y + 2, w - 4, h * 0.52);
    }

    /** Steel robot boss with glowing core */
    private void drawRobotBoss(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(Color.color(0, 0, 0, 0.4));
        gc.fillOval(x + 6, y + h - 5, w - 12, 12);

        // Legs
        gc.setFill(Color.web("#566573"));
        gc.fillRoundRect(x + 12, y + h * 0.7, w * 0.22, h * 0.32, 5, 5);
        gc.fillRoundRect(x + w - 12 - w * 0.22, y + h * 0.7, w * 0.22, h * 0.32, 5, 5);
        gc.setFill(Color.web("#808b96"));
        gc.fillRect(x + 12, y + h * 0.88, w * 0.22, h * 0.12);
        gc.fillRect(x + w - 12 - w * 0.22, y + h * 0.88, w * 0.22, h * 0.12);

        // Body chassis
        gc.setFill(Color.web("#2c3e50"));
        gc.fillRoundRect(x + 5, y + h * 0.30, w - 10, h * 0.55, 12, 12);
        // Body highlight edge
        gc.setFill(Color.color(1, 1, 1, 0.12));
        gc.fillRoundRect(x + 6, y + h * 0.31, w - 12, 4, 4, 4);

        // Chest energy core (pulsing glow)
        double pulse = 0.5 + 0.5 * Math.sin(frameCount * 0.12);
        RadialGradient core = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0.2, 0.9, 1.0, 0.95)),
            new Stop(0.5, Color.color(0.0, 0.5, 0.8, 0.7)),
            new Stop(1, Color.color(0.0, 0.2, 0.5, 0.0)));
        gc.setFill(core);
        gc.fillOval(x + w/2 - 10, y + h * 0.47, 20, 20);
        // Outer glow ring
        gc.setFill(Color.color(0.2, 0.8, 1.0, 0.15 + 0.15 * pulse));
        gc.fillOval(x + w/2 - 16, y + h * 0.44, 32, 26);

        // Arms with hydraulics
        gc.setFill(Color.web("#566573"));
        gc.fillRoundRect(x - 8, y + h * 0.32, 16, h * 0.4, 6, 6);
        gc.fillRoundRect(x + w - 8, y + h * 0.32, 16, h * 0.4, 6, 6);
        // Arm joints
        gc.setFill(Color.web("#aab7b8"));
        gc.fillOval(x - 5, y + h * 0.36, 10, 10);
        gc.fillOval(x + w - 5, y + h * 0.36, 10, 10);
        // Arm cannons
        gc.setFill(Color.web("#1c2833"));
        gc.fillRoundRect(x - 10, y + h * 0.63, 14, 8, 3, 3);
        gc.fillRoundRect(x + w - 4, y + h * 0.63, 14, 8, 3, 3);
        // Muzzle flash
        if (frameCount % 40 < 5) {
            gc.setFill(Color.color(1, 0.8, 0, 0.8));
            gc.fillOval(x - 13, y + h * 0.62, 8, 10);
            gc.fillOval(x + w + 5, y + h * 0.62, 8, 10);
        }

        // Head (square visor)
        gc.setFill(Color.web("#34495e"));
        gc.fillRoundRect(x + 10, y + 2, w - 20, h * 0.32, 8, 8);
        gc.setFill(Color.color(1, 1, 1, 0.1));
        gc.fillRoundRect(x + 11, y + 3, w - 22, 5, 3, 3);

        // Visor / eyes (red scanline)
        gc.setFill(Color.color(0.8, 0.0, 0.0, 0.9));
        gc.fillRoundRect(x + 14, y + h * 0.10, w - 28, h * 0.12, 4, 4);
        // Scanline animation
        gc.setFill(Color.color(1.0, 0.3, 0.3, 0.7));
        double scan = ((frameCount % 20) / 20.0) * (w - 28);
        gc.fillRect(x + 14 + scan, y + h * 0.10, 4, h * 0.12);

        // Antenna
        gc.setStroke(Color.web("#aab7b8"));
        gc.setLineWidth(2);
        gc.strokeLine(x + w/2, y + 2, x + w/2, y - 10);
        gc.setFill(Color.web("#e74c3c"));
        gc.fillOval(x + w/2 - 4, y - 14, 8, 8);
        // Antenna blink
        if (frameCount % 30 < 15) {
            gc.setFill(Color.color(1, 0.2, 0.2, 0.7));
            gc.fillOval(x + w/2 - 6, y - 16, 12, 12);
        }

        // Panel rivets on body
        gc.setFill(Color.web("#7f8c8d"));
        int[] rivetX = {8, (int)(w-12), 8, (int)(w-12)};
        int[] rivetY = {(int)(h*0.34), (int)(h*0.34), (int)(h*0.72), (int)(h*0.72)};
        for (int i = 0; i < rivetX.length; i++) {
            gc.fillOval(x + rivetX[i], y + rivetY[i], 5, 5);
        }

        // Outline
        gc.setStroke(Color.web("#1c2833"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x + 5, y + h * 0.30, w - 10, h * 0.55, 12, 12);
        gc.strokeRoundRect(x + 10, y + 2, w - 20, h * 0.32, 8, 8);
    }

    private void drawGenericEnemy(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(Color.GRAY);
        gc.fillRect(x, y, w, h);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SKILLS / FIREBALL
    // ═══════════════════════════════════════════════════════════════════════
    private void drawSkills(GraphicsContext gc) {
        for (Skill s : gameManager.getActiveSkills()) {
            double sx = s.getX(), sy = s.getY(), sw = s.getWidth(), sh = s.getHeight();
            double cx = sx + sw / 2, cy = sy + sh / 2;

            // Outer aura
            gc.setFill(Color.color(1.0, 0.4, 0.0, 0.15));
            gc.fillOval(cx - 14, cy - 14, 28, 28);

            // Mid glow ring
            gc.setFill(Color.color(1.0, 0.6, 0.0, 0.30));
            gc.fillOval(cx - 10, cy - 10, 20, 20);

            // Fire core gradient
            RadialGradient fire = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.WHITE),
                new Stop(0.3, Color.web("#ffcc00")),
                new Stop(0.7, Color.web("#ff6600")),
                new Stop(1.0, Color.color(1, 0.2, 0, 0)));
            gc.setFill(fire);
            gc.fillOval(sx, sy, sw, sh);

            // Trailing sparks
            gc.setFill(Color.color(1.0, 0.7, 0.0, 0.6));
            gc.fillOval(cx - 2, cy - 2, 5, 5);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HUD
    // ═══════════════════════════════════════════════════════════════════════
    private void drawHUD(GraphicsContext gc) {
        Player p = gameManager.getPlayer();

        // ── Left panel ──
        double panX = 12, panY = 12, panW = 220, panH = 90;
        gc.setFill(Color.color(0.05, 0.05, 0.15, 0.75));
        gc.fillRoundRect(panX, panY, panW, panH, 12, 12);
        gc.setStroke(Color.color(0.4, 0.4, 0.9, 0.6));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(panX, panY, panW, panH, 12, 12);

        // ── Goblin mini icon ──
        gc.setFill(Color.web("#27ae60"));
        gc.fillOval(panX + 8, panY + 8, 26, 26);
        gc.setFill(Color.web("#f1c40f"));
        gc.fillOval(panX + 12, panY + 13, 5, 5);
        gc.fillOval(panX + 21, panY + 13, 5, 5);
        gc.setFill(Color.web("#1e8449"));
        gc.fillPolygon(new double[]{panX+10, panX+7, panX+14},
                       new double[]{panY+12, panY+7, panY+8}, 3);
        gc.fillPolygon(new double[]{panX+28, panX+31, panX+24},
                       new double[]{panY+12, panY+7, panY+8}, 3);

        double barsX = panX + 42, barsW = panW - 50;

        // HP label + bar
        gc.setFill(Color.web("#e74c3c"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText("HP", barsX - 2, panY + 19);
        drawStyledBar(gc, barsX + 14, panY + 9, barsW - 14, 12,
                (double) p.getHealth() / p.getMaxHealth(),
                Color.web("#2ecc71"), Color.web("#e74c3c"), Color.web("#7f0000"));
        gc.setFill(Color.color(1, 1, 1, 0.7));
        gc.setFont(Font.font("Arial", 9));
        gc.fillText(p.getHealth() + "/" + p.getMaxHealth(), barsX + 18, panY + 20);

        // MP label + bar
        gc.setFill(Color.web("#3498db"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText("MP", barsX - 2, panY + 39);
        drawStyledBar(gc, barsX + 14, panY + 29, barsW - 14, 12,
                (double) p.getMana() / p.getMaxMana(),
                Color.web("#3498db"), Color.web("#1a5276"), Color.web("#0a1a2e"));
        gc.setFill(Color.color(1, 1, 1, 0.7));
        gc.setFont(Font.font("Arial", 9));
        gc.fillText(p.getMana() + "/" + p.getMaxMana(), barsX + 18, panY + 40);

        // Level badge
        gc.setFill(Color.web("#f39c12"));
        gc.fillRoundRect(panX + 8, panY + 40, 26, 14, 6, 6);
        gc.setFill(Color.web("#1c1c1c"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        gc.fillText("Lv" + p.getLevel(), panX + 11, panY + 51);

        // EXP bar (thin strip at bottom of panel)
        gc.setFill(Color.color(0, 0, 0, 0.4));
        gc.fillRoundRect(panX + 8, panY + 57, panW - 16, 7, 4, 4);
        gc.setFill(Color.web("#f39c12"));
        gc.fillRoundRect(panX + 8, panY + 57, (panW - 16) * p.getExp() / 100.0, 7, 4, 4);
        gc.setFill(Color.color(1, 1, 1, 0.5));
        gc.setFont(Font.font("Arial", 8));
        gc.fillText("EXP  " + p.getExp() + "/100", panX + 10, panY + 64);

        // ── Skill slot bottom-left ──
        drawSkillSlot(gc, 12, HEIGHT - 58);

        // ── Enemy count (top-right) ──
        int eCount = gameManager.getEnemies().size();
        gc.setFill(Color.color(0.05, 0.05, 0.15, 0.70));
        gc.fillRoundRect(WIDTH - 130, 12, 118, 32, 8, 8);
        gc.setStroke(Color.color(0.8, 0.2, 0.2, 0.6));
        gc.setLineWidth(1);
        gc.strokeRoundRect(WIDTH - 130, 12, 118, 32, 8, 8);
        gc.setFill(Color.web("#e74c3c"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText("⚔ Enemies: " + eCount, WIDTH - 122, panY + 22);

        // ── Controls hint ──
        gc.setFill(Color.color(1, 1, 1, 0.38));
        gc.setFont(Font.font("Arial", FontPosture.ITALIC, 9));
        gc.fillText("WASD / ↑↓←→  Move   |   J  Fireball   |   ESC  Menu", 10, HEIGHT - 8);
    }

    private void drawSkillSlot(GraphicsContext gc, double x, double y) {
        int cd = gameManager.getSkillCooldown();
        boolean ready = cd == 0;

        // Slot background
        gc.setFill(Color.color(0.05, 0.05, 0.15, 0.80));
        gc.fillRoundRect(x, y, 46, 46, 8, 8);
        gc.setStroke(ready ? Color.web("#f39c12") : Color.web("#555570"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, 46, 46, 8, 8);

        // Fireball icon
        RadialGradient icon = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.WHITE), new Stop(0.4, Color.ORANGE), new Stop(1, Color.color(1,0.2,0,0)));
        gc.setFill(icon);
        double pulse = ready ? 1.0 : 0.5;
        gc.fillOval(x + 10, y + 6, 26 * pulse, 26 * pulse);

        // Cooldown overlay
        if (!ready) {
            gc.setFill(Color.color(0, 0, 0, 0.55));
            gc.fillRoundRect(x, y, 46, 46, 8, 8);
            gc.setFill(Color.color(1, 0.4, 0.4, 0.9));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(cd / 6 + 1), x + 23, y + 30);
            gc.setTextAlign(TextAlignment.LEFT);
        } else {
            gc.setFill(Color.web("#f39c12"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("READY", x + 23, y + 42);
            gc.setTextAlign(TextAlignment.LEFT);
        }

        // Key binding label
        gc.setFill(Color.color(1, 1, 1, 0.65));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        gc.fillText("[J]", x + 17, y + 14);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  END SCREEN OVERLAY
    // ═══════════════════════════════════════════════════════════════════════
    private void drawEndOverlay(GraphicsContext gc, boolean win) {
        // Dimmed background
        gc.setFill(Color.color(0, 0, 0, 0.72));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Central panel
        double panW = 500, panH = 200;
        double panX = WIDTH / 2.0 - panW / 2, panY = HEIGHT / 2.0 - panH / 2;
        gc.setFill(win ? Color.color(0.05, 0.2, 0.05, 0.95) : Color.color(0.2, 0.02, 0.02, 0.95));
        gc.fillRoundRect(panX, panY, panW, panH, 20, 20);
        gc.setStroke(win ? Color.web("#2ecc71") : Color.web("#e74c3c"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(panX, panY, panW, panH, 20, 20);

        // Title
        gc.setFill(win ? Color.web("#2ecc71") : Color.web("#e74c3c"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 58));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(win ? "VICTORY!" : "GAME OVER", WIDTH / 2.0, panY + 90);

        // Subtitle
        gc.setFill(Color.color(1, 1, 1, 0.75));
        gc.setFont(Font.font("Arial", 18));
        gc.fillText(win ? "The Robot Boss has been defeated!" : "The Goblin has fallen...", WIDTH / 2.0, panY + 130);

        // Return hint
        gc.setFill(Color.color(1, 1, 1, 0.5));
        gc.setFont(Font.font("Arial", FontPosture.ITALIC, 14));
        gc.fillText("Press  ESC  to return to menu", WIDTH / 2.0, panY + 168);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPER BARS
    // ═══════════════════════════════════════════════════════════════════════
    private void drawStyledBar(GraphicsContext gc, double x, double y, double w, double h,
                               double ratio, Color full, Color low, Color bg) {
        ratio = Math.max(0, Math.min(1, ratio));
        gc.setFill(bg);
        gc.fillRoundRect(x, y, w, h, 5, 5);
        Color fill = ratio > 0.5 ? full : ratio > 0.25 ? Color.ORANGE : low;
        gc.setFill(fill);
        gc.fillRoundRect(x, y, w * ratio, h, 5, 5);
        // Shine sheen
        gc.setFill(Color.color(1, 1, 1, 0.15));
        gc.fillRoundRect(x, y, w * ratio, h / 2.5, 5, 5);
        gc.setStroke(Color.color(0, 0, 0, 0.4));
        gc.setLineWidth(0.7);
        gc.strokeRoundRect(x, y, w, h, 5, 5);
    }

    private void drawEnemyHealthBar(GraphicsContext gc, double x, double y, double w, double h, double ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        gc.setFill(Color.color(0, 0, 0, 0.5));
        gc.fillRect(x, y, w, h);
        Color fill = ratio > 0.5 ? Color.web("#2ecc71") : ratio > 0.25 ? Color.ORANGE : Color.RED;
        gc.setFill(fill);
        gc.fillRect(x, y, w * ratio, h);
        gc.setStroke(Color.color(0, 0, 0, 0.6));
        gc.setLineWidth(0.5);
        gc.strokeRect(x, y, w, h);
    }

    public void stopGameLoop() {
        if (gameLoop != null) gameLoop.stop();
    }
}
