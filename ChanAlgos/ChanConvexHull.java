package ChanAlgos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import GrahamScanAlgos.GrahamScanConvexHull;
import JarvisAlgos.JarvisMarchOriginal;
import Utilities.Point;

public class ChanConvexHull {

    public List<Point> findConvexHull(Point[] points) {
        // Handle edge cases
        if (points == null || points.length < 3) {
            return new ArrayList<>(Arrays.asList(points));
        }

        // For small point sets, use Graham Scan directly
        if (points.length <= 6) {
            GrahamScanConvexHull graham = new GrahamScanConvexHull();
            return graham.findConvexHull(points);
        }

        // Optimal initial guess for m based on empirical studies
        int m = Math.min(10, points.length); 
        int iterationLimit = (int) (Math.log(points.length)/Math.log(2)) + 2;

        for (int iteration = 0; iteration < iterationLimit; iteration++) {
            // Partition points into ⌈n/m⌉ subsets
            List<List<Point>> partitions = partitionPoints(points, m);
            
            // Compute partial hulls
            List<List<Point>> partialHulls = computePartialHulls(partitions);
            
            // Combine and find convex hull
            List<Point> combined = combineHulls(partialHulls);
            Point[] combinedArray = combined.toArray(new Point[0]);
            
            // Try Jarvis march with early termination
            List<Point> hull = tryJarvisMarch(combinedArray, m);
            
            if (hull != null) {
                return hull;
            }
            
            // Exponential search for optimal m
            m = Math.min(m * 2, points.length);
            
            // Fallback to Graham Scan if m gets too large
            if (m == points.length) {
                GrahamScanConvexHull graham = new GrahamScanConvexHull();
                return graham.findConvexHull(points);
            }
        }
        
        // Final fallback
        GrahamScanConvexHull graham = new GrahamScanConvexHull();
        return graham.findConvexHull(points);
    }

    private List<List<Point>> partitionPoints(Point[] points, int m) {
        int partitionCount = (int) Math.ceil((double) points.length / m);
        List<List<Point>> partitions = new ArrayList<>(partitionCount);
        
        int partitionSize = (int) Math.ceil((double) points.length / partitionCount);
        
        for (int i = 0; i < partitionCount; i++) {
            int start = i * partitionSize;
            int end = Math.min(start + partitionSize, points.length);
            partitions.add(new ArrayList<>(Arrays.asList(Arrays.copyOfRange(points, start, end))));
        }
        return partitions;
    }

    private List<List<Point>> computePartialHulls(List<List<Point>> partitions) {
        List<List<Point>> partialHulls = new ArrayList<>(partitions.size());
        GrahamScanConvexHull graham = new GrahamScanConvexHull();
        
        for (List<Point> partition : partitions) {
            Point[] partitionArray = partition.toArray(new Point[0]);
            partialHulls.add(graham.findConvexHull(partitionArray));
        }
        return partialHulls;
    }

    private List<Point> combineHulls(List<List<Point>> partialHulls) {
        List<Point> combined = new ArrayList<>();
        for (List<Point> hull : partialHulls) {
            combined.addAll(hull);
        }
        return combined;
    }

    private List<Point> tryJarvisMarch(Point[] points, int m) {
        JarvisMarchOriginal jarvis = new JarvisMarchOriginal();
        List<Point> hull = jarvis.findConvexHull(points);
        
        // Early termination check
        if (hull.size() <= m * 2) { // More generous threshold
            return hull;
        }
        return null;
    }

    public static Point[] createPoints(int[][] coords) {
        Point[] points = new Point[coords.length];
        for (int i = 0; i < coords.length; i++) {
            points[i] = new Point(coords[i][0], coords[i][1]);
        }
        return points;
    }

    public static void main(String[] args) {
        ChanConvexHull chan = new ChanConvexHull();
        
        // Test case
        int[][] input = {
            {0, 0}, {1, 1}, {2, 2}, {3, 1}, {4, 0}, 
            {3, -1}, {2, -2}, {1, -1}, {0, -2}, {-1, -1},
            {-2, -2}, {-3, -1}, {-4, 0}, {-3, 1}, {-2, 2}, {-1, 1}
        };
        
        Point[] points = createPoints(input);
        List<Point> hull = chan.findConvexHull(points);
        System.out.println("Convex Hull: " + hull);
    }
}
