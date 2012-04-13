package eecs545;

/**
 *
 * @author Augie
 */
public class Point {

    public double x, y, z;
    public int r, g, b;

    public Point(String row) {
        String[] parts = row.split(" ");
        x = Double.valueOf(parts[0]);
        y = Double.valueOf(parts[1]);
        z = Double.valueOf(parts[2]);
        r = Integer.valueOf(parts[3]);
        g = Integer.valueOf(parts[4]);
        b = Integer.valueOf(parts[5]);
    }
    
    @Override
    public String toString() {
        return x + " " + y + " " + z + " " + r + " " + g + " " + b;
    }
}
