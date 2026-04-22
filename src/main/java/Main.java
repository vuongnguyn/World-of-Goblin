
import controller.GameManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import view.GameScene;
import view.MenuScene;

public class Main extends Application {

    private Stage primaryStage;
    private GameManager gameManager;
    private GameScene gameScene;
    private boolean hasSave = false;

    // show menu scene in the game window.
    public void showMenu() {
        primaryStage.setResizable(false);
        MenuScene menu = new MenuScene();
        Scene scene = hasSave
                ? menu.menuSceneWithContinue()
                : menu.menuSceneWithoutContinue();

        menu.setOnNewGame(this::startNewGame);
        menu.setOnContinue(this::resumeGame);
        menu.setOnExit(() -> primaryStage.close());

        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(628); // 600 + ~ title bar
    }

    private void startNewGame() {
        if (gameScene != null) gameScene.stopGameLoop();

        gameManager = new GameManager();
        gameManager.initializeGame();
        gameScene  = new GameScene(gameManager);

        // buildScene() returns the scene WITHOUT any key listeners —
        // we attach them all here so there is exactly one handler.
        Scene scene = gameScene.buildScene();
        attachKeyHandlers(scene);

        // Resize stage to fit the game canvas (960 x 640)
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.setWidth(GameManager.MAP_COLS * GameManager.TILE_SIZE + 16);
        primaryStage.setHeight(GameManager.MAP_ROWS * GameManager.TILE_SIZE + 39);
        primaryStage.setResizable(false);
    }

    private void resumeGame() {
        if (gameManager == null) { startNewGame(); return; }

        gameScene = new GameScene(gameManager);
        Scene scene = gameScene.buildScene();
        attachKeyHandlers(scene);

        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.setWidth(GameManager.MAP_COLS * GameManager.TILE_SIZE + 16);
        primaryStage.setHeight(GameManager.MAP_ROWS * GameManager.TILE_SIZE + 39);
        primaryStage.setResizable(false);
    }

    /** Attaches keyboard input for game controls + ESC to go back to menu. */
    private void attachKeyHandlers(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                gameScene.stopGameLoop();
                hasSave = true;
                showMenu();
            } else {
                gameManager.handleInput(e.getCode(), true);
            }
        });
        scene.setOnKeyReleased(e -> gameManager.handleInput(e.getCode(), false));
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("World of Goblin");
        showMenu();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}