package com.mark.graph;

public class DecodeReturn {
    public DecodeReturn(byte[] data, int NextNonReadByte) {
        this.data = data;
        this.NextNonReadByte = NextNonReadByte;
    }

    public byte[] data;
    public int NextNonReadByte;
}
