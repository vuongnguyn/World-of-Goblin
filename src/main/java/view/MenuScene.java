package view;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;


import java.util.Objects;

public class MenuScene {

    private Runnable onNewGame;
    private Runnable onContinue;
    private Runnable onExit;

    public void setOnNewGame(Runnable r) { this.onNewGame = r; }
    public void setOnContinue(Runnable r) { this.onContinue = r; }
    public void setOnExit(Runnable r)     { this.onExit = r; }


    // This method creates a menu scene with where has continue button
    public Scene menuSceneWithContinue() {
        Pane root = new Pane();

        Image bgImage = new Image(
                Objects.requireNonNull(
                        getClass().getResource("../assets/images/menu_background.png")
                ).toExternalForm()
        );

        BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(800, 600, false, false, false, false)
        );

        

        ImageView menuButtonImageView = new ImageView(new Image(
                Objects.requireNonNull(
                        getClass().getResource("../assets/images/menu_button_image_within_conti.png")
                ).toExternalForm()
        ));
        menuButtonImageView.setLayoutX(175);
        menuButtonImageView.setLayoutY(250);
        menuButtonImageView.setFitWidth(450);
        menuButtonImageView.setFitHeight(300);

        ImageView logoImageView = new ImageView(new Image(
                Objects.requireNonNull(
                        getClass().getResource("../assets/images/logo_in_menu_game.png")
                ).toExternalForm()
        ));
        logoImageView.setLayoutX(100);
        logoImageView.setLayoutY(-150);
        logoImageView.setFitWidth(600);
        logoImageView.setFitHeight(600);

        // button in menu game is invisible, so we can click on the image to trigger the button's action
        Button buttonNewGame = createButton(272, 300);
        Button buttonContinue = createButton(272, 370);
        Button buttonSetting = createButton(272, 440);
        Button buttonExit = createButton(272, 510);
        buttonNewGame.setOnAction(e  -> { if (onNewGame  != null) onNewGame.run(); });
        buttonContinue.setOnAction(e -> { if (onContinue != null) onContinue.run(); });
        buttonSetting.setOnAction(e  -> System.out.println("Settings – coming soon"));
        buttonExit.setOnAction(e     -> { if (onExit     != null) onExit.run(); });

        root.setBackground(new Background(backgroundImage));
        root.getChildren().addAll(menuButtonImageView, logoImageView);
        root.getChildren().addAll(buttonNewGame, buttonContinue, buttonSetting, buttonExit);

        return new Scene(root, 800, 600);
    }

    // This method creates a menu scene without continue button
    public Scene menuSceneWithoutContinue() {
        Pane root = new Pane();

        Image bgImage = new Image(
                Objects.requireNonNull(
                        getClass().getResource("../assets/images/menu_background.png")
                ).toExternalForm()
        );

        BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(800, 600, false, false, false, false)
        );

        

        ImageView menuButtonImageView = new ImageView(new Image(
                Objects.requireNonNull(
                        getClass().getResource("../assets/images/menu_button_image_without_conti.png")
                ).toExternalForm()
        ));
        menuButtonImageView.setLayoutX(175);
        menuButtonImageView.setLayoutY(250);
        menuButtonImageView.setFitWidth(450);
        menuButtonImageView.setFitHeight(300);

        ImageView logoImageView = new ImageView(new Image(
                Objects.requireNonNull(
                        getClass().getResource("../assets/images/logo_in_menu_game.png")
                ).toExternalForm()
        ));
        logoImageView.setLayoutX(100);
        logoImageView.setLayoutY(-150);
        logoImageView.setFitWidth(600);
        logoImageView.setFitHeight(600);

        // button in menu game is invisible, so we can click on the image to trigger the button's action
        Button buttonNewGame = createButton(272, 300);
        Button buttonSetting = createButton(272, 370);
        Button buttonExit = createButton(272, 440);
        buttonNewGame.setOnAction(e -> { if (onNewGame != null) onNewGame.run(); });
        buttonSetting.setOnAction(e  -> System.out.println("Settings – coming soon"));
        buttonExit.setOnAction(e     -> { if (onExit    != null) onExit.run(); });

        root.setBackground(new Background(backgroundImage));
        root.getChildren().addAll(menuButtonImageView, logoImageView);
        root.getChildren().add(buttonNewGame);
        root.getChildren().add(buttonSetting);
        root.getChildren().add(buttonExit);

        return new Scene(root, 800, 600);
    }
    
    // Create a button for menu game
    private Button createButton(double x, double y) {
        Button button = new Button();
        button.setStyle("-fx-background-color: transparent;-fx-border-color: transparent;");
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setPrefWidth(218);
        button.setPrefHeight(45);

        return button;
    }
}