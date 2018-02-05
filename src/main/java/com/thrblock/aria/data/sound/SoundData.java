package com.thrblock.aria.data.sound;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BooleanSupplier;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.thrblock.aria.data.DataSource;
import com.thrblock.aria.decoder.IDecoder;
import com.thrblock.aria.sound.AriaSoundException;

public class SoundData {
    private byte[] decodedSource;
    private AudioFormat decodedFormat;

    public SoundData(IDecoder decoder, File f) throws AriaSoundException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
            buildSound(decoder, bis);
        } catch (IOException e) {
            throw new AriaSoundException(e);
        }
    }

    public SoundData(IDecoder decoder, InputStream src) throws AriaSoundException {
        buildSound(decoder, src);
    }

    private void buildSound(IDecoder decoder, InputStream src) throws AriaSoundException {
        try (AudioInputStream srcInput = AudioSystem.getAudioInputStream(src)) {
            AudioFormat baseFormat = srcInput.getFormat();
            this.decodedFormat = decoder.getDecodedAudioFormat(baseFormat);
            AudioInputStream decodedStream = decoder.getDecodedAudioInputStream(srcInput);
            byte[] loadCache = new byte[1024];
            ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
            for (int realRead = 0; realRead != -1; realRead = decodedStream.read(loadCache, 0, loadCache.length)) {
                byteOS.write(loadCache, 0, realRead);
            }
            this.decodedSource = byteOS.toByteArray();
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new AriaSoundException(e);
        }
    }

    public DataSource createSharedSource(ByteCopyStrategy sharedStrategy) {
        return new SharedSoundData(decodedFormat, decodedSource, sharedStrategy);
    }

    public DataSource createDisposableSource() {
        int[] copyReg = new int[1];
        copyReg[0] = 0;
        return createSharedSource((s, d) -> {
            if (copyReg[0] < s.length) {
                int remain = s.length - copyReg[0];
                int realCopy = remain > d.length ? d.length : remain;
                System.arraycopy(s, copyReg[0], d, 0, realCopy);
                copyReg[0] += realCopy;
                return realCopy;
            }
            return -1;
        });
    }

    public DataSource createLoopSource(int loopCount) {
        int[] loopReg = { loopCount };
        int[] copyReg = new int[1];
        copyReg[0] = 0;
        return createSharedSource((s, d) -> {
            if (copyReg[0] < s.length) {
                int remain = s.length - copyReg[0];
                int realCopy = remain > d.length ? d.length : remain;
                System.arraycopy(s, copyReg[0], d, 0, realCopy);
                copyReg[0] += realCopy;
                return realCopy;
            } else if (loopReg[0] > 1) {
                loopReg[0]--;
                copyReg[0] = 0;
                return 0;
            }
            return -1;
        });
    }

    public DataSource createLoopSource(BooleanSupplier condition) {
        int[] copyReg = new int[1];
        copyReg[0] = 0;
        return createSharedSource((s, d) -> {
            if (copyReg[0] < s.length) {
                int remain = s.length - copyReg[0];
                int realCopy = remain > d.length ? d.length : remain;
                System.arraycopy(s, copyReg[0], d, 0, realCopy);
                copyReg[0] += realCopy;
                return realCopy;
            } else if (condition.getAsBoolean()) {
                copyReg[0] = 0;
                return 0;
            }
            return -1;
        });
    }
}
