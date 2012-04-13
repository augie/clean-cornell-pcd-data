package eecs545;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Reads a PLY file.
 * @author Augie
 */
public class PointCloud {

    public StringBuilder header = new StringBuilder();
    public List<Point> points = new LinkedList<Point>();

    public PointCloud(File file) throws Exception {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(FileUtils.openInputStream(file)));
            // Header
            in.readLine();
            in.readLine();
            String verticesLine = in.readLine();
            int vertices = Integer.valueOf(verticesLine.split(" ")[2]);
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            // Points
            for (int i = 0; i < vertices; i++) {
                points.add(new Point(in.readLine()));
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
