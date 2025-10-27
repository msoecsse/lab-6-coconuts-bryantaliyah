package coconuts;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Base class for all world objects (crab, beach, coconut, laser).
 * This class owns position, size, and optional ImageView, plus common geometry helpers.
 */
public abstract class IslandObject {
    protected final int width;
    protected final OhCoconutsGameManager containingGame;
    protected int x, y;                  // top-left position in world coords.
    ImageView imageView = null;          // present if this object has a sprite

    public IslandObject(OhCoconutsGameManager game, int x, int y, int width, Image image) {
        containingGame = game;
        if (image != null) {
            imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(width);   // scale sprite to desired width
        }
        this.x = x;
        this.y = y;
        this.width = width;
        display();                          // ensure ImageView is placed initially
        //System.out.println(this + " left " + left() + " right " + right());
    }

    //manager to add/remove to the Pane
    protected ImageView getImageView() {
        return imageView;
    }

    //Push current (x,y) into the ImageView so it moves on screen
    public void display() {
        if (imageView != null) {
            imageView.setLayoutX(x);
            imageView.setLayoutY(y);
        }
    }

    // Whether the object is a target in hittable list.
    public boolean isHittable() {
        return false;
    }

    // The y-coordinate line used for collision checks (top or bottom)
    protected int hittable_height() {
        return 0;
    }

    // True if this object is considered “on the ground” (crab/beach)
    public boolean isGroundObject() {
        return false;
    }

    //True if this object is currently falling (coconuts)
    public boolean isFalling() {
        return false;
    }

    // True if this object can collide with the other based on game rules
    public boolean canHit(IslandObject other) {
        return false;
    }

    // ---------------- Geometry helpers (horizontal only) ----------------

    public int left()   { return x; }
    public int right()  { return x + width; }
    public int centerX(){ return x + (width / 2); }

    public int getWidth() {
        return width;
    }


     //Collision test used by the manager:
    public boolean isTouching(IslandObject other) {
        // y must be close enough (top vs. bottom lines)
        int dy = Math.abs(this.hittable_height() - other.hittable_height());
        if (dy > 4) return false;  // small tolerance

        // horizontal: other center within this bounds OR this center within other bounds
        int thisLeft  = this.left();
        int thisRight = this.right();
        int otherCX   = other.centerX();
        return (otherCX >= thisLeft && otherCX <= thisRight)
                || (this.centerX() >= other.left() && this.centerX() <= other.right());
    }

    // ---------------- Lifecycle / movement ----------------

    // Advance one tick
    public abstract void step();

    // Optional movement hooks (used by Crab via controller)
    public void moveLeft() { }
    public void moveRight() { }
}
