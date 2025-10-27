package coconuts;

import javafx.scene.image.Image;

/**
 * Domain rule summary:
 *  - Not ground, not falling.
 *  - Can "hit" falling objects (coconuts).
 *  - Uses its top edge (y) as the collision line.
 */
public class LaserBeam extends IslandObject {

    private static final int WIDTH = 10;                 // visual width of the beam sprite
    private static final Image LASER_IMG =
            new Image("file:images/laser-1.png");

    public LaserBeam(OhCoconutsGameManager game, int eyeY, int centerX) {
        // center the beam horizontally over the crab's centerX and start at eyeY
        super(game, centerX - WIDTH / 2, eyeY, WIDTH, LASER_IMG);
    }

    @Override protected int hittable_height() { return y; }       // collision line = top edge
    @Override public boolean isGroundObject() { return false; }   // projectile, not ground
    @Override public boolean isFalling()      { return false; }   // moves upward
    @Override public boolean canHit(IslandObject other) { return other.isFalling(); } // targets falling

    @Override
    public void step() {
        y -= 6;                    // move upward at a visible speed
        display();                 // sync ImageView position
        if (y < 0) containingGame.scheduleForDeletion(this); // clean up off-screen beams
    }
}
