package coconuts;

import javafx.scene.layout.Pane;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/** Manages island objects, collisions, and hit notifications. */
public class OhCoconutsGameManager implements Subject {

    // --- Collections of game objects ---
    private final Collection<IslandObject> allObjects          = new LinkedList<>();
    private final Collection<HittableIslandObject> hittables   = new LinkedList<>();
    private final Collection<IslandObject> scheduledForRemoval = new LinkedList<>();

    // --- World + UI ---
    private final int height, width;
    private final int DROP_INTERVAL = 10;
    private final int MAX_TIME = 100;
    private final Pane gamePane;

    // --- Key actors ---
    private Crab  theCrab;
    private Beach theBeach;
    private ScoreBoard scoreboard;


    // --- Tick state ---
    private int coconutsInFlight = 0;
    private int gameTick = 0;

    // --- Observer hub (Subject impl) ---
    private final List<Observer> observers = new LinkedList<>();
    //private final ScoreBoard scoreboard = new ScoreBoard();

    public OhCoconutsGameManager(int height, int width, Pane gamePane) {
        this.height = height;
        this.width  = width;
        this.gamePane = gamePane;

        // Create & register core objects
        theCrab = new Crab(this, height, width);
        registerObject(theCrab);
        gamePane.getChildren().add(theCrab.getImageView());

        notifyObservers("LASER_HIT_COCONUT");


        theBeach = new Beach(this, height, width);
        registerObject(theBeach);
        if (theBeach.getImageView() != null) {
            System.out.println("Unexpected image view for beach");
        }

        // Attach ScoreBoard observer
        this.scoreboard = new ScoreBoard(gamePane, this);  // pass gamePane to place label
        attach(scoreboard);
        System.out.println("✅ ScoreBoard attached to GameManager!");
    }

    // ---- Subject implementation ----
    @Override public void attach(Observer o) { observers.add(o); }

    @Override public void detach(Observer o) { observers.remove(o); }

    @Override public void notifyObservers(Object event) {
        for (Observer o : observers) o.update(event);
    }

    // Backwards-compat if you already called notifyAllObservers(...)
    public void notifyAllObservers(Object event) {
        notifyObservers(event);
    }


    // ---- Object registration ----
    private void registerObject(IslandObject obj) {
        allObjects.add(obj);
        if (obj.isHittable()) {              // no instanceof
            hittables.add((HittableIslandObject) obj); // safe cast by contract
        }
    }

    public void scheduleForDeletion(IslandObject obj) { scheduledForRemoval.add(obj); }

    // ---- Spawning / ticks ----
    public void tryDropCoconut() {
        if (gameTick % DROP_INTERVAL == 0 && theCrab != null) {
            coconutsInFlight++;
            Coconut c = new Coconut(this, (int) (Math.random() * width));
            registerObject(c);
            gamePane.getChildren().add(c.getImageView());
        }
        gameTick++;
    }

    public void coconutDestroyed() { coconutsInFlight--; }

    public void advanceOneTick() {
        // check hits
        for (IslandObject actor : allObjects) {
            for (HittableIslandObject target : hittables) {
                if (actor.canHit(target) && actor.isTouching(target)) {

                    // --- Publish simple events (no instanceof) ---
                    if (!actor.isGroundObject() && !actor.isFalling() && target.isFalling()) {
                        // e.g., a laser (not ground, not falling) hits a falling coconut
                        notifyObservers("LASER_HIT_COCONUT");
                    } else if (actor == theBeach && target.isFalling()) {
                        notifyObservers("COCONUT_HIT_BEACH");
                    } else if (actor == theCrab && target.isFalling()) {
                        notifyObservers("CRAB_HIT");
                    }


                    // remove visual + queue for deletion
                    scheduledForRemoval.add(target);
                    gamePane.getChildren().remove(target.getImageView());
                }
            }
        }

        // actually remove queued objects (no instanceof)
        for (IslandObject obj : scheduledForRemoval) {
            allObjects.remove(obj);
            if (obj.isHittable()) {
                hittables.remove((HittableIslandObject) obj);
            }
        }
        scheduledForRemoval.clear();
    }

    public boolean done() { return coconutsInFlight == 0 && gameTick >= MAX_TIME; }

    // ---- Getters ----
    public Crab getCrab() { return theCrab; }
    public int getHeight() { return height; }
    public int getWidth() { return width; }
}
