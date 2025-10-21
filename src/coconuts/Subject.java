package coconuts;

public interface Subject {
    void attach(Observer o);
    void detach(Observer o);

    // Use a unique name to avoid clashing with Object.notifyAll()
    void notifyObservers(Object event);
}
