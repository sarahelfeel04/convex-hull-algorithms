package ChanAlgos;
import java.util.*;

/**
 * Implementation of Chan's Algorithm for computing the convex hull of a set of points
 * in 2D space. This algorithm combines Graham scan and Jarvis march to achieve
 * O(n log h) time complexity, where n is the number of input points and h is the
 * number of points in the convex hull.
 */
public class ChansAlgorithm {
    
    /**
     * Represents a 2D point with x and y coordinates
     */
    static class Point {
        double x;
        double y;
        
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Point)) return false;
            Point other = (Point) obj;
            return Double.compare(this.x, other.x) == 0 && Double.compare(this.y, other.y) == 0;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
    
    /**
     * Computes the convex hull of a set of points using Chan's algorithm
     * 
     * @param points the input set of points
     * @return the convex hull as a list of points in counter-clockwise order
     */
    public static List<Point> convexHull(List<Point> points) {
        if (points.size() <= 3) {
            if (points.size() <= 1) return new ArrayList<>(points);
            if (points.size() == 2) return new ArrayList<>(points);
            
            // For 3 points, check if they form a non-degenerate triangle
            List<Point> result = new ArrayList<>(points);
            if (orientation(result.get(0), result.get(1), result.get(2)) == 0) {
                // Points are collinear, keep only the extreme points
                result.remove(1);
            }
            return result;
        }
        
        // Get the point with the lowest y-coordinate (leftmost in case of tie)
        Point p1 = pickStart(points);
        
        // Try increasing values of m (number of iterations)
        for (int t = 1; t <= Math.ceil(Math.log(Math.log(points.size())) / Math.log(2)); t++) {
            // Set parameter m (using the "squaring scheme")
            int m = (int) Math.pow(2, Math.pow(2, t));
            
            // Attempt to find the convex hull with the current value of m
            List<Point> hull = computeHullWithParameter(points, m, p1);
            
            // If a hull was found, return it
            if (hull != null) {
                return hull;
            }
            // Otherwise, try again with a larger value of m
        }
        
        // This should never happen with reasonable inputs
        throw new IllegalStateException("Failed to compute convex hull");
    }
    
    /**
     * Tries to compute the convex hull using a specific value of parameter m
     * 
     * @param points the input set of points
     * @param m the parameter value
     * @param p1 the starting point
     * @return the convex hull if found, or null if m is too small
     */
    private static List<Point> computeHullWithParameter(List<Point> points, int m, Point p1) {
        // Initialize empty list to store the points of the convex hull
        List<Point> hull = new ArrayList<>();
        hull.add(p1);
        
        // Set first point
        Point p0 = new Point(Double.NEGATIVE_INFINITY, 0); // p0 is not a point of P
        Point pCurrent = p1;
        
        // For each iteration (at most m)
        for (int i = 1; i <= m; i++) {
            // Split the points into K = ⌈n/m⌉ subsets
            List<List<Point>> subsets = splitIntoSubsets(points, m);
            int K = subsets.size();
            
            // Compute the convex hull of each subset using Graham scan
            List<List<Point>> convexHulls = new ArrayList<>();
            for (List<Point> subset : subsets) {
                List<Point> subsetHull = grahamScan(subset);
                convexHulls.add(subsetHull);
            }
            
            // In the inner loop, compute K possible next points
            List<Point> candidates = new ArrayList<>();
            
            // Previous point in the hull
            Point prevPoint = (i == 1) ? p0 : hull.get(i - 2);
            
            // Find candidate points from each subset hull
            for (int k = 0; k < K; k++) {
                Point candidate = jarvisBinarySearch(prevPoint, pCurrent, convexHulls.get(k));
                if (candidate != null) {
                    candidates.add(candidate);
                }
            }
            
            // Choose the point with the maximum angle
            Point nextPoint = jarvisNextPoint(prevPoint, pCurrent, candidates);
            
            // Check if we have come full circle
            if (nextPoint.equals(p1)) {
                // Return the convex hull
                return hull;
            }
            
            // Add the point to the hull
            hull.add(nextPoint);
            pCurrent = nextPoint;
            
            // Check if we've exceeded m iterations
            if (i == m) {
                // m is too small, need to try again with a larger value
                return null;
            }
        }
        
        // If we get here, m is too small
        return null;
    }
    
    /**
     * Picks the starting point for the convex hull algorithm
     * (the point with the lowest y-coordinate, leftmost in case of tie)
     * 
     * @param points the input set of points
     * @return the starting point
     */
    private static Point pickStart(List<Point> points) {
        Point start = points.get(0);
        for (Point p : points) {
            if (p.y < start.y || (p.y == start.y && p.x < start.x)) {
                start = p;
            }
        }
        return start;
    }
    
    /**
     * Splits the input points into roughly equal-sized random subsets
     * Randomly shuffles the points before splitting to increase the chance that hull points are on subset boundaries.
     * 
     * @param points the input set of points
     * @param m the parameter value
     * @return the subsets of points
     */
    private static List<List<Point>> splitIntoSubsets(List<Point> points, int m) {
        List<Point> shuffledPoints = new ArrayList<>(points);
        Collections.shuffle(shuffledPoints, new Random()); // Use default seed for true randomness
        List<List<Point>> subsets = new ArrayList<>();
        int n = shuffledPoints.size();
        int K = (int) Math.ceil((double) n / m);
        for (int i = 0; i < K; i++) {
            int start = i * m;
            int end = Math.min(start + m, n);
            subsets.add(new ArrayList<>(shuffledPoints.subList(start, end)));
        }
        return subsets;
    }
    
    /**
     * Implements Graham scan algorithm to compute the convex hull of a set of points
     * 
     * @param points the input set of points
     * @return the convex hull as a list of points in counter-clockwise order
     */
    private static List<Point> grahamScan(List<Point> points) {
        if (points.size() <= 3) {
            if (points.size() <= 1) return new ArrayList<>(points);
            if (points.size() == 2) return new ArrayList<>(points);
            
            // For 3 points, check if they form a non-degenerate triangle
            List<Point> result = new ArrayList<>(points);
            if (orientation(result.get(0), result.get(1), result.get(2)) == 0) {
                // Points are collinear, keep only the extreme points
                result.remove(1);
            }
            return result;
        }
        
        // Find the point with the lowest y-coordinate (leftmost in case of tie)
        Point lowest = pickStart(points);
        
        // Sort the points by polar angle with respect to the lowest point
        final Point finalLowest = lowest;
        points.sort((p1, p2) -> {
            // Handle the case where one of the points is the lowest point
            if (p1.equals(finalLowest)) return -1;
            if (p2.equals(finalLowest)) return 1;
            
            // Use the orientation test to determine order
            int orient = orientation(finalLowest, p1, p2);
            if (orient == 0) {
                // If collinear, sort by distance from lowest
                double dist1 = distanceSquared(finalLowest, p1);
                double dist2 = distanceSquared(finalLowest, p2);
                return Double.compare(dist1, dist2);
            }
            
            return (orient > 0) ? -1 : 1;
        });
        
        // Initialize the result with the first three points
        Stack<Point> hull = new Stack<>();
        hull.push(points.get(0));
        
        // Process the sorted points
        for (int i = 1; i < points.size(); i++) {
            Point top = hull.pop();
            // Remove points that make a non-left turn
            while (!hull.isEmpty() && orientation(hull.peek(), top, points.get(i)) <= 0) {
                top = hull.pop();
            }
            hull.push(top);
            hull.push(points.get(i));
        }
        
        return new ArrayList<>(hull);
    }
    
    /**
     * Finds the point in a convex hull that maximizes the angle with the given points
     * using binary search (part of Jarvis march)
     * 
     * @param prev the previous point
     * @param current the current point
     * @param convexHull the convex hull to search in
     * @return the point that maximizes the angle
     */
    private static Point jarvisBinarySearch(Point prev, Point current, List<Point> convexHull) {
        if (convexHull.isEmpty()) return null;
        if (convexHull.size() == 1) return convexHull.get(0);
        
        // Handle the case where the convex hull has only two points
        if (convexHull.size() == 2) {
            Point p1 = convexHull.get(0);
            Point p2 = convexHull.get(1);
            
            // Return the point with the larger angle
            if (compareAngles(prev, current, p1, p2) > 0) {
                return p1;
            } else {
                return p2;
            }
        }
        
        // Use binary search to find the point with the maximum angle
        int low = 0;
        int high = convexHull.size() - 1;
        
        while (high - low > 1) {
            int mid = (low + high) / 2;
            
            // Check if the angle increases or decreases at mid
            if (compareAngles(prev, current, convexHull.get(mid), convexHull.get((mid + 1) % convexHull.size())) > 0) {
                high = mid;
            } else {
                low = mid;
            }
        }
        
        // Return the point with the maximum angle
        if (compareAngles(prev, current, convexHull.get(low), convexHull.get(high)) > 0) {
            return convexHull.get(low);
        } else {
            return convexHull.get(high);
        }
    }
    
    /**
     * Selects the next point for the convex hull from a list of candidates
     * (part of Jarvis march)
     * 
     * @param prev the previous point
     * @param current the current point
     * @param candidates the candidate points
     * @return the next point for the convex hull
     */
    private static Point jarvisNextPoint(Point prev, Point current, List<Point> candidates) {
        if (candidates.isEmpty()) return null;
        
        Point maxPoint = candidates.get(0);
        for (Point p : candidates) {
            if (compareAngles(prev, current, maxPoint, p) < 0) {
                maxPoint = p;
            }
        }
        
        return maxPoint;
    }
    
    /**
     * Computes the orientation of three points
     * 
     * @param p the first point
     * @param q the second point
     * @param r the third point
     * @return 0 if collinear, 1 if counterclockwise, -1 if clockwise
     */
    private static int orientation(Point p, Point q, Point r) {
        double val = (q.x - p.x) * (r.y - p.y) - (q.y - p.y) * (r.x - p.x);
        if (Math.abs(val) < 1e-9) return 0;  // collinear
        return (val > 0) ? 1 : -1;  // 1: counterclockwise, -1: clockwise
    }
    
    /**
     * Computes the squared distance between two points
     * 
     * @param p1 the first point
     * @param p2 the second point
     * @return the squared distance
     */
    private static double distanceSquared(Point p1, Point p2) {
        return (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
    }
    
    /**
     * Compares two angles formed by three points
     * Used to determine which of two points makes a larger angle with the current point
     * 
     * @param p0 the reference point
     * @param p1 the current point
     * @param p2 the first candidate point
     * @param p3 the second candidate point
     * @return a negative value if angle p0-p1-p2 < angle p0-p1-p3,
     *         zero if equal, positive if angle p0-p1-p2 > angle p0-p1-p3
     */
    private static int compareAngles(Point p0, Point p1, Point p2, Point p3) {
        // Use the orientation test to compare angles
        return orientation(p1, p2, p3);
    }
    
    // /**
    //  * Main method to test the implementation
    //  */
    // public static void main(String[] args) {
    //     // Example usage
    //     List<Point> points = new ArrayList<>();
    //     points.add(new Point(0, 0));
    //     points.add(new Point(1, 0));
    //     points.add(new Point(0, 1));
    //     points.add(new Point(1, 1));
    //     points.add(new Point(0.5, 0.5));
    //     points.add(new Point(0.25, 0.75));
    //     points.add(new Point(0.75, 0.25));
        
    //     List<Point> hull = convexHull(points);
        
    //     System.out.println("Convex Hull:");
    //     for (Point p : hull) {
    //         System.out.println(p);
    //     }
    // }

    
    /**
     * Main method to test the implementation
     */
    public static void main(String[] args) {
        // Create a larger example with points in different configurations (all integers)
        List<Point> points = new ArrayList<>();
        points.add(new Point(10, 0));     // 0: right
        points.add(new Point(7, 7));      // 1: top-right
        points.add(new Point(0, 10));     // 2: top
        points.add(new Point(-7, 7));     // 3: top-left
        points.add(new Point(-10, 0));    // 4: left
        points.add(new Point(-7, -7));    // 5: bottom-left
        points.add(new Point(0, -10));    // 6: bottom
        points.add(new Point(7, -7));     // 7: bottom-right
        points.add(new Point(9, 4));      // 8
        points.add(new Point(4, 9));      // 9
        points.add(new Point(-9, 4));     // 10
        points.add(new Point(-4, -9));    // 11
        points.add(new Point(0, 0));      // 12: center
        points.add(new Point(1, 1));      // 13
        points.add(new Point(-2, 3));     // 14
        points.add(new Point(4, -2));     // 15
        points.add(new Point(-3, -1));    // 16
        points.add(new Point(5, 0));      // 17
        points.add(new Point(-5, 0));     // 18
        points.add(new Point(0, 5));      // 19
        points.add(new Point(0, -5));     // 20
        points.add(new Point(-2, 2));     // 21
        points.add(new Point(-4, 7));     // 22
        points.add(new Point(2, -6));     // 23
        points.add(new Point(-2, 7));     // 24
        points.add(new Point(3, 7));      // 25
        points.add(new Point(7, 1));      // 26
        points.add(new Point(-10, -10));  // 27
        points.add(new Point(-2, 7));     // 28 (duplicate of 24)
        points.add(new Point(-10, 2));    // 29
        points.add(new Point(-3, -8));    // 30


        // // Add points on the perimeter of a rough octagon
        // points.add(new Point(10, 0));   // right
        // points.add(new Point(7, 7));    // top-right
        // points.add(new Point(0, 10));   // top
        // points.add(new Point(-7, 7));   // top-left
        // points.add(new Point(-10, 0));  // left
        // points.add(new Point(-7, -7));  // bottom-left
        // points.add(new Point(0, -10));  // bottom
        // points.add(new Point(7, -7));   // bottom-right
        
        // // Add some additional points that may be on the convex hull
        // points.add(new Point(9, 4));
        // points.add(new Point(4, 9));
        // points.add(new Point(-9, 4));
        // points.add(new Point(-4, -9));
        
        // // Add some interior points
        // points.add(new Point(0, 0));
        // points.add(new Point(1, 1));
        // points.add(new Point(-2, 3));
        // points.add(new Point(4, -2));
        // points.add(new Point(-3, -1));
        // points.add(new Point(5, 0));
        // points.add(new Point(-5, 0));
        // points.add(new Point(0, 5));
        // points.add(new Point(0, -5));
        
        // // Add some additional scattered integer points
        // Random random = new Random(42); // Fixed seed for reproducibility
        // for (int i = 0; i < 10; i++) {
        //     int x = random.nextInt(21) - 10;  // Range: -10 to 10
        //     int y = random.nextInt(21) - 10;  // Range: -10 to 10
        //     points.add(new Point(x, y));
        // }
        
        // Print all input points
        System.out.println("Input Points (" + points.size() + " points):");
        for (int i = 0; i < points.size(); i++) {
            System.out.println(i + ": " + points.get(i));
        }
        System.out.println();
        
        // Run Chan's algorithm with tracing
        System.out.println("Running Chan's Algorithm with detailed tracing:");
        List<Point> hull = traceChansAlgorithm(points);
        
        // Print the resulting convex hull
        System.out.println("\nFinal Convex Hull (" + hull.size() + " points):");
        for (Point p : hull) {
            System.out.println(p);
        }
    }
    
    /**
     * Traces the execution of Chan's Algorithm with detailed output for each step
     * 
     * @param points the input set of points
     * @return the convex hull
     */
    private static List<Point> traceChansAlgorithm(List<Point> points) {
        if (points.size() <= 3) {
            System.out.println("Small input with <= 3 points, returning directly.");
            if (points.size() <= 1) return new ArrayList<>(points);
            if (points.size() == 2) return new ArrayList<>(points);
            
            List<Point> result = new ArrayList<>(points);
            if (orientation(result.get(0), result.get(1), result.get(2)) == 0) {
                result.remove(1);
            }
            return result;
        }
        
        // Get the point with the lowest y-coordinate
        Point p1 = pickStart(points);
        System.out.println("Starting point p1: " + p1);
        
        // Try increasing values of m
        for (int t = 1; t <= Math.ceil(Math.log(Math.log(points.size())) / Math.log(2)); t++) {
            int m = (int) Math.pow(2, Math.pow(2, t));
            System.out.println("\nAttempting with m = " + m + " (iteration t = " + t + ")");
            
            // Attempt to find the hull with current m
            List<Point> hull = traceComputeHullWithParameter(points, m, p1);
            
            if (hull != null) {
                System.out.println("Success! Found convex hull with m = " + m);
                return hull;
            }
            
            System.out.println("Failed with m = " + m + ", trying larger value.");
        }
        
        throw new IllegalStateException("Failed to compute convex hull");
    }
    
    /**
     * Traces the computation of the hull with a specific parameter value
     * 
     * @param points the input set of points
     * @param m the parameter value
     * @param p1 the starting point
     * @return the convex hull if found, or null if m is too small
     */
    private static List<Point> traceComputeHullWithParameter(List<Point> points, int m, Point p1) {
        System.out.println("Computing hull with parameter m = " + m);
        
        // Initialize empty list for the hull
        List<Point> hull = new ArrayList<>();
        hull.add(p1);
        
        // Set first point
        Point p0 = new Point(Double.NEGATIVE_INFINITY, 0);
        Point pCurrent = p1;
        
        System.out.println("Initial hull point: " + p1);
        
        // For each iteration (at most m)
        for (int i = 1; i <= m; i++) {
            System.out.println("\n--- Iteration " + i + " ---");
            
            // Split the points into subsets
            List<List<Point>> subsets = splitIntoSubsets(points, m);
            int K = subsets.size();
            System.out.println("Split points into K = " + K + " subsets of roughly m = " + m + " points each");
            
            // Compute the convex hull of each subset
            List<List<Point>> convexHulls = new ArrayList<>();
            for (int k = 0; k < K; k++) {
                List<Point> subset = subsets.get(k);
                System.out.println("Computing convex hull of subset " + k + " with " + subset.size() + " points");
                List<Point> subsetHull = grahamScan(subset);
                System.out.println("Subset " + k + " hull has " + subsetHull.size() + " points");
                convexHulls.add(subsetHull);
            }
            
            // Compute K possible next points
            List<Point> candidates = new ArrayList<>();
            Point prevPoint = (i == 1) ? p0 : hull.get(i - 2);
            
            System.out.println("Finding candidate points using Jarvis march:");
            System.out.println("Previous point: " + prevPoint);
            System.out.println("Current point: " + pCurrent);
            
            // Find candidate points from each subset hull
            for (int k = 0; k < K; k++) {
                System.out.println("Searching in subset hull " + k);
                Point candidate = jarvisBinarySearch(prevPoint, pCurrent, convexHulls.get(k));
                if (candidate != null) {
                    System.out.println("Found candidate from subset " + k + ": " + candidate);
                    candidates.add(candidate);
                } else {
                    System.out.println("No candidate found in subset " + k);
                }
            }
            
            if (candidates.isEmpty()) {
                System.out.println("No candidates found, m is too small");
                return null;
            }
            
            // Choose the point with the maximum angle
            Point nextPoint = jarvisNextPoint(prevPoint, pCurrent, candidates);
            System.out.println("Selected next point: " + nextPoint);
            
            // Check if we have come full circle
            if (nextPoint.equals(p1)) {
                System.out.println("Returned to starting point - hull is complete!");
                return hull;
            }
            
            // Add the point to the hull
            hull.add(nextPoint);
            pCurrent = nextPoint;
            
            System.out.println("Current hull has " + hull.size() + " points");
            
            // Check if we've exceeded m iterations
            if (i == m) {
                System.out.println("Reached maximum iterations m = " + m + " without completing the hull");
                System.out.println("Need to try again with a larger value of m");
                return null;
            }
        }
        
        // If we get here, m is too small
        return null;
    }
}
