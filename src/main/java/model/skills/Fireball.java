package model.skills;

import model.characters.CharacterObject;

public class Fireball extends Skill {
    public Fireball(double x, double y, double dx, double dy, CharacterObject owner) {
        super(x, y, 20, 20, "" /* TODO: "assets/images/fireball.png" */, dx, dy, 5.0, 25, owner);
    }
}
