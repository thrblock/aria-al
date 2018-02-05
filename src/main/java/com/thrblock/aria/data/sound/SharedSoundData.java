package com.thrblock.aria.data.sound;

import javax.sound.sampled.AudioFormat;

import com.jogamp.openal.AL;
import com.thrblock.aria.data.DataSource;

class SharedSoundData implements DataSource {
    private AudioFormat decodedFormat;
    private byte[] sharedSrc;
    private ByteCopyStrategy sharedStrategy;

    public SharedSoundData(AudioFormat decodedFormat, byte[] sharedSrc, ByteCopyStrategy sharedStrategy) {
        this.decodedFormat = decodedFormat;
        this.sharedSrc = sharedSrc;
        this.sharedStrategy = sharedStrategy;
    }

    @Override
    public int getFormat() {
        return decodedFormat.getChannels() == 1 ? AL.AL_FORMAT_MONO16 : AL.AL_FORMAT_STEREO16;
    }

    @Override
    public int getRate() {
        return (int) decodedFormat.getSampleRate();
    }

    @Override
    public int fill(byte[] dst) throws Exception {
        return sharedStrategy.docopy(sharedSrc, dst);
    }

}
