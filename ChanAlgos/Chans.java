package ChanAlgos;

import java.util.*;

public class Chans {

    static final int RIGHT_TURN = -1;
    static final int LEFT_TURN = 1;
    static final int COLLINEAR = 0;

    static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Point p = (Point) obj;
            return x == p.x && y == p.y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    static Point p0 = new Point(0, 0);

    static int dist(Point p1, Point p2) {
        return (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
    }

    static int orientation(Point p, Point q, Point r) {
        int val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
        if (val == 0) return COLLINEAR;
        return (val > 0) ? RIGHT_TURN : LEFT_TURN;
    }

    static Comparator<Point> polarOrder = (p1, p2) -> {
        int orient = orientation(p0, p1, p2);
        if (orient == 0)
            return (dist(p0, p2) >= dist(p0, p1)) ? -1 : 1;
        return (orient == LEFT_TURN) ? -1 : 1;
    };

    static int tangent(List<Point> v, Point p) {
        int n = v.size();
        if (n == 1) return 0;
        if (n == 2)
            return (orientation(p, v.get(0), v.get(1)) != RIGHT_TURN) ? 0 : 1;

        int l = 0, r = n;
        while (l < r) {
            int c = (l + r) / 2;
            int c_prev = (c - 1 + n) % n;
            int c_next = (c + 1) % n;

            Point pc = v.get(c);
            Point pprev = v.get(c_prev);
            Point pnext = v.get(c_next);

            int o1 = orientation(p, pc, pprev);
            int o2 = orientation(p, pc, pnext);

            if (o1 != RIGHT_TURN && o2 != RIGHT_TURN)
                return c;

            int o = orientation(p, v.get(l), pc);
            if (o == RIGHT_TURN && (o1 == RIGHT_TURN || orientation(p, v.get(l), v.get((l + 1) % n)) == o))
                r = c;
            else
                l = c + 1;
        }
        return l % n;
    }

    static int[] extremeHullPoint(List<List<Point>> hulls) {
        int h = 0, p = 0;
        for (int i = 0; i < hulls.size(); i++) {
            for (int j = 0; j < hulls.get(i).size(); j++) {
                Point curr = hulls.get(i).get(j);
                Point best = hulls.get(h).get(p);
                if (curr.y < best.y || (curr.y == best.y && curr.x < best.x)) {
                    h = i;
                    p = j;
                }
            }
        }
        return new int[]{h, p};
    }

    static int[] nextHullPoint(List<List<Point>> hulls, int[] lpoint) {
        Point p = hulls.get(lpoint[0]).get(lpoint[1]);
        int[] next = new int[]{lpoint[0], (lpoint[1] + 1) % hulls.get(lpoint[0]).size()};

        for (int h = 0; h < hulls.size(); h++) {
            if (h != lpoint[0]) {
                int s = tangent(hulls.get(h), p);
                Point q = hulls.get(next[0]).get(next[1]);
                Point r = hulls.get(h).get(s);
                int t = orientation(p, q, r);
                if (t == RIGHT_TURN || (t == COLLINEAR && dist(p, r) > dist(p, q))) {
                    next = new int[]{h, s};
                }
            }
        }
        return next;
    }

    static List<Point> keepLeft(List<Point> v, Point p) {
        while (v.size() > 1 && orientation(v.get(v.size() - 2), v.get(v.size() - 1), p) != LEFT_TURN)
            v.remove(v.size() - 1);
        if (v.isEmpty() || !v.get(v.size() - 1).equals(p))
            v.add(p);
        return v;
    }

    static List<Point> grahamScan(List<Point> points) {
        if (points.size() <= 1) return new ArrayList<>(points);

        points.sort((p1, p2) -> {
            if (p1.y != p2.y) return Integer.compare(p1.y, p2.y);
            return Integer.compare(p1.x, p2.x);
        });
        p0 = points.get(0);
        points.sort(polarOrder);

        List<Point> lower = new ArrayList<>();
        for (Point p : points) lower = keepLeft(lower, p);

        Collections.reverse(points);
        List<Point> upper = new ArrayList<>();
        for (Point p : points) upper = keepLeft(upper, p);

        upper.remove(0);  // avoid duplication
        lower.addAll(upper);
        return lower;
    }

    static List<Point> chansAlgorithm(List<Point> v) {
        int n = v.size();
        if (n <= 3) return grahamScan(v);

        for (int t = 1; ; t++) {
            int m = Math.min(n, 1 << (1 << t));
            List<List<Point>> hulls = new ArrayList<>();

            for (int i = 0; i < n; i += m) {
                List<Point> chunk = v.subList(i, Math.min(i + m, n));
                hulls.add(grahamScan(new ArrayList<>(chunk)));
            }

            List<int[]> hull = new ArrayList<>();
            hull.add(extremeHullPoint(hulls));

            for (int i = 0; i < m; ++i) {
                int[] p = nextHullPoint(hulls, hull.get(hull.size() - 1));
                if (Arrays.equals(p, hull.get(0))) {
                    List<Point> result = new ArrayList<>();
                    for (int[] pt : hull)
                        result.add(hulls.get(pt[0]).get(pt[1]));
                    return result;
                }
                hull.add(p);
            }
        }
    }

    public static void main(String[] args) {
        List<Point> points = Arrays.asList(
            new Point(10, 0), new Point(7, 7), new Point(0, 10), new Point(-7, 7),
            new Point(-10, 0), new Point(-7, -7), new Point(0, -10), new Point(7, -7),
            new Point(9, 4), new Point(4, 9), new Point(-9, 4), new Point(-4, -9),
            new Point(0, 0), new Point(1, 1), new Point(-2, 3), new Point(4, -2),
            new Point(-3, -1), new Point(5, 0), new Point(-5, 0), new Point(0, 5),
            new Point(0, -5), new Point(-2, 2), new Point(-4, 7), new Point(2, -6),
            new Point(-2, 7), new Point(3, 7), new Point(7, 1), new Point(-10, -10),
            new Point(-2, 7), new Point(-10, 2), new Point(-3, -8)
        );

        List<Point> result = chansAlgorithm(points);
        for (Point p : result)
            System.out.println(p);
    }
}


