package eecs545;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Creates new PCD files, each with unique labeled data
 * @author Augie
 */
public class SeparateByLabel {

    private static final Map<Integer, File> LABEL_DIRS = new HashMap<Integer, File>();

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

        // For every PCD file in the input directory
        for (String fileName : inDir.list()) {
            if (!fileName.toLowerCase().endsWith(".pcd")) {
                continue;
            }
            File inFile = new File(inDir, fileName);
            InputStream in = null;
            try {
                in = FileUtils.openInputStream(inFile);
                separateByLabel(in, outDir, fileName);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
    }

    public static void separateByLabel(InputStream in, File outDir, String fileName) throws Exception {
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

        // Revise to remove the label field
        String[] splitFields = fields.split(" ");
        String[] splitSize = size.split(" ");
        String[] splitType = type.split(" ");
        String[] splitCount = count.split(" ");
        String[] revisedFields = new String[splitFields.length - 1];
        String[] revisedSize = new String[splitFields.length - 1];
        String[] revisedType = new String[splitFields.length - 1];
        String[] revisedCount = new String[splitFields.length - 1];
        for (int i = 0, fieldCount = 0; i < splitFields.length; i++) {
            if (splitFields[i].trim().equals("label")) {
                continue;
            }
            revisedFields[fieldCount] = splitFields[i];
            revisedSize[fieldCount] = splitSize[i];
            revisedType[fieldCount] = splitType[i];
            revisedCount[fieldCount] = splitCount[i];
            fieldCount++;
        }

        // Cluster data points by their label
        Map<Integer, List<String>> pointListMap = new HashMap<Integer, List<String>>();
        // How many points?
        int totalPointCount = Integer.valueOf(points.split(" ")[1].trim());
        // Read the points
        for (int i = 0; i < totalPointCount; i++) {
            // Read the point
            String dataPoint = Utils.readLine(in).trim();
            // What is the label
            String[] dataPointSplit = dataPoint.split(" ");
            // The label
            int label = Integer.valueOf(dataPointSplit[dataPointSplit.length - 1]);
            // Assign the data point to the label
            if (!pointListMap.containsKey(label)) {
                pointListMap.put(label, new LinkedList<String>());
            }
            pointListMap.get(label).add(Utils.join(Utils.getStrings(dataPointSplit, 0, dataPointSplit.length - 2), " "));
        }

        // Write files to a label dir
        for (int l : pointListMap.keySet()) {
            if (!LABEL_DIRS.containsKey(l)) {
                LABEL_DIRS.put(l, new File(outDir, String.valueOf(l)));
            }
        }

        // Write out the files
        for (int l : pointListMap.keySet()) {
            File outFile = new File(LABEL_DIRS.get(l), fileName.replace(".pcd", "") + "_l" + l + ".pcd");
            // Open an output stream
            OutputStream outputStream = FileUtils.openOutputStream(outFile);
            PrintStream out = new PrintStream(outputStream);

            // Write out the head of the converted file
            out.println(version);
            out.println(Utils.join(revisedFields, " "));
            out.println(Utils.join(revisedSize, " "));
            out.println(Utils.join(revisedType, " "));
            out.println(Utils.join(revisedCount, " "));
            int pointCount = pointListMap.get(l).size();
            out.println("WIDTH " + pointCount);
            out.println(height);
            out.println(viewpoint);
            out.println("POINTS " + pointCount);
            out.println("DATA ascii");

            // Write out the data points
            for (String dataPoint : pointListMap.get(l)) {
                out.println(dataPoint);
            }
        }
    }
}
