package cz.trigon.ecubes.util;

public class NumberPacker {
    public static byte[] pack(int i) {
        return new byte[]{(byte) (i >> 24), (byte) (i >> 16),
                (byte) (i >> 8), (byte) i};
    }

    public static byte[] pack(float f) {
        return NumberPacker.pack(Float.floatToIntBits(f));
    }

    public static byte[] pack(long l) {
        return new byte[]{
                (byte) (l >> 56), (byte) (l >> 48),
                (byte) (l >> 40), (byte) (l >> 32),
                (byte) (l >> 24), (byte) (l >> 16),
                (byte) (l >> 8), (byte) l};
    }

    public static byte[] pack(short s) {
        return new byte[] { (byte) (s >> 8), (byte) s };
    }

    public static int unpackInt(byte[] a) {
        return (a[0] << 24) | ((a[1] & 0xFF) << 16) | ((a[2] & 0xFF) << 8) | (a[3] & 0xFF);
    }

    public static float unpackFloat(byte[] a) {
        return Float.intBitsToFloat(NumberPacker.unpackInt(a));
    }

    public static long unpackLong(byte[] a) {
        return ((long) a[0] << 56) | (((long) a[1] & 0xFF) << 48) | (((long) a[2] & 0xFF) << 40) | (((long) a[3] & 0xFF) << 32) |
                (((long) a[4] & 0xFF) << 24) | (((long) a[5] & 0xFF) << 16) | (((long) a[6] & 0xFF) << 8) | ((long) a[7] & 0xFF);
    }

    public static short unpackShort(byte[] a) {
        return (short) (((a[0] & 0xFF) << 8) | (a[1] & 0xFF));
    }

    public static int unpackInt(byte[] a, int offset) {
        return (a[offset] << 24) | ((a[1 + offset] & 0xFF) << 16) |
                ((a[2 + offset] & 0xFF) << 8) | (a[3 + offset] & 0xFF);
    }

    public static float unpackFloat(byte[] a, int offset) {
        return Float.intBitsToFloat(NumberPacker.unpackInt(a, offset));
    }

    public static long unpackLong(byte[] a, int offset) {
        return ((long) a[offset] << 56) | (((long) a[1 + offset] & 0xFF) << 48) |
                (((long) a[2 + offset] & 0xFF) << 40) | (((long) a[3 + offset] & 0xFF) << 32) |
                (((long) a[4 + offset] & 0xFF) << 24) | (((long) a[5 + offset] & 0xFF) << 16) |
                (((long) a[6 + offset] & 0xFF) << 8) | ((long) a[7 + offset] & 0xFF);
    }

    public static short unpackShort(byte[] a, int offset) {
        return (short) (((a[offset] & 0xFF) << 8) | (a[1 + offset] & 0xFF));
    }
}
