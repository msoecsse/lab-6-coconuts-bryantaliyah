package coconuts;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * JavaFX controller (View + input wiring).
 *
 * Responsibilities
 *  - Builds the game model (OhCoconutsGameManager) and passes the Pane so the manager
 *    can add/remove ImageViews.
 *  - Runs the game loop : spawn, step, check for end, show final score.
 *  - Listens to keys and calls model actions (left/right/laser).
 */
public class GameController {

    //Time between ticks → ~30 FPS.
    private static final double MILLISECONDS_PER_STEP = 1000.0 / 30;

    private Timeline coconutTimeline;
    private boolean started = false;

    // These must match fx:id values in coconuts.fxml
    @FXML private Pane gamePane;
    @FXML private Pane theBeach;

    // Game model owned by the controller
    private OhCoconutsGameManager theGame;

    //Initialize after FXML loads: create model, hook input, and prep the loop.
    @FXML
    public void initialize() {
        // Build the model with world sizes derived from FXML layout
        theGame = new OhCoconutsGameManager(
                (int) (gamePane.getPrefHeight() - theBeach.getPrefHeight()), // sky (play) height
                (int) gamePane.getPrefWidth(),                               // island width
                gamePane                                                     // pane for sprites
        );

        // Direct key handling (LEFT/RIGHT/UP). Space is handled in onKeyPressed below for pause.
        gamePane.setFocusTraversable(true);
        gamePane.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT  -> theGame.getCrab().moveLeft();
                case RIGHT -> theGame.getCrab().moveRight();
                case UP    -> theGame.fireLaser();    // fire from crab's center
                default    -> { /* ignore */ }
            }
        });

        // The game loop: drop coconuts, move everything, detect hits.
        coconutTimeline = new Timeline(new KeyFrame(Duration.millis(MILLISECONDS_PER_STEP), e -> {
            theGame.tryDropCoconut();     // maybe spawn a new coconut this tick
            theGame.advanceOneTick();     // move + detect collisions + notify observers

            // End condition: either crab is hit (gameOver), or level time is up.
            if (theGame.isGameOver() || theGame.done()) {
                coconutTimeline.pause();
                theGame.showFinalScore(); // overlay final banner with scoreboard summary
                return;
            }
        }));
        coconutTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    //Optional secondary handler (kept for pause / accessibility):
    // RIGHT/LEFT - crawl when not done; SPACE - pause/start
    @FXML
    public void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.RIGHT && !theGame.done()) {
            theGame.getCrab().crawl(10);
        } else if (keyEvent.getCode() == KeyCode.LEFT && !theGame.done()) {
            theGame.getCrab().crawl(-10);
        } else if (keyEvent.getCode() == KeyCode.SPACE) {
            if (!started) {
                coconutTimeline.play();
            } else {
                coconutTimeline.pause();
            }
            started = !started;
        }
    }
}
