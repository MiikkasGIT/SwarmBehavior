package mygame;

import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.instancing.InstancedNode;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.List;

/**
 * This class controls and manages all boids within a flock (swarm)
 *
 * @author philipp lensing
 */
public class Flock {
  
    private Material boidMaterial;
    private Mesh boidMesh;
    private Node scene;
    private InstancedNode instancedNode;
    private List<Boid> boids;
    private ArrayList<Texture> textures;


    Octree octree = new Octree(
            new Vector3f(0, 0, 0),
            new Vector3f(100, 100, 100),
            10,
            10
    );

    public Flock(Node scene, int boidCount, Mesh boidMesh, Material boidMaterial, ArrayList<Texture> textures) {
        this.boidMesh = boidMesh;
        this.boidMaterial = boidMaterial;
        this.scene = scene;
        this.textures = textures;
        this.boidMaterial.setBoolean("UseInstancing", true);
        this.instancedNode = new InstancedNode("instanced_node");
        this.scene.attachChild(instancedNode);
        boids = createBoids(boidCount);
        instancedNode.instance();
    }


    public void update(float dtime) {
        for (Boid boid : boids) {

        octree.update(boid);
        
        List<Boid> nachbarn = new ArrayList<Boid>();
        octree.queryReichweite(boid.getPosition(), 50, nachbarn);
        
        
        Vector3f bewegungsIntegration = new Vector3f();
  
        int test = 5;
        
        switch (test) {
            case 1: // Separation
                Vector3f separationTest = erechneteSeparation(boid, 5f, nachbarn);
               
                bewegungsIntegration = separationTest;
                break;
            case 2: // Kohäsion
                Vector3f kohesionTest = erechneteKohesion(boid, 20f,nachbarn);
                
                bewegungsIntegration = kohesionTest;
                break;
                
            case 3: // Ausrichtung
                Vector3f ausrichtungTest = erechneteAusrichtung(boid, 90, 45, nachbarn);
               
                bewegungsIntegration = ausrichtungTest;
                break;
            
            case 4: // Separation + Kohesion
                Vector3f separationTest2 = erechneteSeparation(boid, 5f, nachbarn);
                Vector3f kohesionTest2 = erechneteKohesion(boid, 10f, nachbarn);
               
                bewegungsIntegration = separationTest2.add(kohesionTest2);
                break;
                
            case 5: // Separation + Kohesion + Ausrichtung
                
                Vector3f ausrichtung = erechneteAusrichtung(boid, 90, 25f, nachbarn);
                Vector3f kohesion = erechneteKohesion(boid, 25f, nachbarn);
                Vector3f separation = erechneteSeparation(boid, 15f,nachbarn);
               
                
                float separationWeight = 0.33f; // Gewichtung für Separation
                float ausrichtungWeight = 0.33f; // Gewichtung für Ausrichtung
                float kohesionWeight = 0.33f; // Gewichtung für Kohäsion
      
                
                bewegungsIntegration = separation.mult(separationWeight)
                                        .add(ausrichtung.mult(ausrichtungWeight))
                                        .add(kohesion.mult(kohesionWeight));
                                      
                break;

            default:
                break;
        }
        boid.update(bewegungsIntegration, dtime);
        }
    }
 
    private List<Boid> createBoids(int boidCount) {
        List<Boid> boidList = new ArrayList<>();

        for (int i = 0; i < boidCount; ++i) {
            Geometry geometry = createInstance(); // Erstelle ein separates Geometry-Objekt für jedes Boid
            Boid newBoid = new Boid(geometry);
            
            boidList.add(newBoid);
            octree.boidHinzufügen(newBoid);
        }

        return boidList;
    }

    private Geometry createInstance() {
        Geometry geometry = new Geometry("boid", boidMesh);

        int textureIndex = FastMath.nextRandomInt(0, textures.size() - 1);
        Texture texture = textures.get(textureIndex);

        Material clonedMaterial = boidMaterial.clone();
        clonedMaterial.setTexture("DiffuseMap", texture);

        geometry.setMaterial(clonedMaterial);
        instancedNode.attachChild(geometry);

        return geometry;
    }

    private Vector3f erechneteSeparation(Boid boid, float separationDistance, List<Boid> neighbors) {
        Vector3f separation = new Vector3f();

        if (neighbors.isEmpty()) {
            return separation;
        }

        for (Boid neighbor : neighbors) {
            if (neighbor != boid) {
                Vector3f difference = boid.getPosition().subtract(neighbor.getPosition());
                float distance = difference.length();

                if (distance < separationDistance && distance > 0) {
                    difference.divideLocal(distance * distance);
                    separation.addLocal(difference);
                }
            }
        }

        return separation;
    }

    private Vector3f erechneteAusrichtung(Boid boid, float radius, float winkel, List<Boid> nachbarn) {
        Vector3f durchschnittsGeschwindigkeit = new Vector3f();
        int zaehler = 0;

        float radiusSquared = radius * radius;

        for (int i = 0; i < nachbarn.size(); i++) {
            Boid andereBoids = nachbarn.get(i);
            if (andereBoids == boid) {
                continue;  // Überspringe den aktuellen Boid
            }

            float distanzSquared = boid.position.distanceSquared(andereBoids.position);
            if (distanzSquared < radiusSquared) {
                Vector3f richtung = andereBoids.position.subtract(boid.position).normalize();
                float skalarProdukt = boid.geschwindigkeit.dot(richtung);
                float andererWinkel = (float) Math.acos(skalarProdukt);
                if (andererWinkel < winkel) {
                    durchschnittsGeschwindigkeit = durchschnittsGeschwindigkeit.add(andereBoids.geschwindigkeit);
                    zaehler++;
                }
            }
        }

        if (zaehler == 0) {
            return new Vector3f();
        }

        durchschnittsGeschwindigkeit.divideLocal(zaehler);
        Vector3f ausrichtung = durchschnittsGeschwindigkeit.subtract(boid.geschwindigkeit);

        return ausrichtung;
    }

     private Vector3f erechneteKohesion(Boid boid, float kohesionsDistanz, List<Boid> nachbarn) {
            int nachbarCount = 0;
            Vector3f kohesionsSumme = new Vector3f();

            for (Boid nachbar : nachbarn) {
                if (nachbar != boid) {
                    float distanz = nachbar.getPosition().distance(boid.getPosition());
                    if (distanz <= kohesionsDistanz) {
                        kohesionsSumme.addLocal(nachbar.getPosition());
                        nachbarCount++;
                    }
                }
            }

            if (nachbarCount > 0) {
                kohesionsSumme.divideLocal(nachbarCount);
                return kohesionsSumme.subtract(boid.getPosition()).normalize();
            }

            return new Vector3f();
        }
    }