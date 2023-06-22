package mygame;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import java.util.Stack;

public class Octree {
    private final Vector3f position; // Position des Octree-Knotens
    private final Vector3f groesse; // Größe des Octree-Knotens
    private final int maxTiefe; // Maximale Tiefe des Octrees
    private final int maxBoids; // Maximale Anzahl von Boids in einem Knoten, bevor dieser unterteilt wird
    private List<Boid> boids; // Liste von Boids, die in diesem Knoten enthalten sind


    int aktuelleTiefe; // Aktuelle Tiefe dieses Knotens im Baum
    List<Octree> kind; // Liste von Kind-Knoten
    BoundingBox begerenzung; // Begrenzungsrahmen dieses Knotens
    BoundingSphere begrenzungsKugel; // Begrenzungskugel dieses Knotens

    // Konstruktor des Octree-Knotens
    public Octree(Vector3f position, Vector3f size, int maxTiefe, int maxBoids) {
        // Setzen der Attribute
        this.position = position;
        this.groesse = size;
        this.maxTiefe = maxTiefe;
        this.maxBoids = maxBoids;
        this.aktuelleTiefe = 0;
        this.boids = new ArrayList<>();
        this.kind = new ArrayList<>();
        // Erstellen des Begrenzungsrahmens und der Begrenzungskugel
        this.begerenzung = new BoundingBox(position, size);
        this.begrenzungsKugel = new BoundingSphere(size.length() / 2f, position.add(size).divide(2f));
    }


private void unterteilen() {
    // Wenn die maximale Anzahl an Boids in diesem Octree erreicht wurde oder die maximale Tiefe erreicht wurde, wird die Methode beendet.
    if (boids.size() <= maxBoids || aktuelleTiefe == maxTiefe) {
        return;
    }

    List<Boid> boidsImKind;
    // Füge Boids in die Kinder-Okbäume ein, wenn diese hineinpassen
    for (Octree child : kind) {
        boidsImKind = new ArrayList<>();
        for (Boid boid : boids) {
            if (child.enthaelt(boid.getPosition())) {
                boidsImKind.add(boid);
            }
        }

        // Wenn es Boids gibt, die in den aktuellen Kinder-Okbaum passen, dann werden sie diesem zugewiesen und dieser wird weiter unterteilt.
        if (!boidsImKind.isEmpty()) {
            child.boids = boidsImKind;
            child.unterteilen();
        }
    }
}


public void boidHinzufügen(Boid boid) {
    if (kind.isEmpty()) { // Wenn das Kind leer ist
        boids.add(boid); // Füge den Boid hinzu
        if (boids.size() > maxBoids && aktuelleTiefe < maxTiefe) { // Wenn die maximale Anzahl an Boids erreicht wurde und die aktuelle Tiefe kleiner als die maximale Tiefe ist
            unterteilen(); // Unterteile den Octree in 8 Kinder
            for (Boid childBoid : boids) {
                for (Octree child : kind) {
                    if (child.enthaelt(childBoid.getPosition())) { // Wenn das Kind den Boid enthält
                        child.boidHinzufügen(childBoid); // Füge den Boid dem Kind hinzu
                        break;
                    }
                }
            }
            boids.clear(); // Lösche alle Boids, da sie in Kinder verschoben wurden
        }
    } else { // Wenn das Kind nicht leer ist
        Vector3f point = boid.getPosition();
        int index = ((point.x > position.x) ? 1 : 0) // Berechne den Index des Kindes, das den Boid enthält
                + ((point.y > position.y) ? 2 : 0)
                + ((point.z > position.z) ? 4 : 0);
        kind.get(index).boidHinzufügen(boid); // Füge den Boid dem entsprechenden Kind hinzu
    }
}


public boolean enthaelt(Vector3f point) {
    // Berechnung der halben Breite, Höhe und Tiefe des Octree-Blocks
    float halbeBreite = groesse.x / 2.0f;
    float halbeHoehe = groesse.y / 2.0f;
    float halbeTiefe = groesse.z / 2.0f;

    // Prüfen, ob der Punkt außerhalb des Octree-Blocks liegt (x-Achse)
    if (Math.abs(point.x - position.x) > halbeBreite) {
        return false;
    }
    // Prüfen, ob der Punkt außerhalb des Octree-Blocks liegt (y-Achse)
    if (Math.abs(point.y - position.y) > halbeHoehe) {
        return false;
    }
    // Prüfen, ob der Punkt außerhalb des Octree-Blocks liegt (z-Achse)
    if (Math.abs(point.z - position.z) > halbeTiefe) {
        return false;
    }

    // Wenn der Punkt innerhalb des Octree-Blocks liegt, gib true zurück
    return true;
}



public void update(Boid boid) {
    if (boid == null) {
        return;
    }

    // Wenn die Anzahl der Boids unter dem Maximalwert liegt, füge den Boid einfach hinzu
    if (boids.size() < maxBoids) {
        // Überprüfe, ob der Boid innerhalb der Grenzen der Instanz liegt
        if (begerenzung == null || !begerenzung.contains(boid.getPosition())) {
            return;
        }
        boids.add(boid);
        return;
    }

    // Wenn es keine Unter-Octrees gibt, erstelle sie und füge den Boid hinzu
    if (kind.isEmpty()) {
        unterteilen();
    }

    // Füge den Boid zu den passenden Unter-Octrees hinzu
    for (Octree child : kind) {
        child.update(boid);
    }
}


public void queryReichweite(Vector3f mitte, float radius, List<Boid> ergebnis) {
    // Eine Stack-Datenstruktur wird verwendet, um die Octrees zu durchlaufen
    Stack<Octree> stack = new Stack<>();
    stack.push(this);

    while (!stack.isEmpty()) {
        // Der aktuelle Octree wird aus dem Stack geholt
        Octree aktuell = stack.pop();

        // Überprüfung, ob der aktuelle Octree mit dem Suchbereich schneidet
        if (aktuell.begerenzung == null || !aktuell.begerenzung.intersects(new BoundingSphere(radius, mitte))) {
            continue; // Wenn nicht, wird der aktuelle Octree übersprungen
        }

        // Alle Boids im aktuellen Octree innerhalb des Suchbereichs werden zur Ergebnisliste hinzugefügt
        for (Boid boid : aktuell.boids) {
            if (boid.getPosition().distance(mitte) <= radius) {
                ergebnis.add(boid);
            }
        }

        // Wenn der aktuelle Octree Kinder hat, werden sie zum Stack hinzugefügt
        if (!aktuell.kind.isEmpty()) {
            for (Octree child : aktuell.kind) {
                stack.push(child);
            }
        }
    }
}

}
