package com.thrblock.aria.altest;

import java.io.File;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.thrblock.aria.data.DataFactory;
import com.thrblock.aria.data.music.MusicData;
import com.thrblock.aria.data.sound.SoundData;
import com.thrblock.aria.sound.AriaSoundException;
import com.thrblock.aria.source.ALSource;
import com.thrblock.aria.source.ALSourceFactory;

public class TestWithSpringContext {
    private static AbstractApplicationContext context;
    static {
        context = new ClassPathXmlApplicationContext("aria-al-context.xml");
        context.registerShutdownHook();
    }

    public static void main(String[] args) throws AriaSoundException {
        ALSourceFactory sourceFactory = context.getBean(ALSourceFactory.class);
        ALSource source = sourceFactory.generateALSource();
        DataFactory dataFactory = context.getBean(DataFactory.class);
        
        SoundData sound = dataFactory.generateSoundData(new File("D:\\work\\GoldWave\\MusicWorkSpace\\sfx_point.wav"));
        source.initData(sound.createLoopSource(5));
        source.play();
        
        playMusic(source, dataFactory);
    }

    private static void playMusic(ALSource source, DataFactory dataFactory) throws AriaSoundException {
        MusicData music = dataFactory.generateMusicData(new File(
                "D:\\CloudMusic\\Andreas Waldetoft\\Stellaris Original Soundtrack\\Andreas Waldetoft,Mia Stegmar - Faster Than Light.mp3"));
        source.initData(music);
        source.play();
        source.pause();
        source.play();
        source.destroy();
    }
    
    
}
