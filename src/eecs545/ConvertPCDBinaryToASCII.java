package eecs545;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
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
            throw new Exception("Expected 2 args: [input directory of PCD files] [output directory]");
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
            System.out.println(fileName);
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
        String version = Utils.readLine(in).trim();
        String fields = Utils.readLine(in).trim();
        String size = Utils.readLine(in).trim();
        String type = Utils.readLine(in).trim();
        String count = Utils.readLine(in).trim();
        String width = Utils.readLine(in).trim();
        String height = Utils.readLine(in).trim();
        String viewpoint = Utils.readLine(in).trim();
        String points = Utils.readLine(in).trim();
        String data = Utils.readLine(in).trim();

//        System.out.println(fields);
//        System.out.println(size);
//        System.out.println(type);
//        System.out.println(count);

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
            if (splitFields[i].equals("rgb")) {
                revisedType[fieldCount] = "U";
            } else {
                revisedType[fieldCount] = splitType[i];
            }
            revisedCount[fieldCount] = splitCount[i];
            fieldCount++;
        }

        // Write out the head of the converted file
        out.println(version);
        out.println(Utils.join(revisedFields, " "));
        out.println(Utils.join(revisedSize, " "));
        out.println(Utils.join(revisedType, " "));
        out.println(Utils.join(revisedCount, " "));
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
        
        // Throw away the extra padding bullshit
        in.read(new byte[42]);
        
        // Read in a data point
        for (int i = 0; i < pointCount; i++) {
            // Read in the data point
            byte[] dataPoint = new byte[dataPointSize];
//            if (lastVal <= 0) {
                in.read(dataPoint);
//            } else {
//                dataPoint[0] = last[0];
//                byte[] afterFirstByte = new byte[dataPointSize - 1];
//                in.read(afterFirstByte);
//                for (int j = 0; j < afterFirstByte.length; j++) {
//                    dataPoint[j + 1] = afterFirstByte[j];
//                }
//                lastVal = -1;
//            }

            // Half the data is like this
            float x = Utils.readUnsignedFloat(Utils.getBytes(dataPoint, 0, 3));
            float y = Utils.readUnsignedFloat(Utils.getBytes(dataPoint, 4, 7));
            float z = Utils.readUnsignedFloat(Utils.getBytes(dataPoint, 8, 11));
            byte[] rgbBytes = Utils.getBytes(dataPoint, 16, 19);
            int rgb = Utils.readUnsignedInt(rgbBytes);
            int cameraIndex = Utils.readUnsignedInt(Utils.getBytes(dataPoint, 32, 35));
            float distance = Utils.readUnsignedFloat(Utils.getBytes(dataPoint, 36, 39));
            int segment = Utils.readUnsignedInt(Utils.getBytes(dataPoint, 40, 43));
            int label = Utils.readUnsignedInt(Utils.getBytes(dataPoint, 44, 47));

            // Write out the data point
            String dataPointString = x + " " + y + " " + z + " " + rgb + " " + cameraIndex + " " + distance + " " + segment + " " + label;
//            System.out.println(dataPointString);
            out.println(dataPointString);
            
//            // 
//            String dpf = "";
//            for (int j = 0; j < 48; j += 4) {
//                dpf = dpf + " " + Utils.readFloat(Utils.getBytes(dataPoint, j, j + 3));
//            }
//            System.out.println(dpf);
//            String dpuf = "";
//            for (int j = 0; j < 48; j += 4) {
//                dpuf = dpuf + " " + Utils.readUnsignedFloat(Utils.getBytes(dataPoint, j, j + 3));
//            }
//            System.out.println(dpuf);
//            String dpi = "";
//            for (int j = 0; j < 48; j += 4) {
//                dpi = dpi + " " + Utils.readInt(Utils.getBytes(dataPoint, j, j + 3));
//            }
//            System.out.println(dpi);
//            String dpui = "";
//            for (int j = 0; j < 48; j += 4) {
//                dpui = dpui + " " + Utils.readUnsignedInt(Utils.getBytes(dataPoint, j, j + 3));
//            }
//            System.out.println(dpui);
//            System.out.println();
        }
    }
}
