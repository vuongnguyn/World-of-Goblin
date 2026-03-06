
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

public class Main extends Application {

    private Stage primaryStage;

    public Scene menuScene() {
        System.out.println(getClass().getResource("assets/images/menu_background.png"));

        Pane root = new Pane();

        Image bgImage = new Image(
                Objects.requireNonNull(
                        getClass().getResource("assets/images/menu_background.png")
                ).toExternalForm()
        );

        BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(800, 600, false, false, false, false)
        );

        Button button = createButton(100, 100, "assets/images/download.png");

        root.setBackground(new Background(backgroundImage));
        root.getChildren().add(button);

        return new Scene(root, 800, 600);
    }

    private Button createButton(double x, double y, String imagePath) {

        Image image = new Image(
                Objects.requireNonNull(
                        getClass().getResource(imagePath)
                ).toExternalForm()
        );

        ImageView imageView = new ImageView(image);

        Button button = new Button();
        button.setGraphic(imageView);
        button.setStyle("-fx-background-color: transparent;-fx-border-color: transparent;");

        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setPrefSize(100, 80);

        return button;
    }

    public void showMenu() {
        Scene scene = menuScene();
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