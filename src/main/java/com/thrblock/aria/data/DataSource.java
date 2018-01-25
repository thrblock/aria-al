package com.thrblock.aria.data;

import java.io.IOException;

public abstract class DataSource {
    int format;
    int rate;

    public int getFormat() {
        return format;
    }

    public int getRate() {
        return rate;
    }

    public abstract int fill(byte[] dst) throws IOException;
}
