package com.thrblock.aria.source;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.openal.AL;
import com.thrblock.aria.al.ALService;
import com.thrblock.aria.data.DataSource;

/**
 * 声源
 * 
 * @author zepu.li
 */
public class ALSource {
    private static final Logger LOG = LoggerFactory.getLogger(ALSource.class);

    static final int DEF_UPDATE_SEN = 16;

    private int[] source = new int[1];

    /**
     * Position of the source.
     */
    private float[] sourcePos = { 0.0f, 0.0f, 0.0f };

    /**
     * Velocity of the source.
     */
    private float[] sourceVel = { 0.0f, 0.0f, 0.0f };

    /**
     * Orientations of the source.
     */
    private float[] sourceOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };

    /**
     * state holder
     */
    private int[] state = new int[1];
    private AL al;

    private ALService alservice;
    private ALBufferQueue queue;

    private Queue<IALCmd> cmdQueue = new ConcurrentLinkedQueue<>();

    private enum St {
        STOPED, PLAYING, PAUSED, TERM
    }

    private St flag = St.STOPED;

    public ALSource(ALService alservice, boolean relative, int queueNum, int bufferSize) {
        buildALSource(alservice, relative);
        this.queue = new ALBufferQueue(alservice, this, queueNum, bufferSize);
        alservice.runInCommonsPool(this::run);
    }

    private void buildALSource(ALService alservice, boolean relative) {
        this.al = alservice.getAL();
        this.alservice = alservice;

        al.alGenSources(1, source, 0);

        al.alSourcefv(source[0], AL.AL_POSITION, sourcePos, 0);
        al.alSourcefv(source[0], AL.AL_VELOCITY, sourceVel, 0);
        al.alSourcefv(source[0], AL.AL_DIRECTION, sourceOri, 0);

        al.alSourcef(source[0], AL.AL_ROLLOFF_FACTOR, 0.0f);
        al.alSourcei(source[0], AL.AL_SOURCE_RELATIVE, relative ? AL.AL_TRUE : AL.AL_FALSE);
        alservice.check();
    }

    public void setPosition2D(float x, float y) {
        sourcePos[0] = x;
        sourcePos[1] = y;
    }

    public void setPostition3D(float x, float y, float z) {
        setPosition2D(x, y);
        sourcePos[2] = z;
    }

    public float getPostitonX() {
        return sourcePos[0];
    }

    public float getPostitonY() {
        return sourcePos[1];
    }

    public float getPostitonZ() {
        return sourcePos[2];
    }

    public void setVelocity2D(float x, float y) {
        sourceVel[0] = x;
        sourceVel[1] = y;
    }

    public void setVelocity3D(float x, float y, float z) {
        setPosition2D(x, y);
        sourceVel[2] = z;
    }

    public float getVelocityX() {
        return sourceVel[0];
    }

    public float getVelocityY() {
        return sourceVel[1];
    }

    public float getVelocityZ() {
        return sourceVel[2];
    }

    public void setOrientationAt(float x, float y, float z) {
        sourceOri[0] = x;
        sourceOri[1] = y;
        sourceOri[2] = z;
    }

    public void setOrientationUp(float x, float y, float z) {
        sourceOri[3] = x;
        sourceOri[4] = y;
        sourceOri[5] = z;
    }

    public float[] getSourcePos() {
        return sourcePos;
    }

    public float[] getSourceVel() {
        return sourceVel;
    }

    public float[] getSourceOri() {
        return sourceOri;
    }

    public int getSource() {
        return source[0];
    }

    public boolean isPlaying() {
        al.alGetSourcei(source[0], AL.AL_SOURCE_STATE, state, 0);
        return (state[0] == AL.AL_PLAYING);
    }

    private void run() {
        LOG.info("AL source started,tid:" + Thread.currentThread().getId());
        while (flag != St.TERM) {
            processCmdStep();
            if (flag == St.PLAYING) {
                boolean fillSuccess = queue.refillProcessedBuffer();
                if (!fillSuccess) {
                    waitPlayingFinished();
                    flag = St.STOPED;
                }
            }
            sleepQuietly(DEF_UPDATE_SEN);
        }
        LOG.info("AL source stopped,tid:" + Thread.currentThread().getId());
    }

    private void waitPlayingFinished() {
        while (this.isPlaying()) {
            sleepQuietly(DEF_UPDATE_SEN);
        }
        queue.unAttachAllBuffer();
    }

    private void processCmdStep() {
        if (!cmdQueue.isEmpty()) {
            IALCmd cmd = cmdQueue.poll();
            if (flag != St.TERM) {
                cmd.exec();
            }
        }
    }

    private void sleepQuietly(int milliSecond) {
        try {
            Thread.sleep(milliSecond);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.info("InterruptedException:" + e);
        }
    }

    public void initData(DataSource data) {
        cmdQueue.offer(() -> {
            if (flag == St.STOPED) {
                queue.unAttachAllBuffer();
                queue.attachData(data);
                queue.fillBufferFirstly();
                queue.attachAllBuffer();
                alservice.check();
            }
        });
    }

    public void play() {
        cmdQueue.offer(() -> {
            if (flag == St.STOPED || flag == St.PAUSED) {
                flag = St.PLAYING;
                al.alSourcePlay(getSource());
                alservice.check();
            }
        });
    }

    public void pause() {
        cmdQueue.offer(() -> {
            if (flag == St.PLAYING) {
                flag = St.PAUSED;
                al.alSourcePause(getSource());
                alservice.check();
            }
        });
    }

    public void stop() {
        cmdQueue.offer(() -> {
            if (flag == St.PLAYING || flag == St.PAUSED) {
                al.alSourceStop(getSource());
                waitPlayingFinished();
                alservice.check();
                flag = St.STOPED;
            }
        });
    }

    public void destroy() {
        cmdQueue.offer(() -> {
            al.alSourceStop(getSource());
            waitPlayingFinished();
            queue.destroy();
            al.alDeleteSources(1, source, 0);
            alservice.check();
            flag = St.TERM;
        });
    }
}
