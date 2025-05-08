package JarvisAlgos;

import java.util.ArrayList;
import java.util.List;

public class JarvisMarchConvexHull {
    static class Point {
        int x;
        int y;
        
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    // To find orientation of ordered triplet (p, q, r).
    // The function returns following values
    // 0 --> p, q and r are collinear
    // 1 --> Clockwise
    // 2 --> Counterclockwise
    private static int orientation(Point p, Point q, Point r) {
        int val = (q.y - p.y) * (r.x - q.x) -
                  (q.x - p.x) * (r.y - q.y);
     
        if (val == 0) return 0;  // collinear
        return (val > 0) ? 1 : 2; // clock or counterclock wise
    }
    
    // Returns the square of distance between two points
    private static int distanceSq(Point p1, Point p2) {
        return (p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y);
    }
    
    // Finds convex hull of a set of points and returns them as a List
    public List<Point> findConvexHull(Point[] pts) {
        List<Point> hull = new ArrayList<>();
        int n = pts.length;
        
        if (n < 3) {
            for (Point p : pts) hull.add(p);
            return hull;
        }
     
        // Find leftmost point
        int lm = 0;
        for (int i = 1; i < n; i++) {
            if (pts[i].x < pts[lm].x) lm = i;
        }
     
        int curr = lm, next;
        do {
            hull.add(pts[curr]);
            next = (curr + 1) % n;
            
            for (int i = 0; i < n; i++) {
                int orient = orientation(pts[curr], pts[i], pts[next]);
                
                if (orient == 2) {
                    next = i;
                } else if (orient == 0) {
                    if (distanceSq(pts[curr], pts[i]) > distanceSq(pts[curr], pts[next])) {
                        next = i;
                    }
                }
            }
     
            curr = next;
        } while (curr != lm && hull.size() <= n);
     
        return hull;
    }

    // Utility method to create points from coordinate pairs
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
        JarvisMarchConvexHull graham = new JarvisMarchConvexHull();
    
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