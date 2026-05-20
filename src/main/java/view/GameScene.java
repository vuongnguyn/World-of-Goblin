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
import model.environment.ResourceNode;
import model.environment.Tree;
import model.environment.Rock;
import model.environment.BerryBush;
import model.buildings.Building;
import model.buildings.Campfire;
import model.buildings.WoodenWall;
import model.items.Item;

public class GameScene {

    private final GameManager gameManager;
    private Scene scene;
    private Canvas canvas;
    private AnimationTimer gameLoop;
    private long frameCount = 0;

    private static final int TILE_SIZE = GameManager.TILE_SIZE;
    private static final int VIEW_WIDTH = 960;
    private static final int VIEW_HEIGHT = 640;

    public GameScene(GameManager gm) {
        this.gameManager = gm;
    }

    public Scene buildScene() {
        canvas = new Canvas(VIEW_WIDTH, VIEW_HEIGHT);
        Pane root = new Pane(canvas);
        root.setStyle("-fx-background-color: #2d5a27;");
        scene = new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    gameManager.update();
                    frameCount++;
                    render(canvas.getGraphicsContext2D());
                } catch (Throwable t) {
                    stop();
                    t.printStackTrace();
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.setFill(Color.BLACK);
                    gc.fillRect(0, 0, VIEW_WIDTH, VIEW_HEIGHT);
                    gc.setFill(Color.RED);
                    gc.fillText("GAME CRASHED: " + t.toString(), 20, 30);
                }
            }
        };
        gameLoop.start();
        return scene;
    }

    private void render(GraphicsContext gc) {
        Player p = gameManager.getPlayer();
        
        // Camera logic (center on player)
        double camX = p.getX() + p.getWidth()/2 - VIEW_WIDTH/2;
        double camY = p.getY() + p.getHeight()/2 - VIEW_HEIGHT/2;
        
        // Clamp camera to map bounds
        camX = Math.max(0, Math.min(camX, GameManager.MAP_COLS * TILE_SIZE - VIEW_WIDTH));
        camY = Math.max(0, Math.min(camY, GameManager.MAP_ROWS * TILE_SIZE - VIEW_HEIGHT));

        gc.clearRect(0, 0, VIEW_WIDTH, VIEW_HEIGHT);
        
        gc.save();
        gc.translate(-camX, -camY);

        drawGrassFloor(gc, camX, camY);
        drawResources(gc);
        drawBuildings(gc);
        drawEnemies(gc);
        drawPlayer(gc);
        
        // Draw night overlay
        if (gameManager.isNight()) {
            drawNightOverlay(gc, p, camX, camY);
        }

        gc.restore();

        drawHUD(gc);

        GameState state = gameManager.getGameState();
        if (state == GameState.GAME_OVER) drawEndOverlay(gc);
    }

    private void drawGrassFloor(GraphicsContext gc, double camX, double camY) {
        gc.setFill(Color.web("#2d5a27"));
        gc.fillRect(camX, camY, VIEW_WIDTH, VIEW_HEIGHT);
        
        // Draw grid lines
        gc.setStroke(Color.color(0, 0, 0, 0.1));
        gc.setLineWidth(1);
        int startCol = (int) (camX / TILE_SIZE);
        int startRow = (int) (camY / TILE_SIZE);
        
        for (int i = startCol; i < startCol + VIEW_WIDTH / TILE_SIZE + 2; i++) {
            gc.strokeLine(i * TILE_SIZE, camY, i * TILE_SIZE, camY + VIEW_HEIGHT);
        }
        for (int j = startRow; j < startRow + VIEW_HEIGHT / TILE_SIZE + 2; j++) {
            gc.strokeLine(camX, j * TILE_SIZE, camX + VIEW_WIDTH, j * TILE_SIZE);
        }
    }

    private void drawResources(GraphicsContext gc) {
        for (ResourceNode r : gameManager.getResources()) {
            if (r.isDepleted() && !(r instanceof BerryBush)) continue;
            
            double x = r.getX(), y = r.getY();
            // Shadow
            gc.setFill(Color.color(0,0,0,0.3));
            gc.fillOval(x + 5, y + r.getHeight() - 8, r.getWidth() - 10, 8);
            
            if (r instanceof Tree) {
                // Trunk
                gc.setFill(Color.web("#5d4037"));
                gc.fillRect(x + 15, y + 20, 10, 40);
                // Leaves
                gc.setFill(Color.web("#1e8449"));
                gc.fillOval(x, y, 40, 30);
                gc.setFill(Color.web("#27ae60"));
                gc.fillOval(x + 5, y + 5, 30, 20);
            } else if (r instanceof Rock) {
                gc.setFill(Color.web("#7f8c8d"));
                gc.fillOval(x, y + 10, 35, 20);
                gc.setFill(Color.web("#95a5a6"));
                gc.fillOval(x + 5, y + 12, 25, 10);
            } else if (r instanceof BerryBush) {
                gc.setFill(Color.web("#145a32"));
                gc.fillOval(x, y + 10, 30, 20);
                if (!r.isDepleted()) {
                    gc.setFill(Color.web("#e74c3c"));
                    gc.fillOval(x + 5, y + 15, 6, 6);
                    gc.fillOval(x + 18, y + 12, 6, 6);
                    gc.fillOval(x + 12, y + 22, 6, 6);
                }
            }
        }
    }

    private void drawBuildings(GraphicsContext gc) {
        for (Building b : gameManager.getBuildings()) {
            double x = b.getX(), y = b.getY();
            if (b instanceof Campfire) {
                gc.setFill(Color.web("#5d4037"));
                gc.fillRect(x + 5, y + 20, 20, 5);
                gc.fillRect(x + 12, y + 15, 5, 15);
                if (((Campfire) b).isLit()) {
                    gc.setFill(Color.ORANGE);
                    gc.fillOval(x + 8, y + 5, 14, 18);
                    gc.setFill(Color.YELLOW);
                    gc.fillOval(x + 11, y + 10, 8, 10);
                }
            } else if (b instanceof WoodenWall) {
                gc.setFill(Color.web("#8d6e63"));
                gc.fillRect(x, y, 40, 40);
                gc.setStroke(Color.web("#5d4037"));
                gc.strokeRect(x, y, 40, 40);
                gc.strokeLine(x, y + 10, x + 40, y + 10);
                gc.strokeLine(x, y + 20, x + 40, y + 20);
                gc.strokeLine(x, y + 30, x + 40, y + 30);
            }
        }
    }

    private void drawEnemies(GraphicsContext gc) {
        for (Enemy e : gameManager.getEnemies()) {
            double ex = e.getX(), ey = e.getY(), ew = e.getWidth(), eh = e.getHeight();
            gc.setFill(Color.web("#c0392b"));
            gc.fillRect(ex, ey, ew, eh);
            
            // Eyes
            gc.setFill(Color.YELLOW);
            gc.fillOval(ex + 8, ey + 10, 6, 6);
            gc.fillOval(ex + ew - 14, ey + 10, 6, 6);
        }
    }

    private void drawPlayer(GraphicsContext gc) {
        Player p = gameManager.getPlayer();
        double px = p.getX(), py = p.getY(), pw = p.getWidth(), ph = p.getHeight();

        // Shadow
        gc.setFill(Color.color(0, 0, 0, 0.3));
        gc.fillOval(px + 4, py + ph - 4, pw - 8, 8);

        // Body
        gc.setFill(Color.web("#27ae60"));
        gc.fillOval(px, py, pw, ph);

        // Eyes
        gc.setFill(Color.WHITE);
        gc.fillOval(px + 8, py + 10, 8, 8);
        gc.fillOval(px + 24, py + 10, 8, 8);
        gc.setFill(Color.BLACK);
        gc.fillOval(px + 10, py + 12, 4, 4);
        gc.fillOval(px + 26, py + 12, 4, 4);
        
        // Attack Animation
        if (gameManager.getSelectedSlot() != 0) { // Holding something or swinging
            gc.setFill(Color.web("#5d4037"));
            gc.fillRect(px + pw, py + 10, 20, 5); // Stick
        }
    }

    private void drawNightOverlay(GraphicsContext gc, Player p, double camX, double camY) {
        // We draw a huge black rectangle, but we cut out holes for lights using composite
        // However, standard GraphicsContext makes hole-punching hard.
        // A simpler way: fill screen with 90% black, then draw radial gradients over lights 
        // blending from transparent in middle to 90% black on edge to "brighten" it... wait,
        // JavaFX Canvas SRC_OVER won't lighten it if we draw black first.
        
        // We will just draw the dark overlay, then for lights we draw a yellowish radial glow.
        gc.setFill(Color.color(0, 0, 0, 0.85));
        gc.fillRect(camX, camY, VIEW_WIDTH, VIEW_HEIGHT);
        
        gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.ADD);
        
        // Player light
        RadialGradient playerLight = new RadialGradient(0, 0, p.getX()+p.getWidth()/2, p.getY()+p.getHeight()/2, 120, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(1, 0.9, 0.7, 0.5)),
                new Stop(1, Color.color(0, 0, 0, 0)));
        gc.setFill(playerLight);
        gc.fillOval(p.getX() - 100, p.getY() - 100, 240, 240);
        
        // Campfire lights
        for (Building b : gameManager.getBuildings()) {
            if (b instanceof Campfire && ((Campfire)b).isLit()) {
                RadialGradient fireLight = new RadialGradient(0, 0, b.getX()+15, b.getY()+15, 200, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.color(1, 0.6, 0.2, 0.7)),
                        new Stop(1, Color.color(0, 0, 0, 0)));
                gc.setFill(fireLight);
                gc.fillOval(b.getX() - 185, b.getY() - 185, 400, 400);
            }
        }
        
        gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.SRC_OVER);
    }

    private void drawHUD(GraphicsContext gc) {
        Player p = gameManager.getPlayer();

        // Top Left: Stats
        gc.setFill(Color.color(0, 0, 0, 0.6));
        gc.fillRoundRect(10, 10, 200, 60, 10, 10);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("HP", 20, 28);
        drawBar(gc, 50, 18, 140, 10, (double)p.getHealth() / p.getMaxHealth(), Color.RED);
        
        gc.fillText("Food", 15, 48);
        drawBar(gc, 50, 38, 140, 10, p.getHunger() / p.getMaxHunger(), Color.ORANGE);
        
        // Top Right: Day Progress
        gc.setFill(Color.color(0, 0, 0, 0.6));
        gc.fillRoundRect(VIEW_WIDTH - 160, 10, 140, 40, 10, 10);
        gc.setFill(Color.WHITE);
        gc.fillText(gameManager.isNight() ? "NIGHT TIME" : "DAY TIME", VIEW_WIDTH - 120, 25);
        drawBar(gc, VIEW_WIDTH - 150, 35, 120, 5, gameManager.getDayProgress(), Color.YELLOW);

        // Bottom Center: Hotbar
        int hotbarWidth = 4 * 60;
        int startX = VIEW_WIDTH / 2 - hotbarWidth / 2;
        int startY = VIEW_HEIGHT - 70;
        
        gc.setFill(Color.color(0, 0, 0, 0.6));
        gc.fillRoundRect(startX - 10, startY - 10, hotbarWidth + 20, 70, 10, 10);
        
        drawHotbarSlot(gc, startX, startY, 1, "Campfire", "5W 2S", gameManager.getSelectedSlot() == 1);
        drawHotbarSlot(gc, startX + 60, startY, 2, "Wall", "2W", gameManager.getSelectedSlot() == 2);
        drawHotbarSlot(gc, startX + 120, startY, 3, "Berry", "Eat", gameManager.getSelectedSlot() == 3);
        drawHotbarSlot(gc, startX + 180, startY, 4, "Hand", "", gameManager.getSelectedSlot() == 0);
        
        // Inventory count
        gc.setFill(Color.WHITE);
        gc.fillText("Wood: " + p.getInventory().getOrDefault(Item.WOOD, 0), 10, VIEW_HEIGHT - 30);
        gc.fillText("Stone: " + p.getInventory().getOrDefault(Item.STONE, 0), 10, VIEW_HEIGHT - 15);
        gc.fillText("Berry: " + p.getInventory().getOrDefault(Item.BERRY, 0), 100, VIEW_HEIGHT - 30);
    }

    private void drawHotbarSlot(GraphicsContext gc, int x, int y, int key, String name, String cost, boolean selected) {
        gc.setFill(Color.color(0.2, 0.2, 0.2, 0.8));
        if (selected) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(3);
        } else {
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
        }
        gc.fillRect(x, y, 50, 50);
        gc.strokeRect(x, y, 50, 50);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 10));
        gc.fillText("[" + key + "]", x + 5, y + 15);
        gc.fillText(name, x + 5, y + 30);
        
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", 9));
        gc.fillText(cost, x + 5, y + 45);
    }

    private void drawBar(GraphicsContext gc, double x, double y, double w, double h, double ratio, Color color) {
        ratio = Math.max(0, Math.min(1, ratio));
        gc.setFill(Color.color(0.2, 0.2, 0.2));
        gc.fillRect(x, y, w, h);
        gc.setFill(color);
        gc.fillRect(x, y, w * ratio, h);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, w, h);
    }

    private void drawEndOverlay(GraphicsContext gc) {
        gc.setFill(Color.color(0, 0, 0, 0.8));
        gc.fillRect(0, 0, VIEW_WIDTH, VIEW_HEIGHT);
        gc.setFill(Color.RED);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        gc.fillText("YOU DIED", VIEW_WIDTH / 2.0 - 150, VIEW_HEIGHT / 2.0);
        gc.setFont(Font.font("Arial", 20));
        gc.setFill(Color.WHITE);
        gc.fillText("Press ESC to return to Menu", VIEW_WIDTH / 2.0 - 130, VIEW_HEIGHT / 2.0 + 50);
    }

    public void stopGameLoop() {
        if (gameLoop != null) gameLoop.stop();
    }
}
