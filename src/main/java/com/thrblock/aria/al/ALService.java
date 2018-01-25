package com.thrblock.aria.al;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;
import com.jogamp.openal.ALCcontext;
import com.jogamp.openal.ALCdevice;
import com.jogamp.openal.ALFactory;

@Component
public class ALService {
    /**
     * This is the logger
     * <p>
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(ALService.class);
    private AL al;
    private ALC alc;
    private ExecutorService commonsPool;
    private List<Runnable> destroyHolders = new LinkedList<>();

    @PostConstruct
    public void init() {
        al = ALFactory.getAL();
        alc = ALFactory.getALC();
        ALCdevice device;
        ALCcontext context;
        String deviceSpecifier;
        String deviceName = "DirectSound3D"; // 你可以指定一个其它的设备
        deviceName = null; // 使用null可以获得系统默认的设备

        // 得到设备句柄
        device = alc.alcOpenDevice(deviceName);

        // 获得设备标识符
        deviceSpecifier = alc.alcGetString(device, ALC.ALC_DEVICE_SPECIFIER);
        LOG.info("Using AL device :" + deviceSpecifier);

        // 创建音频上下文
        context = alc.alcCreateContext(device, null);

        // 将其设置为当前上下文
        alc.alcMakeContextCurrent(context);

        check();
        commonsPool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setName("AriaAL-" + t.getId());
            return t;
        });
    }

    public void onDestroy(Runnable r) {
        this.destroyHolders.add(r);
    }

    public void runInCommonsPool(Runnable r) {
        commonsPool.execute(r);
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        destroyHolders.forEach(Runnable::run);
        destroyHolders.clear();

        // 获得当前上下文
        ALCcontext curContext = alc.alcGetCurrentContext();
        // 由上下文获得设备
        ALCdevice curDevice = alc.alcGetContextsDevice(curContext);

        // 重置当前上下文为空
        alc.alcMakeContextCurrent(null);

        // 释放上下文及设备
        alc.alcDestroyContext(curContext);
        alc.alcCloseDevice(curDevice);

        commonsPool.shutdown();
        commonsPool.awaitTermination(3, TimeUnit.SECONDS);
    }

    public AL getAL() {
        return al;
    }

    public void check() {
        if (al.alGetError() != AL.AL_NO_ERROR) {
            LOG.warn("AL Error:" + getALErrorStr());
        }
    }

    public String getALErrorStr() {
        return translateALErrorCode(al.alGetError());
    }

    public String translateALErrorCode(int err) {
        switch (err) {
        case AL.AL_NO_ERROR:
            return "AL_NO_ERROR";
        case AL.AL_INVALID_NAME:
            return "AL_INVALID_NAME";
        case AL.AL_INVALID_ENUM:
            return "AL_INVALID_ENUM";
        case AL.AL_INVALID_VALUE:
            return "AL_INVALID_VALUE";
        case AL.AL_INVALID_OPERATION:
            return "AL_INVALID_OPERATION";
        case AL.AL_OUT_OF_MEMORY:
            return "AL_OUT_OF_MEMORY";
        default:
            return "AL_UNKNOW_ERROR";
        }
    }
}
