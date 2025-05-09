package Utilities;

public class Point implements Comparable<Point> {
    public final int x;
    public final int y;
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Point)) return false;
        Point other = (Point) obj;
        return x == other.x && y == other.y;
    }
    
    @Override
    public int hashCode() {
        return 31 * x + y;
    }
    
    @Override
    public int compareTo(Point other) {
        if (this.x != other.x) {
            return Integer.compare(this.x, other.x);
        }
        return Integer.compare(this.y, other.y);
    }
}

