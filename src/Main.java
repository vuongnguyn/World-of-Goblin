import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("World of Goblin");
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getWidth() / 2 - 400.0);
        stage.setY(screenBounds.getHeight() / 2 - 300.0);
        stage.setWidth(screenBounds.getWidth() - 2 * stage.getX());
        stage.setHeight(screenBounds.getHeight() - 2 * stage.getY());
        
        Button button = new Button("bấm vào đây này!");
        button.setOnAction(e -> {
            System.out.println("Bạn đã bấm vào nút!");
        });

        StackPane root = new StackPane();
        root.getChildren().add(button);

        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}