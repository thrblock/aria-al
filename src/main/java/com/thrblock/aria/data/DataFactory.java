package com.thrblock.aria.data;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.thrblock.aria.al.ALService;
import com.thrblock.aria.data.music.MusicData;
import com.thrblock.aria.data.sound.SoundData;
import com.thrblock.aria.decoder.IDecoder;
import com.thrblock.aria.decoder.SPIDecoder;
import com.thrblock.aria.sound.AriaSoundException;

@Component
@Lazy(true)
public class DataFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DataFactory.class);

    @Autowired
    IDecoder decoder;

    @Autowired
    ALService alservice;

    public MusicData generateMusicData(File audioFile) throws AriaSoundException {
        MusicData music;
        try {
            music = new MusicData(new SPIDecoder(), audioFile);
        } catch (UnsupportedAudioFileException | IOException e) {
            LOG.error("Exception in generate MusicData:{}", e);
            throw new AriaSoundException(e);
        }
        return music;
    }

    public SoundData generateSoundData(File audioFile) throws AriaSoundException {
        return new SoundData(new SPIDecoder(), audioFile);
    }

}
