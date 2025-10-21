package coconuts;

/**
 * The Observer side of the Observer pattern.
 * Any class implementing this interface can listen for events.
 */
public interface Observer {
    /**
     * Called when the subject sends an update.
     * @param event - an object or string describing what happened
     */
    void update(Object event);

    // 💡 Optional helper (for readable console testing)
    default void log(Object event) {
        System.out.println(getClass().getSimpleName() + " received: " + event);
    }
}
