package GrahamScanAlgos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import Utilities.Point;

/**
 * Graham Scan algorithm for finding the convex hull of a set of points.
 * This implementation correctly handles collinear points.
 */
public class GrahamScanConvexHull {

    /**
     * Finds the convex hull of a set of points.
     */
    public List<Point> findConvexHull(Point[] points) {
        if (points.length < 2) {
            return Arrays.asList(points);
        }

        // Find the lowest point (and leftmost if tied)
        Point start = points[0];
        for (int i = 1; i < points.length; i++) {
            if (start.y > points[i].y || (start.y == points[i].y && start.x > points[i].x)) {
                start = points[i];
            }
        }

        // Sort points by polar angle with respect to the start point
        sortByPolarAngle(points, start);

        // Graham scan algorithm
        Stack<Point> stack = new Stack<>();
        stack.push(points[0]);
        
        if (points.length > 1) {
            stack.push(points[1]);
        }
        
        // Process each point in the sorted array (starting from the 3rd point)
        for (int i = 2; i < points.length; i++) {
            // Pop the top point from the stack - this is our current 'top of hull' candidate
            Point top = stack.pop();
            
            // While we can make a right turn or have collinear points (cross product <= 0)
            // and the stack isn't empty, we need to potentially remove points
            while (!stack.isEmpty() && crossProduct(stack.peek(), top, points[i]) <= 0) {
                // Special case: if the points are collinear (cross product == 0)
                if (crossProduct(stack.peek(), top, points[i]) == 0) {
                    // For collinear points, we want to keep only the furthest one
                    if (distance(stack.peek(), points[i]) > distance(stack.peek(), top)) {
                        // If the new point is further, it becomes our new candidate
                        top = points[i];
                    }
                    // We break because we've handled this collinear case
                    break;
                }
                // For non-collinear right turns (cross product < 0), simply remove the bad point
                top = stack.pop();
            }
            
            // Push our current best 'top' candidate back onto the stack
            stack.push(top);
            
            // Now decide whether to add the current point to the hull
            if (stack.size() < 2 || 
                // Add if it makes a non-collinear turn with last two points
                crossProduct(stack.get(stack.size() - 2), stack.peek(), points[i]) != 0) {
                stack.push(points[i]);
            } 
            // Special collinear case - only add if it's further than current last point
            else if (distance(stack.get(stack.size() - 2), points[i]) > 
                    distance(stack.get(stack.size() - 2), stack.peek())) {
                // Replace last point with current point if it's further
                stack.pop();
                stack.push(points[i]);
            }
        }
        
        return new ArrayList<>(stack);
    }

    /**
     * Sort points by polar angle, and by distance for collinear points
     */
    private void sortByPolarAngle(Point[] points, Point start) {
        Arrays.sort(points, (p1, p2) -> {
            if (p1 == start) {
                return -1;
            }
            if (p2 == start) {
                return 1;
            }
            
            int cp = crossProduct(start, p1, p2);
            if (cp == 0) {
                // For collinear points, sort by distance from start
                return Integer.compare(distance(start, p1), distance(start, p2));
            } else {
                // Sort by polar angle (counter-clockwise)
                return -cp;
            }
        });
    }

    /**
     * Calculate the squared distance between two points
     */
    private int distance(Point a, Point b) {
        int dx = a.x - b.x;
        int dy = a.y - b.y;
        return dx * dx + dy * dy;
    }

    /**
     * Cross product to determine orientation of three points.
     * If result > 0: counter-clockwise turn
     * If result = 0: collinear
     * If result < 0: clockwise turn
     */
    private int crossProduct(Point a, Point b, Point c) {
        int y1 = a.y - b.y;
        int y2 = a.y - c.y;
        int x1 = a.x - b.x;
        int x2 = a.x - c.x;
        return y2 * x1 - y1 * x2;
    }

    /**
     * Utility method to create points from coordinate pairs
     */
    public static Point[] createPoints(int[][] coords) {
        Point[] points = new Point[coords.length];
        for (int i = 0; i < coords.length; i++) {
            points[i] = new Point(coords[i][0], coords[i][1]);
        }
        return points;
    }
    

