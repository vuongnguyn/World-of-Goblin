
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.util.Objects;
import view.MenuScene;

public class Main extends Application {

    private boolean isContinue = false;
    private Stage primaryStage;
    private MenuScene menu = new MenuScene();
    
    public void showMenu() {
        Scene scene;
        if (isContinue) {
            scene = menu.menuSceneWithContinue();
        } else {
            scene = menu.menuSceneWithoutContinue();
        }
        primaryStage.setScene(scene);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("World of Goblin");
        primaryStage.setResizable(false);
        showMenu();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}