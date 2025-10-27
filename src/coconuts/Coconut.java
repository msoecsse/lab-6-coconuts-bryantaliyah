package coconuts;

import javafx.scene.image.Image;

/**
 * A falling coconut.
 *
 * Responsibilities
 *  - Falls every tick; when it touches ground objects (beach/crab) we publish events.
 *  - Collision y is its bottom edge so it properly meets ground tops.
 */
public class Coconut extends HittableIslandObject {
    private static final int WIDTH = 50;
    private static final Image coconutImage = new Image("file:images/coco-1.png");

    //Spawn a coconut at the top, with a fixed on-screen width.
    public Coconut(OhCoconutsGameManager game, int x) {
        super(game, x, 0, WIDTH, coconutImage);
    }

    //Fall down a little each frame, then update the ImageView position
    @Override public void step() {
        y += 5;      // falling speed
        display();   // move ImageView to (x,y)
    }

    // Marks that the coconut is a falling object (for collisions & rules)
    @Override public boolean isFalling() { return true; }

    //For falling objects, the collision line is the bottom edge,
     // which lets them meet the top edge of ground objects.
    @Override protected int hittable_height() { return y + width; }

    // Coconuts can collide with ground objects (beach, crab).
    @Override public boolean canHit(IslandObject other) { return other.isGroundObject(); }
}
