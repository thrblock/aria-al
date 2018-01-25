package com.thrblock.aria.source;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.openal.AL;
import com.thrblock.aria.al.ALService;
import com.thrblock.aria.data.DataSource;

class ALBufferQueue {
    private static final Logger LOG = LoggerFactory.getLogger(ALBufferQueue.class);

    private int[] buffer;
    private int[] bufferHolder = new int[1];
    private byte[] pcm;
    private int[] processed = new int[1];
    private ByteBuffer data;
    private AL al;
    private ALSource refSource;

    private DataSource dataSource;

    public ALBufferQueue(ALService alservice, ALSource source, int queueNum, int bufferSize) {
        this.buffer = new int[queueNum];
        this.pcm = new byte[bufferSize];
        this.data = ByteBuffer.wrap(pcm, 0, pcm.length);
        this.al = alservice.getAL();
        this.refSource = source;
        al.alGenBuffers(buffer.length, buffer, 0);
        alservice.check();
    }

    public int[] getBuffer() {
        return buffer;
    }

    public void attachData(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void attachAllBuffer() {
        al.alSourceQueueBuffers(refSource.getSource(), buffer.length, buffer, 0);
    }

    public void unAttachAllBuffer() {
        int[] queued = new int[1];
        al.alGetSourcei(refSource.getSource(), AL.AL_BUFFERS_QUEUED, queued, 0);
        for (int i = 0; i < queued[0]; i++) {
            al.alSourceUnqueueBuffers(refSource.getSource(), 1, bufferHolder, 0);
        }
    }

    public void fillBufferFirstly() {
        int i = 0;
        for (; i < buffer.length; i++) {
            if (stream(buffer[i]) < 0) {
                break;
            }
        }
        for (; i < buffer.length; i++) {
            streamWithEmpty(buffer[i]);
        }
    }

    public boolean refillProcessedBuffer() {
        al.alGetSourcei(refSource.getSource(), AL.AL_BUFFERS_PROCESSED, processed, 0);
        for (int i = 0; i < processed[0]; i++) {
            al.alSourceUnqueueBuffers(refSource.getSource(), 1, bufferHolder, 0);
            if (stream(bufferHolder[0]) > 0) {
                al.alSourceQueueBuffers(refSource.getSource(), 1, bufferHolder, 0);
            } else {
                return false;
            }
        }
        return true;
    }

    private int stream(int buffer) {
        int size = 0;
        try {
            while ((size = dataSource.fill(pcm)) == 0)
                ;// skip zero for ogg spi
        } catch (IOException e) {
            LOG.warn("error in fill pcm data:" + e);
        }
        if (size > 0) {
            al.alBufferData(buffer, dataSource.getFormat(), data, size, dataSource.getRate());
        }
        return size;
    }

    private void streamWithEmpty(int buffer) {
        Arrays.fill(pcm, (byte) 0);
        al.alBufferData(buffer, dataSource.getFormat(), data, pcm.length, dataSource.getRate());
    }

    public void destroy() {
        al.alDeleteBuffers(buffer.length, buffer, 0);
    }
}