package coconuts;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Events it listens for:
 *  "LASER_HIT_COCONUT" -> destroyed++
 *  "COCONUT_HIT_BEACH" -> beached++
 *  "CRAB_HIT"          -> (optional: just keep counts; end handled by GameManager)
 */
public class ScoreBoard implements Observer {

    // counts
    private int destroyed = 0;
    private int beached   = 0;

    // UI
    private final Label miniLabel;   // always visible, top-left
    private Label finalLabel;        // shown once on game over

    public ScoreBoard() {
        // build the small, always-on label
        miniLabel = new Label();
        miniLabel.setTextFill(Color.WHITE);
        miniLabel.setFont(Font.font(18));
        miniLabel.setLayoutX(12);
        miniLabel.setLayoutY(10);
        miniLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 6, 0.8, 1, 1);");

        refreshMini();
    }

    //Expose the mini label so the GameManager/Controller can add it to the Pane
    public Label getMiniLabel() {
        return miniLabel;
    }

    //Current totals (optional getters if you want them)
    public int getDestroyed() { return destroyed; }
    public int getBeached()   { return beached; }

    //Observer callback: update counters based on simple string event names
    @Override
    public void update(Object event) {
        if (!(event instanceof String)) return;
        String e = (String) event;

        // no switch/case: simple if-else chain
        if ("LASER_HIT_COCONUT".equals(e)) {
            destroyed++;
        } else if ("COCONUT_HIT_BEACH".equals(e)) {
            beached++;
        } else if ("CRAB_HIT".equals(e)) {
            // nothing to count here; game manager will stop the game
        }

        refreshMini();
    }

    //Update the top-left mini label
    private void refreshMini() {
        Platform.runLater(() ->
                miniLabel.setText("Destroyed: " + destroyed + " | Beached: " + beached)
        );
    }

     //Show a centered final score banner on the given Pane.
     //Call this once when the game ends.

    public void showFinalOn(Pane pane) {
        Platform.runLater(() -> {
            if (finalLabel == null) {
                finalLabel = new Label();
                finalLabel.setTextFill(Color.WHITE);
                finalLabel.setFont(Font.font(28));
                finalLabel.setStyle(
                        "-fx-background-color: rgba(0,0,0,0.55);" +
                                "-fx-padding: 16 24;" +
                                "-fx-background-radius: 10;"
                );
            }

            finalLabel.setText("Game Over — Destroyed: " + destroyed + " | Beached: " + beached);

            if (!pane.getChildren().contains(finalLabel)) {
                pane.getChildren().add(finalLabel);
            }

            // center after autosize so width/height are known
            finalLabel.autosize();
            double x = (pane.getWidth()  - finalLabel.getWidth())  / 2.0;
            double y = (pane.getHeight() - finalLabel.getHeight()) / 2.0;
            finalLabel.setLayoutX(Math.max(0, x));
            finalLabel.setLayoutY(Math.max(0, y));
        });
    }

    //Optional: clear counts if you add a reset/restart later
    public void reset() {
        destroyed = 0;
        beached   = 0;
        refreshMini();
        if (finalLabel != null) {
            Label toRemove = finalLabel;
            finalLabel = null;
            Platform.runLater(() -> {
                Pane parent = (Pane) toRemove.getParent();
                if (parent != null) parent.getChildren().remove(toRemove);
            });
        }
    }
}
