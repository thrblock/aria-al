package com.thrblock.aria.al;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.jogamp.openal.AL;

@Component
@Lazy(true)
public class ALListener {
    /**
     * Position of the listener.
     */
    private float[] listenerPos = { 0.0f, 0.0f, 0.0f };

    /**
     * Velocity of the listener.
     */
    private float[] listenerVel = { 0.0f, 0.0f, 0.0f };

    /**
     * 听众的朝向. (前三个参数表示“脸”的正对方向,后三个参数表示“头顶”方向)[原文为first 3 elements are "at", second 3
     * are "up"]
     */
    private float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };

    @Autowired
    private ALService alservice;

    private AL al;
    
    /**
     * Spring use only
     */
    ALListener() {
    }
    
    public ALListener(ALService service) {
        this.alservice = service;
    }
    
    @PostConstruct
    public void init() {
        this.al = alservice.getAL();
        al.alListenerfv(AL.AL_POSITION, listenerPos, 0);
        al.alListenerfv(AL.AL_VELOCITY, listenerVel, 0);
        al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
    }
    
    public void setPosition2D(float x, float y) {
        listenerPos[0] = x;
        listenerPos[1] = y;
        al.alListenerfv(AL.AL_POSITION, listenerPos, 0);
    }

    public void setPostition3D(float x, float y, float z) {
        listenerPos[2] = z;
        setPosition2D(x, y);
    }

    public float getPostitonX() {
        return listenerPos[0];
    }

    public float getPostitonY() {
        return listenerPos[1];
    }

    public float getPostitonZ() {
        return listenerPos[2];
    }

    public void setVelocity2D(float x, float y) {
        listenerVel[0] = x;
        listenerVel[1] = y;
        al.alListenerfv(AL.AL_VELOCITY, listenerVel, 0);
    }

    public void setVelocity3D(float x, float y, float z) {
        listenerVel[2] = z;
        setPosition2D(x, y);
    }

    public float getVelocityX() {
        return listenerVel[0];
    }

    public float getVelocityY() {
        return listenerVel[1];
    }

    public float getVelocityZ() {
        return listenerVel[2];
    }

    public void setOrientationAt(float x, float y, float z) {
        listenerOri[0] = x;
        listenerOri[1] = y;
        listenerOri[2] = z;
        al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
    }

    public void setOrientationUp(float x, float y, float z) {
        listenerOri[3] = x;
        listenerOri[4] = y;
        listenerOri[5] = z;
        al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
    }

    public float[] getListenerPos() {
        return listenerPos;
    }

    public float[] getListenerVel() {
        return listenerVel;
    }

    public float[] getListenerOri() {
        return listenerOri;
    }
}
