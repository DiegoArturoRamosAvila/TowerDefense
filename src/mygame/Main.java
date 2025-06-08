package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends SimpleApplication {
    private static Main instance;

    public static final List<AnimatedPlayer> players = new ArrayList<>();
    private final List<EnemyType> enemyTypes = new ArrayList<>();

    private float spawnAcceleration = 0.90f;

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
    private boolean enPausa = false;
    
    private AudioNode musicaFondo;
    private AudioNode sonidoNivelUp;
    private AudioNode sonidoDerrota;
    private AudioNode sonidoRisa;
    
    FrameAnimator fondoAnim;
    String[] fFrames = new String[9];
    private boolean animandoFondo = false;
    private float tiempoAnimacionFondo = 0f;
    private float duracionAnimacionFondo = 1.20f; 
    private Picture fondoBase;

    private final Map<String, Integer> enemyRewards = new HashMap<>();

    public static void main(String[] args) {
        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("....");
        settings.setResolution(1280, 720);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        instance = this;
        fondoBase = new Picture("Fondo");
        fondoBase.setImage(assetManager, "Textures/Fondo.png", false);
        fondoBase.setWidth(settings.getWidth());
        fondoBase.setHeight(settings.getHeight());
        guiNode.attachChild(fondoBase);
        fFrames[0] = "Textures/Fondo/Fondo_0.png";
        fFrames[1] = "Textures/Fondo/Fondo_1.png";
        fFrames[2] = "Textures/Fondo/Fondo_2.png";
        fFrames[3] = "Textures/Fondo/Fondo_3.png";
        fFrames[4] = "Textures/Fondo/Fondo_4.png";
        fFrames[5] = "Textures/Fondo/Fondo_5.png";
        fFrames[6] = "Textures/Fondo/Fondo_6.png";
        fFrames[7] = "Textures/Fondo/Fondo_7.png";
        fFrames[8] = "Textures/Fondo/Fondo_8.png";
        fondoAnim = new FrameAnimator(fondoBase, fFrames, assetManager, 6);
        
        float button = 30f; // Posición vertical fija para las imagenes de costo.
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

        // Castillos con hitbox virtual
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
        inputManager.addMapping("Pausa", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("lvlUp", new KeyTrigger(KeyInput.KEY_RSHIFT));
        inputManager.addListener(actionListener, "SpawnQ", "SpawnW", "SpawnE", "SpawnR", "Pausa", "lvlUp");
        
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
        
        musicaFondo = new AudioNode(assetManager, "Sounds/regular_battle.wav", DataType.Stream);
        musicaFondo.setLooping(true);
        musicaFondo.setPositional(false);
        musicaFondo.setVolume(1f);
        rootNode.attachChild(musicaFondo);
        musicaFondo.play();

        sonidoNivelUp = new AudioNode(assetManager, "Sounds/winneris.ogg", DataType.Buffer);
        sonidoNivelUp.setPositional(false);
        rootNode.attachChild(sonidoNivelUp);

        sonidoDerrota = new AudioNode(assetManager, "Sounds/death_bell_sound_effect.wav", DataType.Buffer);
        sonidoDerrota.setPositional(false);
        rootNode.attachChild(sonidoDerrota);
        
        sonidoRisa = new AudioNode(assetManager, "Sounds/risa.wav", DataType.Buffer);
        sonidoRisa.setPositional(false);
        rootNode.attachChild(sonidoRisa);
    }
    
    private void iniciarAnimacionFondoUnaVez() {
        animandoFondo = true;
        tiempoAnimacionFondo = 0f;
        fondoAnim.play();
        sonidoRisa.playInstance();
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
                    spawnAnimatedPlayer("Textures/CaminaGarrote/caminaGarrote_", "Textures/PegaGarrote/pegaGarrote_", 30, 214, 100, 20, "Garrote");
                    dinero -= 10;
                }
                break;
            case "SpawnE":
                if (dinero >= 15) {
                    spawnAnimatedPlayer("Textures/CaminaAntorcha/caminaAntorcha_", "Textures/PegaAntorcha/pegaAntorcha_", 30, 214, 140, 25, "Antorcha");
                    dinero -= 15;
                }
                break;
            case "SpawnR":
                if (dinero >= 30) {
                    spawnAnimatedPlayer("Textures/CaminaHorquilla/caminaHorquilla_", "Textures/PegaHorquilla/pegaHorquilla_", 30, 214, 220, 35, "Horquilla");
                    dinero -= 30;
                }
                break;
            case "Pausa":
                if(!enPausa){
                    enPausa = true;
                } else if(!playerCastle.isDestroyed()){
                    enPausa = false;
                }
                break;
            case "lvlUp":
                subirNivel();
                break;
                
        }
        textoDinero.setText("Dinero: " + dinero); // Actualiza el texto
    };

    private void spawnAnimatedPlayer(String caminaPrefix, String ataquePrefix, float startX, float startY, int vida, int ataque, String tipo) {
        AnimatedPlayer player = new AnimatedPlayer(assetManager, guiNode, caminaPrefix, ataquePrefix, 64, 64, startX, startY, 100f, false, vida, ataque, tipo);
        players.add(player);
    }

    private void spawnEnemyPlayer(String caminaPrefix, String ataquePrefix, float startX, float startY, int vida, int ataque, String tipo) {
        AnimatedPlayer enemy = new AnimatedPlayer(assetManager, guiNode, caminaPrefix, ataquePrefix, 64, 64, startX, startY, -100f, true, vida, ataque, tipo);
        enemy.setEndX(45f);
        players.add(enemy);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if(playerCastle.isDestroyed() || enPausa){
            if(playerCastle.isDestroyed()) fondoBase.setImage(assetManager, "Textures/FondoFin.png", false);
            return;
        }
        
        if (animandoFondo) {
            tiempoAnimacionFondo += tpf;
            fondoAnim.update(tpf);
            if (tiempoAnimacionFondo >= duracionAnimacionFondo) {
                animandoFondo = false;
                fondoAnim.pause();
                fondoBase.setImage(assetManager, "Textures/Fondo.png", false); // vuelve al fondo normal
            }
        }
        
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
                spawnEnemyPlayer(type.caminaPrefix, type.ataquePrefix, 1200, type.startY, type.vida, type.ataque, type.tipo);
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
        float ataqueCooldown = 1f;
        float ataqueTimer = 0f;
        float stoppedTime = 0f;

        public AnimatedPlayer(AssetManager assetManager, Node guiNode,
                              String caminaPrefix, String ataquePrefix,
                              float width, float height, float startX, float startY,
                              float speed, boolean isEnemy, int vida, int ataque, String tipo) {

            this.picture = new Picture("Player");
            picture.setImage(assetManager, caminaPrefix + "0.png", true);
            picture.setWidth(width);
            picture.setHeight(height);
            picture.setPosition(startX, startY);
            guiNode.attachChild(picture);

            this.speed = speed;
            this.isEnemy = isEnemy;
            this.vida = vida;
            this.ataque = ataque;
            this.tipo = tipo;

            animations.put("camina", new FrameAnimator(picture, generateFrames(caminaPrefix), assetManager, 8));
            animations.put("ataque", new FrameAnimator(picture, generateFrames(ataquePrefix), assetManager, 8));
            currentAnimator = animations.get("camina");
            currentAnimationName = "camina";
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
                    ataqueCastle();
                    picture.removeFromParent();
                    Main.players.remove(this);
                    return;
                }
            } else {
                stoppedTime += tpf;
                ataqueTimer += tpf;

                if (ataqueTimer >= ataqueCooldown) {
                    ataqueNearby();
                    ataqueTimer = 0f;
                }
            }
            currentAnimator.update(tpf);
        }

        private void ataqueCastle() {
            if (isEnemy) {
                Main.playerCastle.takeDamage(ataque);
            } else {
                Main.enemyCastle.takeDamage(ataque);
            }
        }

        private void ataqueNearby() {
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
                AudioMuerte.playRandomDeathSound(instancia.assetManager, instancia.rootNode);
            }
        }

        public void stop() {
            isStopped = true;
            stoppedTime = 0f;
            if(!playerCastle.isDestroyed()){
                setAnimation("ataque");
            }
        }

        public void resume() {
            isStopped = false;
            ataqueTimer = 0f;
            setAnimation("camina");
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
        String caminaPrefix, ataquePrefix, tipo;
        float startY, spawnInterval, spawnIntervalOriginal, timer = 0;
        int vida, ataque;

        public EnemyType(String caminaPrefix, String ataquePrefix, float startY, float spawnInterval, int vida, int ataque, String tipo) {
            this.caminaPrefix = caminaPrefix;
            this.ataquePrefix = ataquePrefix;
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
        musicaFondo.pause();
        sonidoNivelUp.playInstance();
        fondoAnim.play();

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

        enemyCastle.vidaMax += 50;
        dinero = 0;
        intervaloDinero = (float) (intervaloDinero - 0.05);
    }
    
    private void mostrarMensajeNivelSuperado() {
        mensajeNivelSuperado = new BitmapText(guiFont, false);
        mensajeNivelSuperado.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        mensajeNivelSuperado.setColor(ColorRGBA.Yellow);
        mensajeNivelSuperado.setText("¡Nivel Superado!");
        mensajeNivelSuperado.setLocalTranslation(settings.getWidth() / 2 - mensajeNivelSuperado.getLineWidth() / 2,
                                                 settings.getHeight() / 2, 0);
        guiNode.attachChild(mensajeNivelSuperado);
        
        iniciarAnimacionFondoUnaVez();
        
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                enqueue(() -> {
                    guiNode.detachChild(mensajeNivelSuperado);
                    musicaFondo.play();
                    return null;
                });
            }
        }, 3000);
    }
    
    private void mostrarMensajeDerrota() {
        musicaFondo.stop();
        sonidoDerrota.playInstance();
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
                    stop();
                    System.exit(0);
                    return null;
                });
            }
        }, 3500);
    }
    
    public class AudioMuerte {
        private static final String[] deathSounds = {
            "Sounds/Death/death1.wav",
            "Sounds/Death/death2.wav",
            "Sounds/Death/death3.wav",
            "Sounds/Death/death4.wav",
            "Sounds/Death/death5.wav",
            "Sounds/Death/death6.wav",
            "Sounds/Death/death7.wav",
            "Sounds/Death/death8.wav",
            "Sounds/Death/death9.wav",
            "Sounds/Death/death10.wav",
            "Sounds/Death/death11.wav"
        };

        private static final Random rand = new Random();

        public static void playRandomDeathSound(AssetManager assetManager, Node rootNode) {
            int index = rand.nextInt(deathSounds.length);
            AudioNode deathSound = new AudioNode(assetManager, deathSounds[index], AudioData.DataType.Buffer);
            deathSound.setPositional(false);
            deathSound.setLooping(false);
            deathSound.setVolume(0.4f);
            rootNode.attachChild(deathSound);
            deathSound.play();
        }
    }
}