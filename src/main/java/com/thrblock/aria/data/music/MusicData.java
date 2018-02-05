package com.thrblock.aria.data.music;

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
import com.thrblock.aria.data.DataSource;
import com.thrblock.aria.decoder.IDecoder;

public class MusicData implements DataSource {
    private static final Logger LOG = LoggerFactory.getLogger(MusicData.class);

    private File srcFile;
    private IDecoder decoder;

    private InputStream audioInput;
    private boolean autoReload = false;

    private AudioFormat decodedFormat;

    private boolean destroyed = false;

    public MusicData(IDecoder decoder, File music) throws UnsupportedAudioFileException, IOException {
        this.srcFile = music;
        this.decoder = decoder;
        readyStream();
    }

    public void destroy() {
        destroyed = true;
        streamCloseQuietly(audioInput);
    }

    private void readyStream() throws UnsupportedAudioFileException, IOException {
        AudioInputStream srcInput = AudioSystem.getAudioInputStream(srcFile);
        AudioFormat baseFormat = srcInput.getFormat();
        this.decodedFormat = decoder.getDecodedAudioFormat(baseFormat);
        this.audioInput = decoder.getDecodedAudioInputStream(srcInput);
    }

    public void reload() throws UnsupportedAudioFileException, IOException {
        streamCloseQuietly(audioInput);
        readyStream();
    }

    public boolean isAutoReload() {
        return autoReload;
    }

    public void setAutoReload(boolean autoReload) {
        this.autoReload = autoReload;
    }

    @Override
    public int fill(byte[] dst) {
        if (destroyed) {
            streamCloseQuietly(audioInput);
            return -1;
        }
        int count = -1;
        try {
            count = audioInput.read(dst, 0, dst.length);
            if (count < 0) {
                streamCloseQuietly(audioInput);
                count = doReload(dst);
            }
        } catch (IOException e) {
            return -1;
        }
        return count;
    }

    private int doReload(byte[] dst) throws IOException {
        if (autoReload) {
            try {
                readyStream();
                return audioInput.read(dst, 0, dst.length);
            } catch (UnsupportedAudioFileException e) {
                LOG.warn("auto reload fail:" + e);
            }
        }
        return -1;
    }

    private void streamCloseQuietly(AutoCloseable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (Exception e) {
            LOG.info("IOException in close:" + e);
        }
    }

    @Override
    public int getFormat() {
        if (decodedFormat == null) {
            return -1;
        }
        return decodedFormat.getChannels() == 1 ? AL.AL_FORMAT_MONO16 : AL.AL_FORMAT_STEREO16;
    }

    @Override
    public int getRate() {
        if (decodedFormat == null) {
            return -1;
        }
        return (int) decodedFormat.getSampleRate();
    }
}
