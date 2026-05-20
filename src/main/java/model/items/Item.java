package model.items;

public enum Item {
    WOOD("Wood"),
    STONE("Stone"),
    BERRY("Berry");

    private final String displayName;

    Item(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
