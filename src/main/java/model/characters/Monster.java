package model.characters;

public class Monster extends Enemy {
    public Monster(double x, double y) {
        super(x, y, 50, 50, "" /* TODO: "assets/images/monster.png" */, 1.5, 100, 20);
    }
}
