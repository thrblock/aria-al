package com.thrblock.aria.data.sound;

@FunctionalInterface
public interface ByteCopyStrategy {
    int docopy(byte[] src,byte[] dst);
}
