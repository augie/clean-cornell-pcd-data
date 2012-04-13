package eecs545;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 *
 * @author Augie
 */
public class Utils {

    public static final Random RANDOM = new Random();
    
    public static String readLine(InputStream in) throws Exception {
        StringBuilder line = new StringBuilder();
        int c = -1;
        while ((c = in.read()) != -1 && c != (char) '\n') {
            line.append((char) c);
        }
        return line.toString();
    }

    public static byte[] getBytes(byte[] bytes, int from, int to) {
        byte[] ret = new byte[to - from + 1];
        for (int i = from, count = 0; i <= to; i++, count++) {
            ret[count] = bytes[i];
        }
        return ret;
    }

    public static String[] getStrings(String[] strings, int from, int to) {
        String[] ret = new String[to - from + 1];
        for (int i = from, count = 0; i <= to; i++, count++) {
            ret[count] = strings[i];
        }
        return ret;
    }

    public static float readFloat(byte[] bytes) throws Exception {
        return ByteBuffer.wrap(bytes).getFloat();
    }

    public static float readUnsignedFloat(byte[] bytes) throws Exception {
        return Float.intBitsToFloat(readUnsignedInt(bytes));
    }

    public static int readShort(byte[] bytes) throws Exception {
        return ByteBuffer.wrap(bytes).getShort();
    }

    public static int readUnsignedShort(byte[] bytes) throws Exception {
        return (bytes[0] & 0xff)
                | ((bytes[1] & 0xff) << 8);
    }

    public static int readInt(byte[] bytes) throws Exception {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static int readUnsignedInt(byte[] bytes) throws Exception {
        return (bytes[0] & 0xff)
                | ((bytes[1] & 0xff) << 8)
                | ((bytes[2] & 0xff) << 16)
                | ((bytes[3] & 0xff) << 24);
    }
    
    public static int getUnsignedInt(float f) throws Exception {
        return Float.floatToIntBits(f);
    }

    public static int[] readRGB(int f) {
        int b = (int) Math.floor(f / (256 * 256));
        int g = (int) Math.floor((f - b * 256 * 256) / 256);
        int r = (int) Math.floor(f % 256);
        return new int[]{r, g, b};
    }

    public static String join(String[] s, String by) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            buf.append(s[i]);
            buf.append(by);
        }
        for (int i = 0; i < by.length(); i++) {
            buf.deleteCharAt(buf.length() - i - 1);
        }
        return buf.toString();
    }
}
