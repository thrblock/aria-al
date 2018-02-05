package com.thrblock.aria.data;

public interface DataSource {
    public int getFormat();

    public int getRate();

    public int fill(byte[] dst) throws Exception;
}
