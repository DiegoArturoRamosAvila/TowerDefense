package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.ui.Picture;

public class FrameAnimator {

    private final Picture target;
    private final String[] frames;
    private final AssetManager assetManager;
    private final float frameDuration;
    private int currentFrame = 0;
    private float time = 0f;
    private boolean playing = false;

    public FrameAnimator(Picture target, String[] frames, AssetManager assetManager, float fps) {
        this.target = target;
        this.frames = frames;
        this.assetManager = assetManager;
        this.frameDuration = 1f / fps;
    }

    public void play() {
        playing = true;
        currentFrame = 0;
        time = 0f;
    }

    public void pause() {
        playing = false;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void update(float tpf) {
        if (!playing) return;

        time += tpf;
        if (time >= frameDuration) {
            time -= frameDuration;
            currentFrame++;

            if (currentFrame >= frames.length) {
                currentFrame = 0; // 🔁 Reinicia la animación
            }

            target.setImage(assetManager, frames[currentFrame], true);
        }
    }
}
