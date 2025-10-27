package coconuts;

import javafx.scene.image.Image;

/**
 * User controls crab - it lives on the ground and can be hit by falling coconuts.
 * It also serves as the origin for lasers (fired by the controller).
 */
public class Crab extends HittableIslandObject {
    private static final int WIDTH = 50;
    private static final Image crabImage = new Image("file:images/crab-1.png");

    // Start centered on the sand line
    public Crab(OhCoconutsGameManager game, int skyHeight, int islandWidth) {
        super(game, islandWidth / 2, skyHeight, WIDTH, crabImage);
    }

    // Crab is still unless moved by input.
    @Override public void step() { /* no-op */ }

    /** Move horizontally by a small offset; then sync its ImageView. */
    public void crawl(int offset) {
        x += offset;
        display();
    }

    // Crab is a ground object
    @Override public boolean isGroundObject() { return true; }

    //Ground objects use the top edge for collisions.
    @Override protected int hittable_height() { return y; }

    //Falling objects (coconuts) can "hit" the crab.
    @Override public boolean canHit(IslandObject other) { return other.isFalling(); }
}
