package coconuts;

/**
 * The ground strip at the bottom of the world.
 *
 * Responsibilities
 *  - Sits at the top of the sand (y is the top edge of the beach image).
 *  - Collides with falling objects (coconuts) to count "beached".
 */
public class Beach extends IslandObject {

    //Position the beach at the world bottom
    public Beach(OhCoconutsGameManager game, int skyHeight, int islandWidth) {
        // image is null: the Beach has no ImageView (it's rendered by FXML background)
        super(game, 0, skyHeight, islandWidth, null);
    }

    // Beach doesn’t move
    @Override public void step() { /* no-op */ }

    // Beach is on the ground, so other objects treat it as ground
    @Override public boolean isGroundObject() { return true; }

    //For ground objects the collision line is their top edge (y).
    @Override protected int hittable_height() { return y; }

    //The beach "hits" -  it catches anything that is falling.
    @Override public boolean canHit(IslandObject other) { return other.isFalling(); }
}
