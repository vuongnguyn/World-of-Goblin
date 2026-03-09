package controller;

import javafx.scene.input.KeyCode;
import model.characters.CharacterObject;
import model.characters.Knight;
import model.characters.Player;
import model.characters.Wizard;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class GameManager {
    private Player player;
    private GameState gameState;
    private List<CharacterObject> characters;
    
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    
    public enum GameState {
        MENU,
        PLAYING,
        PAUSED,
        GAME_OVER
    }

    public void initializeGame() {
        player = new Player(100, 100, 50, 50, "player.png", 5, 100, 10);
        characters.add(new Knight(200, 200, 50, 50, "knight.png", 3, 80, 15));
        characters.add(new Wizard(300, 300, 50, 50, "wizard.png", 4, 60, 20));
        gameState = GameState.MENU;
        characters = new ArrayList<>();
    }

    public void handleInput(KeyCode keyCode, boolean isPressed) {
        if (isPressed) {
            pressedKeys.add(keyCode);
        } else {
            pressedKeys.remove(keyCode);
        }
    }
}
