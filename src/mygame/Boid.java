/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

/**
 * Boid represents one individual boid in the flock.
 * It's motion is integrated within the update method, which should be called once per frame.
 * @author philipp lensing
 */
public class Boid {
    public static float spawnVolumeSize = 20f;
    public Vector3f position;
    public Vector3f geschwindigkeit;
    private Geometry geometry;
    public Vector3f ziel;
    public float radius = 3f;
    public Vector3f spawnPoint;
              
    /**
     * Der Konstruktor instanziiert ein Boid an einer zufälligen Position p innerhalb von -spawnVolumeSize/2 <= p <= spawnVolumeSize/2.Die Anfangsgeschwindigkeit wird auf einen zufälligen 3D-Vektor mit dem Betrag eins gesetzt.
     * @param geom entspricht einem Geometrieobjekt innerhalb des Szenegraphen und muss vorhanden sein.
     * @param spawnPoint
     */

    public Boid(Geometry geom) {
        this.geometry = geom;
        this.ziel = new Vector3f();
        this.spawnPoint = new Vector3f();
        geschwindigkeit = new Vector3f();
        position = new Vector3f();


     position.x = (FastMath.nextRandomFloat() -0.2f) * spawnVolumeSize; //Deaktivieren für Test Case 1-4
     position.y = (FastMath.nextRandomFloat() -0.2f) * spawnVolumeSize; //Deaktivieren für Test Case 1-4
     position.z = (FastMath.nextRandomFloat() -0.2f) * spawnVolumeSize; //Deaktivieren für Test Case 1-4
        
        geschwindigkeit.x = (FastMath.nextRandomFloat() -0.2f);
        geschwindigkeit.y = (FastMath.nextRandomFloat() -0.2f);
        geschwindigkeit.z = (FastMath.nextRandomFloat() -0.2f);
        geschwindigkeit.normalizeLocal();
        
    }

    public Boid(Geometry geom, Float x, Float y, Float z) {
        this.geometry = geom;
        geschwindigkeit = new Vector3f();
        position = new Vector3f();
        position.x = (x);
        position.y = (y);
        position.z = (z);
        geschwindigkeit.x = (FastMath.nextRandomFloat() -0.5f);
        geschwindigkeit.y = (FastMath.nextRandomFloat() -0.5f);
        geschwindigkeit.z = (FastMath.nextRandomFloat() -0.5f);
        geschwindigkeit.normalizeLocal();
    }
    
    /**
     * update berechnet die neue Position des Boids auf der Grundlage seiner aktuellen Position und der durch die Beschleunigung beeinflussten Geschwindigkeit. update sollte einmal pro Frame aufgerufen werden
     * @param accelaration Die Nettobeschleunigung aller Kräfte, die auf das Boot einwirken
     * @param dtime Die verstrichene Zeit in Sekunden zwischen zwei aufeinanderfolgenden Bildern
     */
   

public void update(Vector3f accelaration, float dtime) {
        
        geschwindigkeit = geschwindigkeit.add(accelaration.mult(dtime));
        position = position.add(geschwindigkeit.mult(dtime));    
        
      

        //update scene instance
        geometry.setLocalTranslation(position);
        geometry.lookAt(position.add(geschwindigkeit), Vector3f.UNIT_Y);
}
    
   

    // Konstruktor und andere Methoden hier
    public Vector3f getDirection() {
        return geschwindigkeit.normalize();
    }
    
    public Vector3f getPosition() {
        return this.position;
    }
    
    
    public float getRadius() {
        return radius;
    }

    public void setDirection(Vector3f direction) {
        geschwindigkeit.set(direction.normalize());
    }
    
    public void setZiel(Vector3f ziel) {
        this.ziel = ziel;
    }
      
}