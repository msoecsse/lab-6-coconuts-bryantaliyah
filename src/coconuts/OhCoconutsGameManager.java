package coconuts;

import javafx.scene.layout.Pane;
import javafx.scene.image.ImageView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages island objects, collisions, and hit notifications.
 * Implements a simple Subject (Observer pattern) so the ScoreBoard

 * Core loop each tick:
 *  1) step() all objects,
 *  2) detect collisions and publish events,
 *  3) queue and remove any objects to delete.
 */
public class OhCoconutsGameManager implements Subject {

    // --- Collections of game objects ---
    private final Collection<IslandObject>        allObjects         = new LinkedList<>();
    private final Collection<HittableIslandObject> hittables         = new LinkedList<>();
    private final Collection<IslandObject>        scheduledForRemoval= new LinkedList<>();

    // --- World + UI ---
    private final int height, width;
    private final Pane gamePane;

    private Crab  theCrab;
    private Beach theBeach;

    // --- Tick state ---
    private int coconutsInFlight = 0;
    private int gameTick         = 0;
    private static final int DROP_INTERVAL = 10;     // lower = more coconuts
    private static final int MAX_TIME      = 10_000;

    // --- Observer hub (Subject impl) ---
    private final List<Observer> observers = new LinkedList<>();
    private ScoreBoard scoreboard;                   // HUD (observer)

    // --- Game-over state ---
    private boolean gameOver = false;
    public boolean isGameOver() { return gameOver; }

    /** One-shot transition to game over; also triggers the final overlay. */
    private void triggerGameOver() {
        if (gameOver) return;
        gameOver = true;
        showFinalScore();      // show the big overlay from ScoreBoard
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    public OhCoconutsGameManager(int height, int width, Pane gamePane) {
        this.height  = height;
        this.width   = width;
        this.gamePane = gamePane;

        // Attach ScoreBoard HUD (top-left)
        this.scoreboard = new ScoreBoard();
        attach(scoreboard);
        if (scoreboard.getMiniLabel() != null) {
            gamePane.getChildren().add(scoreboard.getMiniLabel());
        }

        // Crab (centered) and Beach
        theCrab = new Crab(this, height, width);
        registerObject(theCrab);
        gamePane.getChildren().add(theCrab.getImageView());

        theBeach = new Beach(this, height, width);
        registerObject(theBeach);
        // beach has no image view (domain-only), so no add to pane
    }

    // ------------------------------------------------------------------------
    // Subject implementation
    // ------------------------------------------------------------------------
    @Override
    public void attach(Observer o) { observers.add(o); }

    @Override
    public void detach(Observer o) { observers.remove(o); }

    @Override
    public void notifyObservers(Object event) {
        for (Observer o : observers) o.update(event);
    }

    // ------------------------------------------------------------------------
    // Public API used by controller/timeline
    // ------------------------------------------------------------------------

     //Adds it to collections and attaches its ImageView to the pane
    public void tryDropCoconut() {
        if (gameTick % DROP_INTERVAL == 0 && theCrab != null) {
            coconutsInFlight += 1;
            Coconut c = new Coconut(this, (int) (Math.random() * width));
            registerObject(c);
            ImageView iv = c.getImageView();
            if (iv != null) gamePane.getChildren().add(iv);
        }
        gameTick++;
    }

    //Moves objects, checks collisions, and cleans up
    public void advanceOneTick() {
        // 1) Move everything one step
        for (IslandObject obj : allObjects) {
            obj.step();
        }

        // 2) Check hits (no instanceof): let objects declare their own rules
        for (IslandObject actor : allObjects) {
            for (HittableIslandObject target : hittables) {
                if (actor.canHit(target) && actor.isTouching(target)) {

                    // Publish simple events based on roles of actor/target
                    if (!actor.isGroundObject() && !actor.isFalling() && target.isFalling()) {
                        notifyObservers("LASER_HIT_COCONUT");
                    } else if (actor == theBeach && target.isFalling()) {
                        notifyObservers("COCONUT_HIT_BEACH");
                    } else if (actor == theCrab && target.isFalling()) {
                        notifyObservers("CRAB_HIT");
                        triggerGameOver();   // <-- stop the game & show overlay
                    }

                    // Queue the hit target for removal
                    scheduledForRemoval.add(target);
                    ImageView tiv = target.getImageView();
                    if (tiv != null) gamePane.getChildren().remove(tiv);

                    // If the actor is a projectile (not ground & not falling), remove it too
                    if (!actor.isGroundObject() && !actor.isFalling()) {
                        scheduledForRemoval.add(actor);
                        ImageView aiv = actor.getImageView();
                        if (aiv != null) gamePane.getChildren().remove(aiv);
                    }
                }
            }
        }

        // 3) Actually remove queued objects (no instanceof)
        for (IslandObject obj : scheduledForRemoval) {
            allObjects.remove(obj);

            if (obj.isHittable()) {
                // Safe cast after isHittable() guard
                hittables.remove((HittableIslandObject) obj);
            }

            ImageView iv = obj.getImageView();
            if (iv != null) gamePane.getChildren().remove(iv);
        }
        scheduledForRemoval.clear();
    }

    //create a laser at the crab's eye line and attach it to the world + pane
    public void fireLaser() {
        if (theCrab == null) return;

        // Fire from the crab’s “eyes” (slightly above its top)
        int eyeY     = theCrab.y - 6; // y is protected in IslandObject (same package)
        int centerX  = theCrab.centerX();

        LaserBeam beam = new LaserBeam(this, eyeY, centerX);
        registerObject(beam);
        ImageView iv = beam.getImageView();
        if (iv != null) gamePane.getChildren().add(iv);
    }

    //called by objects that want to be removed at the end of the tick
    public void scheduleForDeletion(IslandObject obj) {
        scheduledForRemoval.add(obj);
    }

    //Show big final scoreboard
    public void showFinalScore() {
        if (scoreboard != null && gamePane != null) {
            scoreboard.showFinalOn(gamePane);
        }
    }

    //Level done: no coconuts left and time limit reached
    public boolean done() {
        return coconutsInFlight == 0 && gameTick >= MAX_TIME;
    }

    // ------------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------------
    /** Add object to world lists (and hittables if applicable). */
    private void registerObject(IslandObject object) {
        allObjects.add(object);
        if (object.isHittable()) {
            HittableIslandObject asHittable = (HittableIslandObject) object;
            hittables.add(asHittable);
        }
    }

    // ------------------------------------------------------------------------
    // Getters used by others
    // ------------------------------------------------------------------------
    public int getHeight() { return height; }
    public int getWidth()  { return width;  }
    public Crab getCrab()  { return theCrab; }

    //count when a coconut is removed from play
    public void coconutDestroyed() { coconutsInFlight -= 1; }
}
