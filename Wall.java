import java.util.Collections;
import java.util.List;

public class Wall {
    private List<Integer> colRange;
    private List<Integer> rowRange;
    private List<Move>    forbiddenMoves;
    private boolean       isHorizontal;

    public Wall(List<Integer> colRange, List<Integer> rowRange, boolean isHorizontal, List<Move> forbiddenMoves) {
        this.colRange       = colRange;
        this.rowRange       = rowRange;
        this.isHorizontal   = isHorizontal;
        this.forbiddenMoves = forbiddenMoves;
        updateForbiddenMoves();
    }

    public boolean isValidWallPlacement() {
        // TODO: A* path check
        return true;
    }

    private void updateForbiddenMoves() {
        Move forward1, back1, forward2, back2;
        if (isHorizontal) {
            forward1 = new Move(new Position(colRange.get(0), rowRange.get(0)), new Position(colRange.get(0), rowRange.get(1)));
            back1    = forward1.reversed();
            forward2 = new Move(new Position(colRange.get(1), rowRange.get(0)), new Position(colRange.get(1), rowRange.get(1)));
            back2    = forward2.reversed();
        } else {
            forward1 = new Move(new Position(colRange.get(0), rowRange.get(0)), new Position(colRange.get(1), rowRange.get(0)));
            back1    = forward1.reversed();
            forward2 = new Move(new Position(colRange.get(0), rowRange.get(1)), new Position(colRange.get(1), rowRange.get(1)));
            back2    = forward2.reversed();
        }
        Collections.addAll(forbiddenMoves, forward1, back1, forward2, back2);
    }
}
