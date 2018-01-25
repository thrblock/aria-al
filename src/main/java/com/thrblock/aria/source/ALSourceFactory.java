package com.thrblock.aria.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thrblock.aria.al.ALService;

@Component
public class ALSourceFactory {

    private static final int DEF_QUEUE_NUM = 4;
    private static final int DEF_BUFF_SIZE = 1024 * 32;

    @Autowired
    private ALService service;

    /**
     * Spirng use only
     */
    ALSourceFactory() {
    }

    public ALSourceFactory(ALService service) {
        this.service = service;
    }

    public ALSource generateALSource(boolean relative, int queueNum, int bufferSize) {
        ALSource source = new ALSource(service, relative, queueNum, bufferSize);
        service.onDestroy(source::destroy);
        return source;
    }

    public ALSource generateALSource(boolean relative) {
        return generateALSource(relative, DEF_QUEUE_NUM, DEF_BUFF_SIZE);
    }

    public ALSource generateALSource() {
        return generateALSource(true);
    }
}
