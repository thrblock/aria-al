package com.thrblock.aria.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.openal.AL;
import com.thrblock.aria.decoder.SPIDecoder;

public class MusicData extends DataSource {
    private static final Logger LOG = LoggerFactory.getLogger(MusicData.class);

    private File srcFile;
    private SPIDecoder decoder;

    private AudioFormat decodedFormat;
    private InputStream audioInput;

    public MusicData(SPIDecoder decoder, File music) throws UnsupportedAudioFileException, IOException {
        this.srcFile = music;
        this.decoder = decoder;
        readyStream();
    }

    private void readyStream() throws UnsupportedAudioFileException, IOException {
        AudioInputStream srcInput = AudioSystem.getAudioInputStream(srcFile);
        AudioFormat baseFormat = srcInput.getFormat();
        this.decodedFormat = decoder.getDecodedAudioFormat(baseFormat);
        this.audioInput = decoder.getDecodedAudioInputStream(srcInput);
        this.format = decodedFormat.getChannels() == 1 ? AL.AL_FORMAT_MONO16 : AL.AL_FORMAT_STEREO16;
        this.rate = (int) decodedFormat.getSampleRate();
    }

    @Override
    public int fill(byte[] dst) {
        int count = -1;
        try {
            count = audioInput.read(dst, 0, dst.length);
            if (count < 0) {
                streamCloseQuietly(audioInput);
            }
        } catch (IOException e) {
            return -1;
        }
        return count;
    }

    private void streamCloseQuietly(AutoCloseable c) {
        try {
            c.close();
        } catch (Exception e) {
            LOG.info("IOException in close:" + e);
        }
    }
}
