package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;
import java.util.ArrayList;

/**
 * Dies ist die Hauptklasse deines Spiels. Hier solltest du nur Initialisierungen vornehmen.
 */

public class Main extends SimpleApplication {
    public static void main(String[] args) {
        Main app = new Main();
       
        // Setze den Vollbildmodus
        AppSettings settings = new AppSettings(true);
        app.setSettings(settings);
        
        app.start();
    }
    
    private final int boidCount = 3000;
    private float waterHeight = 25.0f; // Höhe des Wassers (angepasst an Ihre Szene)
    private Flock flock;
    private Spatial model;
    private WaterFilter waterFilter;
    private FilterPostProcessor fpp;
    private DirectionalLightShadowRenderer dlsr;
    private LightProbe lightProbe;
    private AudioNode audioNode;

    
    @Override
    public void simpleInitApp() {
        // Deaktiviere das FPS Showcase-Interface
        setDisplayStatView(false);
        setDisplayFps(false);
        
        // Setze die Geschwindigkeit der Kamerafahrt
        flyCam.setMoveSpeed(10f);

        // Aktiviere die Maussteuerung der Kamera
        flyCam.setEnabled(true);

        setupFX();
        setupAudio();
        setupFlock(); // Instanziierung der Flock
        loadSkybox(); // Laden der Skybox

    }
    

    private void setupFX() {
        // Gerichtete Lichtquelle
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f));

        // Setze die Farbe der Lichtstrahlen auf helles Blau
        ColorRGBA sunColor = new ColorRGBA(0.52941f, 0.80784f, 0.92157f, 1.0f); // Helles Blau
        float intensityMultiplier = 3.6f; // Anpassen des Multiplikators für die Intensität
        sunColor = sunColor.mult(intensityMultiplier);
        sun.setColor(sunColor);

        rootNode.addLight(sun);

        // Umgebungslicht
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(0.8f)); // Anpassen der Helligkeit des Umgebungslichts
        rootNode.addLight(ambientLight);

        // WasserFilter und Eigenschaften
        Vector3f lightDir = new Vector3f(-0.5f, -0.5f, -0.5f);
        waterFilter = new WaterFilter(rootNode, lightDir);
        waterFilter.setWaterHeight(waterHeight);
        waterFilter.setWaterTransparency(0.45f); // Transparenz des Wassers
        waterFilter.setFoamIntensity(0.8f); // Intensität des Schaumes
        waterFilter.setRefractionStrength(0.1f); // Stärke der Brechung
        waterFilter.setShoreHardness(0.8f); // Härte der Uferlinie
        waterFilter.setDeepWaterColor(new ColorRGBA(0.0f, 0.74902f, 1.0f, 1.0f));  // Farbe des tiefen Wassers
        waterFilter.setWaterColor(new ColorRGBA(0.52941f, 0.80784f, 0.92157f, 1.0f));     // Farbe des flachen Wassers
        
     
        // WasserFilter zum FilterPostProcessor
        fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(waterFilter);
        viewPort.addProcessor(fpp);

        // Unterwasser-Nebel
        FogFilter fog = new FogFilter();
        fog.setFogColor(new ColorRGBA(0.1f, 0.4f, 0.6f, 1.0f)); // Farbe des Unterwasser-Nebels
        fog.setFogDensity(1.6f); //Dichte des Nebels
        fpp.addFilter(fog);

        // Lichtstreuungsfilter für Unterwasserstrahlen
        LightScatteringFilter lightScatteringFilter = new LightScatteringFilter(sun.getDirection().normalizeLocal());
 
        fpp.addFilter(lightScatteringFilter);


        // Schattenrenderer für das gerichtete Licht
        dlsr = new DirectionalLightShadowRenderer(assetManager, 2048, 3);
        dlsr.setLight(sun);
        dlsr.setShadowIntensity(0.6f); // Setze die Intensität der Schatten
        dlsr.setShadowZExtend(100f); // Setze die Länge des Schattenbereichs entlang der Lichtrichtung
        dlsr.setLambda(0.25f); // Setze den Lambda-Wert für die Schattenberechnung
        viewPort.addProcessor(dlsr);
    }
    
    private void setupAudio() { 
        
        audioNode = new AudioNode(assetManager, "Sounds/underwater-loop.wav", false);
        audioNode.setLooping(true);
        audioNode.setPositional(false);
        audioNode.setVolume(0.2f);
        rootNode.attachChild(audioNode);
        audioNode.play();
    }
    private void setupFlock() {
        // 3D-Modell laden
        model = assetManager.loadModel("Models/Fish/fish-v7.obj");
       
        // Extrahiere das Mesh aus dem Modell
        Mesh mesh = null;
        Geometry geometry = (Geometry) model;
        if (geometry != null) {
            mesh = geometry.getMesh();
        }

       
        ArrayList<Texture> textures = new ArrayList<>();
        textures.add(assetManager.loadTexture("Textures/Fish/fish-texture1-min.jpg"));
        textures.add(assetManager.loadTexture("Textures/Fish/fish-texture2-min.jpg"));
        textures.add(assetManager.loadTexture("Textures/Fish/fish-texture3-min.jpg"));
        textures.add(assetManager.loadTexture("Textures/Fish/fish-texture4-min.jpg"));
        textures.add(assetManager.loadTexture("Textures/Fish/fish-texture5-min.jpg"));
        textures.add(assetManager.loadTexture("Textures/Fish/fish-texture6-min.jpg"));
        textures.add(assetManager.loadTexture("Textures/Fish/fish-texture1-min.jpg"));
        textures.add(assetManager.loadTexture("Textures/Fish/fish-texture4-min.jpg"));
        
        // Lighting.j3md-Materialdefinition
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        // Wende das neue Material auf das Modell an
        model.setMaterial(mat);

        // Instanziierung der Flock
        flock = new Flock(rootNode, boidCount, mesh, mat, textures);
    }

    private void loadSkybox() {
        Texture west = assetManager.loadTexture("Textures/Sky/west.jpg");
        Texture east = assetManager.loadTexture("Textures/Sky/east.jpg");
        Texture north = assetManager.loadTexture("Textures/Sky/north.jpg");
        Texture south = assetManager.loadTexture("Textures/Sky/south.jpg");
        Texture up = assetManager.loadTexture("Textures/Sky/up.jpg");
        Texture down = assetManager.loadTexture("Textures/Sky/down.bmp");

        Spatial sky = SkyFactory.createSky(assetManager, west, east, north, south, up, down);
        rootNode.attachChild(sky);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
       
        // Position jedes Fisches an
        float depthOffset = 0.2f; // Passe den Abstand an, um die Tiefe des Fisches im Wasser zu steuern
        float newY = waterHeight + depthOffset;
        model.setLocalTranslation(model.getLocalTranslation().setY(newY));



        model.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.PI / 2, Vector3f.UNIT_Y));
        
    

        flock.update(tpf); // Wird einmal pro Frame aufgerufen
    }
    
    @Override
    public void simpleRender(RenderManager rm) {
        // Füge hier benutzerdefinierte Rendering-Elemente hinzu, falls erforderlich
    }
}