    public static void main(String[] args) {
        // original code:
        //GrahamScanConvexHull grahamScanConvexHull = new GrahamScanConvexHull();
        //int[][] input = new int[][]{{0,0},{0,1},{0,2},{1,2},{2,2},{3,2},{3,1},{3,0},{2,0},{1,0},{1,1},{4,3}};
        //int[][] input = new int[][] {{1,1},{2,2},{2,0},{2,4},{3,3},{4,2}};
        // int[][] input = new int[][] {{1,1},{2,2},{3,3},{4,4}};
        // Point[] points = new Point[input.length];
        // int index = 0;
        // for (int[] i : input) {
        //     points[index++] = new Point(i[0], i[1]);
        // }
        // System.out.println(grahamScanConvexHull.findConvexHull(points));

        //for testing purposes:
        GrahamScanConvexHull graham = new GrahamScanConvexHull();
    
    // Test Case 1: All collinear points
    System.out.println("Test Case 1 - Collinear:");
    int[][] input1 = {{0,0}, {1,1}, {2,2}, {3,3}};
    Point[] points1 = createPoints(input1);
    System.out.println("Expected: [(0,0), (3,3)]");
    System.out.println("Actual: " + graham.findConvexHull(points1));
    
    // Test Case 2: Square with interior points
    System.out.println("\nTest Case 2 - Square with interior:");
    int[][] input2 = {{0,0}, {0,3}, {3,3}, {3,0}, {1,1}, {2,2}};
    Point[] points2 = createPoints(input2);
    System.out.println("Expected: [(0,0), (0,3), (3,3), (3,0)]");
    System.out.println("Actual: " + graham.findConvexHull(points2));
    
    // Test Case 3: Triangle with collinear edge
    System.out.println("\nTest Case 3 - Triangle with collinear:");
    int[][] input3 = {{0,0}, {2,0}, {1,1}, {1,0}};
    Point[] points3 = createPoints(input3);
    System.out.println("Expected: [(0,0), (2,0), (1,1)]");
    System.out.println("Actual: " + graham.findConvexHull(points3));
    
    // Test Case 4: Single point
    System.out.println("\nTest Case 4 - Single point:");
    int[][] input4 = {{5,5}};
    Point[] points4 = createPoints(input4);
    System.out.println("Expected: [(5,5)]");
    System.out.println("Actual: " + graham.findConvexHull(points4));
    
    // Test Case 5: Two points
    System.out.println("\nTest Case 5 - Two points:");
    int[][] input5 = {{1,2}, {3,4}};
    Point[] points5 = createPoints(input5);
    System.out.println("Expected: [(1,2), (3,4)]");
    System.out.println("Actual: " + graham.findConvexHull(points5));
    
    // Test Case 6: Random non-convex shape
    System.out.println("\nTest Case 6 - Random shape:");
    int[][] input6 = {{0,0}, {1,3}, {2,2}, {4,4}, {0,5}, {3,1}, {5,0}};
    Point[] points6 = createPoints(input6);
    System.out.println("Expected: [(0,0), (0,5), (4,4), (5,0)]");
    System.out.println("Actual: " + graham.findConvexHull(points6));
    
    // Test Case 7: Vertical line
    System.out.println("\nTest Case 7 - Vertical line:");
    int[][] input7 = {{2,0}, {2,1}, {2,3}, {2,2}};
    Point[] points7 = createPoints(input7);
    System.out.println("Expected: [(2,0), (2,3)]");
    System.out.println("Actual: " + graham.findConvexHull(points7));
    
    // Test Case 8: Horizontal line
    System.out.println("\nTest Case 8 - Horizontal line:");
    int[][] input8 = {{1,5}, {3,5}, {2,5}, {4,5}};
    Point[] points8 = createPoints(input8);
    System.out.println("Expected: [(1,5), (4,5)]");
    System.out.println("Actual: " + graham.findConvexHull(points8));
    
    // Test Case 9: Pentagon
    System.out.println("\nTest Case 9 - Pentagon:");
    int[][] input9 = {{0,0}, {2,0}, {3,2}, {1,4}, {-1,2}};
    Point[] points9 = createPoints(input9);
    System.out.println("Expected: [(0,0), (2,0), (3,2), (1,4), (-1,2)]");
    System.out.println("Actual: " + graham.findConvexHull(points9));
    
    // Test Case 10: Duplicate points
    System.out.println("\nTest Case 10 - Duplicates:");
    int[][] input10 = {{1,1}, {1,1}, {2,2}, {1,1}};
    Point[] points10 = createPoints(input10);
    System.out.println("Expected: [(1,1), (2,2)]");
    System.out.println("Actual: " + graham.findConvexHull(points10));

    // Test Case 11: Large test case with mixed scenarios
    System.out.println("Large Test Case - Mixed Points:");
    int[][] input = {
        {0, 0},    // Should be in hull (bottom-left corner)
        {2, 0},    // Should be in hull (bottom edge)
        {4, 0},    // Should be in hull (bottom-right corner)
        {6, 2},    // Should be in hull (right edge)
        {6, 4},    // Should be in hull (top-right corner)
        {4, 6},    // Should be in hull (top edge)
        {2, 6},    // Should be in hull (top-left corner)
        {0, 4},    // Should be in hull (left edge)
        {1, 1},    // Interior point (should be excluded)
        {3, 1},    // Collinear with {2,0} and {4,0} (should be excluded)
        {5, 3},    // Interior point (should be excluded)
        {3, 5},    // Interior point (should be excluded)
        {1, 3},    // Interior point (should be excluded)
        {2, 2},    // Interior point (should be excluded)
        {4, 2},    // Interior point (should be excluded)
        {2, 4},    // Interior point (should be excluded)
        {4, 4},    // Interior point (should be excluded)
        {3, 3},    // Dead center (should be excluded)
        {0, 2},    // Collinear with {0,0} and {0,4} (should be excluded)
        {6, 0}     // Collinear with {4,0} and {6,2} (should be excluded)
    };
    
    Point[] points = createPoints(input);
    System.out.println("Expected: [(0,0), (6,0), (6,4), (4,6), (2,6), (0,4)]");
    System.out.println("Actual: " + graham.findConvexHull(points));


    }
}