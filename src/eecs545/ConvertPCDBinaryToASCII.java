package eecs545;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Converts the Cornell data set from their weird binary format to
 *  a nice ASCII format. Also removes the '_' fields.
 * @author Augie
 */
public class ConvertPCDBinaryToASCII {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("Expected 2 args: [input directory of PCD files] [empty output directory]");
        }

        // Set up input and output directories
        File inDir = new File(args[0]);
        if (!inDir.exists()) {
            throw new Exception("Input directory does not exist.");
        }

        File outDir = new File(args[1]);
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new Exception("Could not create output directory.");
        }

        // For every PCD file in the input directory, convert it from binary to ASCII data format
        for (String fileName : inDir.list()) {
            if (!fileName.toLowerCase().endsWith(".pcd")) {
                continue;
            }
            File inFile = new File(inDir, fileName);
            File outFile = new File(outDir, fileName);
            InputStream in = null;
            OutputStream out = null;
            try {
                in = FileUtils.openInputStream(inFile);
                out = FileUtils.openOutputStream(outFile);
                convertBinaryToASCII(in, out);
            } finally {
                try {
                    out.flush();
                } catch (Exception e) {
                }
                IOUtils.closeQuietly(out);
                IOUtils.closeQuietly(in);
            }
        }
    }

    public static void convertBinaryToASCII(InputStream in, OutputStream outputStream) throws Exception {
        // Open a stream to the test file
        PrintStream out = new PrintStream(outputStream);

        // Read in the head of the file
        String version = readLine(in);
        String fields = readLine(in);
        String size = readLine(in);
        String type = readLine(in);
        String count = readLine(in);
        String width = readLine(in);
        String height = readLine(in);
        String viewpoint = readLine(in);
        String points = readLine(in);
        String data = readLine(in);

        // Revise to remove the 2 '_' fields
        String[] splitFields = fields.split(" ");
        String[] splitSize = size.split(" ");
        String[] splitType = type.split(" ");
        String[] splitCount = count.split(" ");
        String[] revisedFields = new String[splitFields.length - 2];
        String[] revisedSize = new String[splitFields.length - 2];
        String[] revisedType = new String[splitFields.length - 2];
        String[] revisedCount = new String[splitFields.length - 2];
        for (int i = 0, fieldCount = 0; i < splitFields.length; i++) {
            if (splitFields[i].equals("_")) {
                continue;
            }
            revisedFields[fieldCount] = splitFields[i];
            revisedSize[fieldCount] = splitSize[i];
            revisedType[fieldCount] = splitType[i];
            revisedCount[fieldCount] = splitCount[i];
            fieldCount++;
        }
//        String[] revisedFields = new String[splitFields.length - 6];
//        String[] revisedSize = new String[splitFields.length - 6];
//        String[] revisedType = new String[splitFields.length - 6];
//        String[] revisedCount = new String[splitFields.length - 6];
//        for (int i = 0, fieldCount = 0; i < splitFields.length; i++) {
//            if (splitFields[i].equals("_") || fieldCount >= 5) {
//                continue;
//            }
//            revisedFields[fieldCount] = splitFields[i];
//            revisedSize[fieldCount] = splitSize[i];
//            revisedType[fieldCount] = splitType[i];
//            revisedCount[fieldCount] = splitCount[i];
//            fieldCount++;
//        }

        // Write out the head of the converted file
        out.println(version);
        out.println(join(revisedFields, " "));
        out.println(join(revisedSize, " "));
        out.println(join(revisedType, " "));
        out.println(join(revisedCount, " "));
        out.println(width);
        out.println(height);
        out.println(viewpoint);
        out.println(points);
        out.println("DATA ascii");

        // How many points?
        int pointCount = Integer.valueOf(points.split(" ")[1]);

        // How many bytes are a single data point?
        int dataPointSize = 0;
        String[] sizes = size.split(" ");
        String[] counts = count.split(" ");
        for (int s = 1; s < sizes.length; s++) {
            dataPointSize += Integer.valueOf(sizes[s]) * Integer.valueOf(counts[s]);
        }

        // Get rid of the crap at the beginning
        byte[] last = new byte[1];
        int lastVal = -1;
        while (lastVal <= 0) {
            in.read(last);
            lastVal = (int) last[0];
        }

        // Read in a data point
        for (int i = 0; i < pointCount; i++) {
            // Read in the data point
            byte[] dataPoint = new byte[dataPointSize];
            if (lastVal <= 0) {
                in.read(dataPoint);
            } else {
                dataPoint[0] = last[0];
                byte[] afterFirstByte = new byte[dataPointSize - 1];
                in.read(afterFirstByte);
                for (int j = 0; j < afterFirstByte.length; j++) {
                    dataPoint[j + 1] = afterFirstByte[j];
                }
                lastVal = -1;
            }
            float x = readUnsignedFloat(getBytes(dataPoint, 0, 3));
            float y = readUnsignedFloat(getBytes(dataPoint, 4, 7));
            float z = readUnsignedFloat(getBytes(dataPoint, 8, 11));
            byte[] wats = getBytes(dataPoint, 12, 15);
            byte[] rgbBytes = getBytes(dataPoint, 16, 19);
//            float rgbFloat = readUnsignedFloat(rgbBytes);
//            float rgbFloat = readFloat(rgbBytes);
            int rgbInt = readUnsignedInt(rgbBytes);
//            int rgbInt = readInt(rgbBytes);
//            int[] rgb = readRGB(rgbInt);
            byte[] wats2 = getBytes(dataPoint, 20, 31);
            int cameraIndex = readUnsignedInt(getBytes(dataPoint, 32, 35));
            float distance = readUnsignedFloat(getBytes(dataPoint, 36, 39));
            int segment = readUnsignedInt(getBytes(dataPoint, 40, 43));
            int label = readUnsignedInt(getBytes(dataPoint, 44, 47));

            // Write out the data point
//            out.println(x + " " + y + " " + z + " " + rgbFloat + " " + cameraIndex + " " + distance + " " + segment + " " + label);
            out.println(x + " " + y + " " + z + " " + rgbInt + " " + cameraIndex + " " + distance + " " + segment + " " + label);
//            out.println(x + " " + y + " " + z + " " + rgbFloat);
//            out.println(x + " " + y + " " + z + " " + rgbInt);
        }
    }

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

    public static float readFloat(byte[] bytes) throws Exception {
        return ByteBuffer.wrap(bytes).getFloat();
    }

    public static float readUnsignedFloat(byte[] bytes) throws Exception {
        return Float.intBitsToFloat(readUnsignedInt(bytes));
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
