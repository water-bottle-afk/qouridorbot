import java.util.List;

public class Move {
    private Position startPos;
    private Position targetPos;

    public Move(Position startPos, Position targetPos) {
        this.startPos  = startPos;
        this.targetPos = targetPos;
    }

    public Position getStartPos()  { return startPos; }
    public Position getTargetPos() { return targetPos; }

    public Move reversed() {
        return new Move(this.targetPos, this.startPos);
    }

    public static boolean isLegalMove(Move move, List<Move> forbiddenMoves) {
        if (move == null) return false;
        return !forbiddenMoves.contains(move) && !forbiddenMoves.contains(move.reversed());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Move)) return false;
        Move other = (Move) obj;
        return this.startPos.equals(other.startPos) && this.targetPos.equals(other.targetPos);
    }

    @Override
    public int hashCode() {
        return 81 * startPos.hashCode() + targetPos.hashCode();
    }
}
