package eecs545;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Augie
 */
public class SeparateObjects {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new Exception("Expected 3 arg: [input PLY file] [# objects (K)] [iterations]");
        }
        int argCount = 0;

        // Read in the input point cloud file (PLY format)
        File inFile = new File(args[argCount++]);
        if (!inFile.exists()) {
            throw new Exception("Input file does not exist.");
        }

        // Get the number of clusters K
        int K = Integer.valueOf(args[argCount++]);
        if (K <= 0) {
            throw new Exception("K must be greater than 0.");
        }
        
        // For how many iterations should the clustering run?
        int CUTOFF_ITERATIONS = Integer.valueOf(args[argCount++]);
        if (CUTOFF_ITERATIONS <= 0) {
            throw new Exception("Iterations must be greater than 0.");
        }

        // Set up the output files
        Map<Integer, File> outputFiles = new HashMap<Integer, File>();
        for (int i = 0; i < K; i++) {
            outputFiles.put(i, new File(inFile.getParentFile(), inFile.getName().replace(".ply", "_o" + i + ".ply")));
        }

        // Read in the point clouds of the input file
        PointCloud pointCloud = new PointCloud(inFile);

        // Run K means clustering on the points in the cloud
        // Keep track of the centroids of the clusters
        double[][] centroids = new double[K][3];

        // Initialize each cluster centroid with color values of a random pixel
        for (int i = 0; i < K; i++) {
            // Choose a random point
            Point randomPoint = pointCloud.points.get(Utils.RANDOM.nextInt(pointCloud.points.size()));
            centroids[i][0] = randomPoint.x;
            centroids[i][1] = randomPoint.y;
            centroids[i][2] = randomPoint.z;
        }

        // Assignments of pixels to clusters
        Map<Point, Integer> r = new HashMap<Point, Integer>();

        // Iterate until convergence or cutoff
        for (int i = 0; i < CUTOFF_ITERATIONS; i++) {
            // E-step: assign each point to the closest centroid
            int[] pointsPerCluster = new int[K];
            Arrays.fill(pointsPerCluster, 0);
            for (Point p : pointCloud.points) {
                // Calculate distances
                int closestCluster = -1;
                double[] distance = new double[K];
                for (int k = 0; k < K; k++) {
                    distance[k] = Math.pow(p.x - centroids[k][0], 2) + Math.pow(p.y - centroids[k][1], 2) + Math.pow(p.z - centroids[k][2], 2);
                    if (closestCluster == -1 || distance[k] < distance[closestCluster]) {
                        closestCluster = k;
                    }
                }

                // Assign pixel to closest
                r.put(p, closestCluster);
                pointsPerCluster[closestCluster]++;
            }

            // M-step
            for (int k = 0; k < K; k++) {
                if (pointsPerCluster[k] == 0) {
                    continue;
                }
                for (int c = 0; c < 3; c++) {
                    double sum = 0;
                    for (Point p : pointCloud.points) {
                        if (r.get(p).intValue() == k) {
                            if (c == 0) {
                                sum += p.x;
                            } else if (c == 1) {
                                sum += p.y;
                            } else if (c == 2) {
                                sum += p.z;
                            }
                        }
                    }
                    centroids[k][c] = sum / (double) pointsPerCluster[k];
                }
            }
        }

        // Write out the new point cloud files with separated objects
        for (int k = 0; k < K; k++) {
            // Compile a list of all the points assigned to this cluster
            List<Point> clusterPoints = new LinkedList<Point>();
            for (Point p : r.keySet()) {
                if (r.get(p) == k) {
                    clusterPoints.add(p);
                }
            }
            // Write out the file
            File outFile = outputFiles.get(k);
            outFile.createNewFile();
            PrintStream out = null;
            try {
                out = new PrintStream(FileUtils.openOutputStream(outFile));
                // Header
                out.println("ply");
                out.println("format ascii 1.0");
                out.println("element vertex " + clusterPoints.size());
                out.println("property float x");
                out.println("property float y");
                out.println("property float z");
                out.println("property uchar diffuse_red");
                out.println("property uchar diffuse_green");
                out.println("property uchar diffuse_blue");
                out.println("element face 0");
                out.println("end_header");
                // Points
                for (Point p : clusterPoints) {
                    out.println(p.toString());
                }
            } finally {
                try {
                    out.flush();
                } catch (Exception e) {
                }
                IOUtils.closeQuietly(out);
            }
        }
    }
}
