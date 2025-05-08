package GrahamScanAlgos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
//collinearity
public class GrahamScan {

    static class Point{
        int x;
        int y;
        Point(int x, int y){
            this.x = x;
            this.y = y;
        }
        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    public List<Point> findConvexHull(Point[] points) {
        if (points.length < 2) {
            return Arrays.asList(points);
        }

        //find the lowest point in the plane. If there are multiple lowest points
        //then pick the leftmost one.
        Point start = points[0];
        for (int i = 1; i < points.length; i++) {
            if (start.y > points[i].y) {
                start = points[i];
            } else if (start.y == points[i].y && start.x > points[i].x) {
                start = points[i];
            }
        }

        sortToHandleCollinear(points, start);

        Stack<Point> stack = new Stack<>();
        stack.push(points[0]);
        stack.push(points[1]);
        for (int i = 2; i < points.length; i++) {
            Point top = stack.pop();
            //second point will always be in answer so this will never cause empty stack exception.
            //as long as points[i] is on right of vector stack.peek() -> top keep getting rid of top of stack.
            while (crossProduct(stack.peek(), top, points[i]) < 0) {
                top = stack.pop();
            }
            stack.push(top);
            stack.push(points[i]);
        }
        return new ArrayList<>(stack);
    }

    private void sortToHandleCollinear(Point[] points, Point start) {
        Arrays.sort(points, (p1, p2) -> {
            if (p1 == start) {
                return -1;
            }
            if (p2 == start) {
                return 1;
            }
            int cp = crossProduct(start, p1, p2);
            if (cp == 0) {
                return distance(start, p1, p2);  // Keep closer points first
            } else {
                return -cp;  // Sort by descending order of cross product
            }
        });
        
        // Removed the collinear points reversal code completely
    }
    /**
     * Returns < 0 if 'b' is closer to 'a' compared to 'c', == 0 if 'b' and 'c' are same distance from 'a'
     * or > 0 if 'c' is closer to 'a' compared to 'b'.
     */
    private int distance(Point a, Point b, Point c) {
        int y1 = a.y - b.y;
        int y2 = a.y - c.y;
        int x1 = a.x - b.x;
        int x2 = a.x - c.x;
        return Integer.compare(y1 * y1 + x1 * x1, y2 * y2 + x2 * x2);
    }

    /**
     * Cross product to find where c belongs in reference to vector ab.
     * If result > 0 it means 'c' is on left of ab
     *    result == 0 it means 'a','b' and 'c' are collinear
     *    result < 0  it means 'c' is on right of ab
     */
    private int crossProduct(Point a, Point b, Point c) {
        int y1 = a.y - b.y;
        int y2 = a.y - c.y;
        int x1 = a.x - b.x;
        int x2 = a.x - c.x;
        return y2 * x1 - y1 * x2;
    }

     // Helper method to create Point arrays
     private static Point[] createPoints(int[][] coords) {
        Point[] points = new Point[coords.length];
        for (int i = 0; i < coords.length; i++) {
            points[i] = new Point(coords[i][0], coords[i][1]);
        }
        return points;
    }

    public static void main(String[] args) {
        // GrahamScan grahamScanConvexHull = new GrahamScan();
        // //int[][] input = new int[][]{{0,0},{0,1},{0,2},{1,2},{2,2},{3,2},{3,1},{3,0},{2,0},{1,0},{1,1},{4,3}};
        // int[][] input = new int[][] {{1,1},{2,2},{2,0},{2,4},{3,3},{4,2}};
        // Point[] points = new Point[input.length];
        // int index = 0;
        // for (int[] i : input) {
        //     points[index++] = new Point(i[0], i[1]);
        // }
        // System.out.println(grahamScanConvexHull.findConvexHull(points));

        //for testing purposes:
        GrahamScan graham = new GrahamScan();
    
    // Test Case 1: All collinear points
    System.out.println("Test Case 1 - Collinear:");
    int[][] input1 = {{0,0}, {1,1}, {2,2}, {3,3}};
    Point[] points1 = createPoints(input1);
    System.out.println("Expected: [(0,0), (3,3), (1,1), (2,2)]");
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
    System.out.println("Expected: [(0,0), (2,0), (1,1), (1,0)]");
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
    System.out.println("Expected: [(2,0), (2,3), (2,1), (2,0)]");
    System.out.println("Actual: " + graham.findConvexHull(points7));
    
    // Test Case 8: Horizontal line
    System.out.println("\nTest Case 8 - Horizontal line:");
    int[][] input8 = {{1,5}, {3,5}, {2,5}, {4,5}};
    Point[] points8 = createPoints(input8);
    System.out.println("Expected: [(4,5), (3,5), (2,5), (1,5)]");
    System.out.println("Actual: " + graham.findConvexHull(points8));
    
    // Test Case 9: Pentagon
    System.out.println("\nTest Case 9 - Pentagon:");
    int[][] input9 = {{0,0}, {2,0}, {3,2}, {1,4}, {-1,2}};
    Point[] points9 = createPoints(input9);
    System.out.println("Expected: [(0,0), (2,0), (3,2), (1,4), (-1,2)]");
    System.out.println("Actual: " + graham.findConvexHull(points9));


    // Test Case 10: Large test case with mixed scenarios
    System.out.println("\nLarge Test Case - Mixed Points:");
    int[][] input = {
        {0, 0},  
        {2, 0}, 
        {4, 0}, 
        {6, 2}, 
        {6, 4}, 
        {4, 6},   
        {2, 6},   
        {0, 4},   
        {1, 1},  
        {3, 1},   
        {5, 3}, 
        {3, 5},  
        {1, 3},  
        {2, 2},   
        {4, 2},  
        {2, 4},   
        {4, 4},    
        {3, 3},  
        {0, 2},  
        {6, 0}     
    };
    
    Point[] points = createPoints(input);
    System.out.println("Expected: (0,0), (2,0), (4,0), (6,0), (6,2), (6,4), (4,6), (2,6), (0,4), (0,2)");
    System.out.println("Actual: " + graham.findConvexHull(points));
    }
}