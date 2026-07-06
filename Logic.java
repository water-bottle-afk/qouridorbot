import java.util.*;

public class Logic {
    private static final int BOARD_SIZE = 9;

    // ── A* shortest path (for display) ────────────────
    // Uses Manhattan row-distance as heuristic. Always uses the player's
    // CURRENT position — caller passes active.getPosition() directly.

    public static List<Position> aStarPath(Position start, int goalRow,
                                            List<Move> forbidden, Position opponentPos) {
        Map<Position, Integer>  gScore   = new HashMap<>();
        Map<Position, Position> cameFrom = new HashMap<>();
        Set<Position>           closed   = new HashSet<>();

        // Heap entry: {f, g, x, y}  (f fixed at insertion → lazy deletion handles stale entries)
        PriorityQueue<int[]> openSet = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));

        gScore.put(start, 0);
        cameFrom.put(start, null);
        openSet.add(new int[]{h(start, goalRow), 0, start.x, start.y});

        while (!openSet.isEmpty()) {
            int[] node = openSet.poll();
            int g = node[1], cx = node[2], cy = node[3];
            Position current = new Position(cx, cy);

            if (!closed.add(current)) continue; // stale heap entry — skip

            if (cy == goalRow) {
                return reconstructPath(current, cameFrom);
            }

            for (Position nb : passableNeighbors(current, forbidden, opponentPos)) {
                if (closed.contains(nb)) continue;
                int tentG = g + 1;
                if (tentG < gScore.getOrDefault(nb, Integer.MAX_VALUE)) {
                    gScore.put(nb, tentG);
                    cameFrom.put(nb, current);
                    openSet.add(new int[]{tentG + h(nb, goalRow), tentG, nb.x, nb.y});
                }
            }
        }
        return Collections.emptyList();
    }

    private static int h(Position pos, int goalRow) {
        return Math.abs(pos.y - goalRow); // admissible: each step reduces row-distance by at most 1
    }

    private static List<Position> reconstructPath(Position goal, Map<Position, Position> cameFrom) {
        List<Position> path = new ArrayList<>();
        for (Position step = goal; step != null; step = cameFrom.get(step))
            path.add(step);
        Collections.reverse(path);
        return path;
    }

    // ── Wall forbidden-move computation ────────────────

    public static List<Move> wallToForbiddenMoves(int startCol, int startRow, boolean isHorizontal) {
        List<Move> moves = new ArrayList<>(4);
        if (isHorizontal) {
            addBoth(moves, startCol,     startRow, startCol,     startRow + 1);
            addBoth(moves, startCol + 1, startRow, startCol + 1, startRow + 1);
        } else {
            addBoth(moves, startCol, startRow,     startCol + 1, startRow);
            addBoth(moves, startCol, startRow + 1, startCol + 1, startRow + 1);
        }
        return moves;
    }

    private static void addBoth(List<Move> list, int x1, int y1, int x2, int y2) {
        Move fwd = new Move(new Position(x1, y1), new Position(x2, y2));
        list.add(fwd);
        list.add(fwd.reversed());
    }

    // ── Wall placement validation (BFS) ───────────────
    // Uses plain BFS — not A* — because we only need a yes/no answer, not a path.

    public static boolean wallKeepsBothPathsOpen(Position p1Pos, Position p2Pos,
                                                  List<Move> existingForbidden,
                                                  int wallCol, int wallRow,
                                                  boolean wallIsHorizontal) {
        List<Move> tentative = new ArrayList<>(existingForbidden);
        tentative.addAll(wallToForbiddenMoves(wallCol, wallRow, wallIsHorizontal));
        return hasPath(p1Pos, BOARD_SIZE - 1, tentative)
            && hasPath(p2Pos, 0,              tentative);
    }

    // BFS connectivity check — ignores opponent, used only for wall validation
    public static boolean hasPath(Position start, int goalRow, List<Move> forbidden) {
        Set<Position>   visited  = new HashSet<>();
        Queue<Position> frontier = new LinkedList<>();
        frontier.add(start);
        visited.add(start);
        while (!frontier.isEmpty()) {
            Position cur = frontier.poll();
            if (cur.y == goalRow) return true;
            for (Position nb : passableNeighbors(cur, forbidden, null))
                if (visited.add(nb)) frontier.add(nb);
        }
        return false;
    }

    // ── Shared neighbor helper ─────────────────────────

    private static List<Position> passableNeighbors(Position from, List<Move> forbidden,
                                                      Position blocked) {
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        List<Position> result = new ArrayList<>(4);
        for (int[] d : dirs) {
            int nx = from.x + d[0], ny = from.y + d[1];
            if (nx < 0 || nx >= BOARD_SIZE || ny < 0 || ny >= BOARD_SIZE) continue;
            Position nb = new Position(nx, ny);
            if (blocked != null && nb.equals(blocked)) continue;
            if (Move.IsValidMove(new Move(from, nb), forbidden)) result.add(nb);
        }
        return result;
    }
}
