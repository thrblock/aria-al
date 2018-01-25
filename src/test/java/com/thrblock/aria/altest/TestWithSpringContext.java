package com.thrblock.aria.altest;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.thrblock.aria.data.MusicData;
import com.thrblock.aria.decoder.SPIDecoder;
import com.thrblock.aria.source.ALSource;
import com.thrblock.aria.source.ALSourceFactory;

public class TestWithSpringContext {
    private static AbstractApplicationContext context;
    static {
        context = new ClassPathXmlApplicationContext("aria-al-context.xml");
        context.registerShutdownHook();
    }

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, InterruptedException {
        ALSourceFactory sourceFactory = context.getBean(ALSourceFactory.class);
        ALSource source = sourceFactory.generateALSource();
        MusicData music = new MusicData(new SPIDecoder(),new File("D:\\CloudMusic\\Andreas Waldetoft\\Stellaris Synthetic Dawn Soundtrack\\Andreas Waldetoft - Robo Sapiens.mp3"));
        source.initData(music);
        source.play();
        source.pause();
        source.play();
        source.destroy();
    }
}
