package com.mark;

import java.io.BufferedInputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class FileLoader {
    public static byte[] Encode(byte[] data) {return Encode(data, 0);}
    public static byte[] Encode(byte[] data, int StartIndex) {return Encode(data, StartIndex, Integer.MAX_VALUE);}
    public static byte[] Encode(byte[] data, int StartIndex, int EndIndex) {
        EndIndex = Math.min(Math.max(EndIndex, 0), data.length);
        StartIndex = Math.min(Math.max(StartIndex, 0), data.length);
        if (EndIndex < StartIndex) return null;
        byte[] out = new byte[4 + EndIndex - StartIndex];
        ByteBuffer.allocate(4).putInt(EndIndex - StartIndex).get(0, out, 0, 4);
        for (int i = 4, j = StartIndex; j < EndIndex; i++, j++) {
            out[i] = data[j];
        }
        return out;
        /*
        EndIndex = Math.min(Math.max(EndIndex, 0), data.length);
        StartIndex = Math.min(Math.max(StartIndex, 0), data.length);
        int ExtraLength = data.length / 255 + 1;
        byte[] out = new byte[data.length + ExtraLength];
        {
            boolean AddZeroByteAtEnd = false;
            for (int i = StartIndex, j = StartIndex; i < EndIndex; i++, j++) {
                if (j % 256 == 0) {
                    int remaining = EndIndex - i;
                    if (remaining > 255) {
                        remaining = 255;
                        AddZeroByteAtEnd = true;
                    } else AddZeroByteAtEnd = false;
                    out[j++] = (byte) remaining;
                }
                out[j] = data[i];
            }
            if (AddZeroByteAtEnd) out[out.length - 1] = (byte) 0;
        }
        return out;
         */
    }
    public static DecodeReturn Decode(byte[] data) {return Decode(data, 0);}
    public static DecodeReturn Decode(byte[] data, int StartIndex) {
        System.out.print("Loading bytes: " + StartIndex);
        int length;
        try { length = ByteBuffer.wrap(data, StartIndex, 4).getInt(); } catch (Exception ex) { ex.printStackTrace(); return null; }
        System.out.println("->" + length + ".");
        byte[] out = new byte[length];
        int i;
        for (i = 0; i < out.length; i++) {
            out[i] = data[StartIndex + 4 + i];
        }
        return new DecodeReturn(out, StartIndex + 4 + i);
        /*
        System.out.println("StartIndex: " + StartIndex + "/" + data.length);
        ArrayList<Byte> out = new ArrayList<Byte>();
        int i = StartIndex;
        while (true) {
            if (i >= data.length) return null;
            int NextLength = data[i++] & 0xFF; // byte to int
            for (int j = 0; j < NextLength; j++) {
                out.add(data[i++]);
            }
            if (NextLength < 255) {
                break;
            }
        }
        byte[] Out = new byte[out.size()];
        for (int j = 0; j < Out.length; j++) {
            Out[j] = out.get(j);
        }
        return new DecodeReturn(Out, i);
        */
    }
}

class DecodeReturn {
    public DecodeReturn(byte[] data, int NextNonReadByte) {
        this.data = data;
        this.NextNonReadByte = NextNonReadByte;
    }
    public byte[] data;
    public int NextNonReadByte;
}