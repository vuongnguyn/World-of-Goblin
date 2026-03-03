package World_of_Goblin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import World_of_Goblin.view.Menu;

public class Main extends Application {

    private Stage primaryStage;
    private Menu menu = new Menu();

    public void showMenu() {
        Scene scene = menu.menuScene(primaryStage, this);
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