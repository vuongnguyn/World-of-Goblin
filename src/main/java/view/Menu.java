package view;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

public class Menu {

    public Scene menuScene(Stage stage) {

        Pane root = new Pane();

        Image bgImage = new Image(
                Objects.requireNonNull(
                        getClass().getResource("file:assets/image/menu_background.png")
                ).toExternalForm()
        );

        BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(800, 600, false, false, false, false)
        );

        root.setBackground(new Background(backgroundImage));

        Button buttonJoinGame = createButton(300, 200, "file:assets/image/new_game.png");

        root.getChildren().add(buttonJoinGame);

        return new Scene(root, 800, 600);
    }

    private Button createButton(double x, double y, String imagePath) {

        Image image = new Image(
                Objects.requireNonNull(
                        getClass().getResource("file:" + imagePath)
                ).toExternalForm()
        );

        ImageView imageView = new ImageView(image);

        Button button = new Button();
        button.setGraphic(imageView);
        button.setStyle("-fx-background-color: transparent;");
        button.setLayoutX(x);
        button.setLayoutY(y);

        return button;
    }
}
// Button buttonSetting = createButton("Setting", 300, 300, "/assets/image/setting.png");
        // Button buttonExit = createButton("Exit", 300, 400, "/assets/image/exit.png");
        // root.getChildren().addAll(buttonJoinGame, buttonSetting, buttonExit);