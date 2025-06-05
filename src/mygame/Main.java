package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends SimpleApplication {
    private static Main instance;

    public static final List<AnimatedPlayer> players = new ArrayList<>();
    private final List<EnemyType> enemyTypes = new ArrayList<>();

    private float spawnAcceleration = 0.10f;

    private static Castle playerCastle;
    private static Castle enemyCastle;
    
    private int dinero = 0;
    private float dineroTimer = 0f;
    private float intervaloDinero = 1f; 
    private BitmapText textoDinero;
    
    private int nivelActual = 1;
    private BitmapText textoNivel;
    private BitmapText mensajeNivelSuperado;
    private BitmapText mensajeDerrota;

    
    private final Map<String, Integer> enemyRewards = new HashMap<>();

    public static void main(String[] args) {
        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Tower Defense");
        settings.setResolution(1280, 720);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        instance = this;
        Picture bg = new Picture("Fondo");
        bg.setImage(assetManager, "Textures/Fondo.png", false);
        bg.setWidth(settings.getWidth());
        bg.setHeight(settings.getHeight());
        guiNode.attachChild(bg);
        
        float button = 30f; // Posición vertical fija
        Picture qPic = new Picture("Q");
        qPic.setImage(assetManager, "Textures/RocaPrecio.png", true);
        qPic.setWidth(128);
        qPic.setHeight(128);
        qPic.setPosition(50, button);
        guiNode.attachChild(qPic);

        Picture wPic = new Picture("W");
        wPic.setImage(assetManager, "Textures/GarrotePrecio.png", true);
        wPic.setWidth(128);
        wPic.setHeight(128);
        wPic.setPosition(200, button);
        guiNode.attachChild(wPic);

        Picture ePic = new Picture("E");
        ePic.setImage(assetManager, "Textures/AntorchaPrecio.png", true);
        ePic.setWidth(128);
        ePic.setHeight(128);
        ePic.setPosition(350, button);
        guiNode.attachChild(ePic);

        Picture rPic = new Picture("R");
        rPic.setImage(assetManager, "Textures/HorquillaPrecio.png", true);
        rPic.setWidth(128);
        rPic.setHeight(128);
        rPic.setPosition(500, button);
        guiNode.attachChild(rPic);
        
        guiFont = assetManager.loadFont("Interface/Fonts/PixelFont.fnt");
        textoDinero = new BitmapText(guiFont, false);
        textoDinero.setSize(guiFont.getCharSet().getRenderedSize());
        textoDinero.setColor(ColorRGBA.White);
        textoDinero.setLocalTranslation(1000, 40, 0);
        textoDinero.setText("Dinero: 0");
        guiNode.attachChild(textoDinero);

        // Inicializar castillos con hitbox virtual
        playerCastle = new Castle(assetManager, guiNode, 25, 50, 500, false);
        enemyCastle = new Castle(assetManager, guiNode, 1150, 50, 35, true);

        enemyTypes.add(new EnemyType("Textures/CaminaRocaMalo/caminaRocaMalo_", "Textures/PegaRocaMalo/pegaRocaMalo_", 214, 5f, 20, 5, "Roca"));
        enemyTypes.add(new EnemyType("Textures/CaminaGarroteMalo/caminaGarroteMalo_", "Textures/PegaGarroteMalo/pegaGarroteMalo_", 214, 15f, 40, 10, "Garrote"));
        enemyTypes.add(new EnemyType("Textures/CaminaAntorchaMalo/caminaAntorchaMalo_", "Textures/PegaAntorchaMalo/pegaAntorchaMalo_", 214, 30f, 60, 15, "Antorcha"));
        enemyTypes.add(new EnemyType("Textures/CaminaHorquillaMalo/caminaHorquillaMalo_", "Textures/PegaHorquillaMalo/pegaHorquillaMalo_", 214, 60f, 100, 25, "Horquilla"));

        inputManager.addMapping("SpawnQ", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("SpawnW", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("SpawnE", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("SpawnR", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addListener(actionListener, "SpawnQ", "SpawnW", "SpawnE", "SpawnR");
        
        enemyRewards.put("Roca", 0);
        enemyRewards.put("Garrote", 5);
        enemyRewards.put("Antorcha", 10);
        enemyRewards.put("Horquilla", 15);
        
        textoNivel = new BitmapText(guiFont, false);
        textoNivel.setSize(guiFont.getCharSet().getRenderedSize());
        textoNivel.setColor(ColorRGBA.White);
        textoNivel.setText("Nivel: " + nivelActual);
        textoNivel.setLocalTranslation(settings.getWidth() - 200, settings.getHeight() - 20, 0);
        guiNode.attachChild(textoNivel);
    }
    
    public static Main instance() {
        return instance;
    }

    private final ActionListener actionListener = (name, isPressed, tpf) -> {
        if (!isPressed) return;
        switch (name) {
            case "SpawnQ":
                if (dinero >= 5) {
                    spawnAnimatedPlayer("Textures/CaminaRoca/caminaRoca_", "Textures/PegaRoca/pegaRoca_", 30, 214, 60, 15, "Roca");
                    dinero -= 5;
                }
                break;
            case "SpawnW":
                if (dinero >= 10) {
                    spawnAnimatedPlayer("Textures/CaminaGarrote/caminaGarrote_", "Textures/PegaGarrote/pegaGarrote_", 30, 214, 80, 20, "Garrote");
                    dinero -= 10;
                }
                break;
            case "SpawnE":
                if (dinero >= 15) {
                    spawnAnimatedPlayer("Textures/CaminaAntorcha/caminaAntorcha_", "Textures/PegaAntorcha/pegaAntorcha_", 30, 214, 100, 25, "Antorcha");
                    dinero -= 15;
                }
                break;
            case "SpawnR":
                if (dinero >= 30) {
                    spawnAnimatedPlayer("Textures/CaminaHorquilla/caminaHorquilla_", "Textures/PegaHorquilla/pegaHorquilla_", 30, 214, 140, 35, "Horquilla");
                    dinero -= 30;
                }
                break;
        }
        textoDinero.setText("Dinero: " + dinero); // Actualiza el texto
    };

    private void spawnAnimatedPlayer(String idlePrefix, String attackPrefix, float startX, float startY, int vida, int ataque, String tipo) {
        AnimatedPlayer player = new AnimatedPlayer(assetManager, guiNode, idlePrefix, attackPrefix, 64, 64, startX, startY, 100f, false, vida, ataque, tipo);
        players.add(player);
    }

    private void spawnEnemyPlayer(String idlePrefix, String attackPrefix, float startX, float startY, int vida, int ataque, String tipo) {
        AnimatedPlayer enemy = new AnimatedPlayer(assetManager, guiNode, idlePrefix, attackPrefix, 64, 64, startX, startY, -100f, true, vida, ataque, tipo);
        enemy.setEndX(45f);
        players.add(enemy);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if(playerCastle.isDestroyed()) return;
        
        // Generar dinero automáticamente
        dineroTimer += tpf;
        if (dineroTimer >= intervaloDinero) {
            dinero += 1;
            dineroTimer = 0f;
            textoDinero.setText("Dinero: " + dinero);
        }
        
        checkForObstacles();

        Iterator<AnimatedPlayer> iterator = new ArrayList<>(players).iterator();
        while (iterator.hasNext()) {
            AnimatedPlayer p = iterator.next();
            p.update(tpf);
        }

        for (EnemyType type : enemyTypes) {
            type.timer += tpf;
            if (type.timer >= type.spawnInterval) {
                spawnEnemyPlayer(type.idlePrefix, type.attackPrefix, 1200, type.startY, type.vida, type.ataque, type.tipo);
                type.timer = 0;
            }
        }

        // Verificar victoria o derrota
        if (playerCastle.isDestroyed()) {
            mostrarMensajeDerrota();
            System.out.println("¡Has perdido!");
        } else if (enemyCastle.isDestroyed()) {
            subirNivel();
        }
    }

    private void checkForObstacles() {
        for (int i = 0; i < players.size(); i++) {
            AnimatedPlayer current = players.get(i);
            boolean shouldStop = false;

            for (int j = 0; j < players.size(); j++) {
                if (i == j) continue;
                AnimatedPlayer other = players.get(j);
                if (current.isEnemy == other.isEnemy) continue;

                if (Math.abs(current.picture.getLocalTranslation().y - other.picture.getLocalTranslation().y) < 10) {
                    float dx = other.picture.getLocalTranslation().x - current.picture.getLocalTranslation().x;
                    if (current.speed > 0 && dx > 0 && dx < 25) {
                        shouldStop = true;
                        break;
                    }
                    if (current.speed < 0 && dx < 0 && dx > -25) {
                        shouldStop = true;
                        break;
                    }
                }
            }

            if (shouldStop) {
                current.stop();
            } else if (current.isStopped()) {
                current.resume();
            }
        }
    }

    private static class Castle {
        int vida, vidaMax;
        float x, y;
        boolean isEnemy;
        Geometry barraVida;

        public Castle(AssetManager assetManager, Node guiNode, float x, float y, int vida, boolean isEnemy) {
            this.x = x;
            this.y = y;
            this.vida = vida;
            this.vidaMax = vida;
            this.isEnemy = isEnemy;

            Geometry fondoBarra = createHealthBarBackground(assetManager);
            barraVida = createHealthBar(assetManager);
            guiNode.attachChild(fondoBarra);
            guiNode.attachChild(barraVida);
            updateHealthBar();
        }

        private Geometry createHealthBarBackground(AssetManager assetManager) {
            Quad q = new Quad(102, 12); 
            Geometry g = new Geometry("HealthBarBG", q);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Black);
            g.setMaterial(mat);
            g.setLocalTranslation(x - 1, y + 414, 0);
            return g;
        }

        private Geometry createHealthBar(AssetManager assetManager) {
            Quad q = new Quad(100, 10);
            Geometry g = new Geometry("HealthBar", q);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Red);
            g.setMaterial(mat);
            g.setLocalTranslation(x, y + 415, 1);
            return g;
        }


        public void takeDamage(int amount) {
            vida -= amount;
            if (vida < 0) vida = 0;
            updateHealthBar();
        }

        private void updateHealthBar() {
            float width = (vida / (float) vidaMax) * 100f;
            barraVida.setLocalScale(width / 100f, 1, 1);
        }

        public boolean isDestroyed() {
            return vida <= 0;
        }
    }

    private static class AnimatedPlayer {
        Picture picture;
        Map<String, FrameAnimator> animations = new HashMap<>();
        FrameAnimator currentAnimator;
        String currentAnimationName;
        float speed, endX = 1125f;
        boolean isStopped = false, isEnemy;
        String tipo;

        int vida;
        int ataque;
        float attackCooldown = 1f;
        float attackTimer = 0f;
        float stoppedTime = 0f;

        public AnimatedPlayer(AssetManager assetManager, Node guiNode,
                              String idlePrefix, String attackPrefix,
                              float width, float height, float startX, float startY,
                              float speed, boolean isEnemy, int vida, int ataque, String tipo) {

            this.picture = new Picture("Player");
            picture.setImage(assetManager, idlePrefix + "0.png", true);
            picture.setWidth(width);
            picture.setHeight(height);
            picture.setPosition(startX, startY);
            guiNode.attachChild(picture);

            this.speed = speed;
            this.isEnemy = isEnemy;
            this.vida = vida;
            this.ataque = ataque;
            this.tipo = tipo;

            animations.put("idle", new FrameAnimator(picture, generateFrames(idlePrefix), assetManager, 8));
            animations.put("attack", new FrameAnimator(picture, generateFrames(attackPrefix), assetManager, 8));
            currentAnimator = animations.get("idle");
            currentAnimationName = "idle";
            currentAnimator.play();
        }

        private String[] generateFrames(String prefix) {
            String[] frames = new String[8];
            for (int i = 0; i < frames.length; i++) frames[i] = prefix + i + ".png";
            return frames;
        }

        public void update(float tpf) {
            if (!isStopped) {
                picture.move(speed * tpf, 0, 0);
                if (hasReachedEnd()) {
                    attackCastle();
                    picture.removeFromParent();
                    Main.players.remove(this);
                    return;
                }
            } else {
                stoppedTime += tpf;
                attackTimer += tpf;

                if (attackTimer >= attackCooldown) {
                    attackNearby();
                    attackTimer = 0f;
                }
            }
            currentAnimator.update(tpf);
        }

        private void attackCastle() {
            if (isEnemy) {
                Main.playerCastle.takeDamage(ataque);
            } else {
                Main.enemyCastle.takeDamage(ataque);
            }
        }

        private void attackNearby() {
            for (AnimatedPlayer other : Main.players) {
                if (other == this || other.isEnemy == this.isEnemy) continue;

                float dx = other.picture.getLocalTranslation().x - this.picture.getLocalTranslation().x;
                float dy = Math.abs(other.picture.getLocalTranslation().y - this.picture.getLocalTranslation().y);

                if (dy < 10 && Math.abs(dx) < 30) {
                    other.takeDamage(this.ataque);
                    break;
                }
            }
        }

        public void takeDamage(int amount) {
            vida -= amount;
            if (vida <= 0) {
                picture.removeFromParent();
                Main.players.remove(this);
                Main instancia = Main.instance();
                if (isEnemy) {
                    Integer recompensa = Main.instance().enemyRewards.getOrDefault(tipo, 0);
                    Main.instance().agregarDinero(recompensa);
                }
            }
        }

        public void stop() {
            isStopped = true;
            stoppedTime = 0f;
            if(!playerCastle.isDestroyed()){
                setAnimation("attack");
            }
        }

        public void resume() {
            isStopped = false;
            attackTimer = 0f;
            setAnimation("idle");
        }

        public void setAnimation(String name) {
            if (!animations.containsKey(name) || name.equals(currentAnimationName)) return;
            currentAnimator.pause();
            currentAnimator = animations.get(name);
            currentAnimationName = name;
            currentAnimator.play();
        }

        public boolean hasReachedEnd() {
            if (speed == 0f) return false;
            return (speed > 0 && picture.getLocalTranslation().x >= 1165f) ||
                   (speed < 0 && picture.getLocalTranslation().x <= 45f);
        }

        public void setEndX(float endX) {
            this.endX = endX;
        }

        public boolean isStopped() {
            return isStopped;
        }
    }

    private static class EnemyType {
        String idlePrefix, attackPrefix, tipo;
        float startY, spawnInterval, spawnIntervalOriginal, timer = 0;
        int vida, ataque;

        public EnemyType(String idlePrefix, String attackPrefix, float startY, float spawnInterval, int vida, int ataque, String tipo) {
            this.idlePrefix = idlePrefix;
            this.attackPrefix = attackPrefix;
            this.startY = startY;
            this.spawnInterval = spawnInterval;
            this.spawnIntervalOriginal = spawnInterval; // guarda el valor inicial
            this.vida = vida;
            this.ataque = ataque;
            this.tipo = tipo;
        }
    }
    
    public void agregarDinero(int cantidad) {
        dinero += cantidad;
        textoDinero.setText("Dinero: " + dinero);
    }
    
    private void subirNivel() {
        nivelActual++;
        textoNivel.setText("Nivel: " + nivelActual);

        // Reiniciar vida de las torres
        playerCastle.vida = playerCastle.vidaMax;
        playerCastle.updateHealthBar();
        enemyCastle.vida = enemyCastle.vidaMax;
        enemyCastle.updateHealthBar();

        // Limpiar tropas
        for (AnimatedPlayer p : new ArrayList<>(players)) {
            p.picture.removeFromParent();
        }
        players.clear();
        
        mostrarMensajeNivelSuperado();
        
        //Aumentar recompensas
        if(nivelActual >= 5){
            for (Map.Entry<String, Integer> entry : enemyRewards.entrySet()) {
                int nuevaRecompensa = (int)(entry.getValue() + 1); 
                enemyRewards.put(entry.getKey(), nuevaRecompensa);
            }
        }
        
        for (EnemyType type : enemyTypes) {
            type.spawnInterval *= spawnAcceleration;
            if (type.spawnInterval < 0.5f) type.spawnInterval = 0.5f;
        }

        for (EnemyType type : enemyTypes) {
            type.vida += 8;
            type.ataque += 2;
            type.timer = 0;
        }

        enemyCastle.vida += 50;
        enemyCastle.vidaMax += 50;
        dinero = 0;
        intervaloDinero = (float) (intervaloDinero - 0.02);
    }
    
    private void mostrarMensajeNivelSuperado() {
        mensajeNivelSuperado = new BitmapText(guiFont, false);
        mensajeNivelSuperado.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        mensajeNivelSuperado.setColor(ColorRGBA.Yellow);
        mensajeNivelSuperado.setText("¡Nivel Superado!");
        mensajeNivelSuperado.setLocalTranslation(settings.getWidth() / 2 - mensajeNivelSuperado.getLineWidth() / 2,
                                                 settings.getHeight() / 2, 0);
        guiNode.attachChild(mensajeNivelSuperado);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                enqueue(() -> {
                    guiNode.detachChild(mensajeNivelSuperado);
                    return null;
                });
            }
        }, 3000);
    }
    
    private void mostrarMensajeDerrota() {
        mensajeDerrota = new BitmapText(guiFont, false);
        mensajeDerrota.setSize(guiFont.getCharSet().getRenderedSize() * 2); 
        mensajeDerrota.setColor(ColorRGBA.Red);
        mensajeDerrota.setText("¡Has Perdido!");
        mensajeDerrota.setLocalTranslation(settings.getWidth() / 2 - mensajeDerrota.getLineWidth() / 2,
                                           settings.getHeight() / 2, 0);
        guiNode.attachChild(mensajeDerrota);
        
        players.clear();
        
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                enqueue(() -> {
                    guiNode.detachChild(mensajeDerrota);
                    stop();
                    return null;
                });
            }
        }, 5000);
    }

}