package coconuts;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class ScoreBoard implements Observer {

    private int destroyed = 0;
    private int beached = 0;
    private final Label label;
    private final Pane gamePane;
    private final OhCoconutsGameManager manager;

    public ScoreBoard(Pane gamePane, OhCoconutsGameManager manager) {
        this.gamePane = gamePane;
        this.manager = manager;

        label = new Label("Destroyed: 0 | Beached: 0");
        label.setFont(new Font("Arial", 16));
        label.setTextFill(javafx.scene.paint.Color.WHITE);
        label.setLayoutX(10);
        label.setLayoutY(10);
        gamePane.getChildren().add(label);
    }

    @Override
    public void update(Object event) {
        if (event instanceof String e) {
            switch (e) {
                case "LASER_HIT_COCONUT" -> destroyed++;
                case "COCONUT_HIT_BEACH" -> beached++;
                case "CRAB_HIT" -> showFinalScore(); // game over
            }

            Platform.runLater(() ->
                    label.setText("Destroyed: " + destroyed + " | Beached: " + beached)
            );
        }
    }

    private void showFinalScore() {
        Platform.runLater(() -> {
            Label finalLabel = new Label(
                    "🏁 Game Over!\nDestroyed: " + destroyed + "\nBeached: " + beached
            );
            finalLabel.setFont(new Font("Arial", 32));
            finalLabel.setTextFill(javafx.scene.paint.Color.YELLOW);
            finalLabel.setTextAlignment(TextAlignment.CENTER);
            finalLabel.setLayoutX(gamePane.getWidth() / 2 - 150);
            finalLabel.setLayoutY(gamePane.getHeight() / 2 - 100);
            gamePane.getChildren().add(finalLabel);
        });
    }
}
