import java.util.ArrayList;
import java.util.List;

class Player {
    private String   name;
    private Position position;
    private int      wallsRemaining;
    List<Move>       legalMoves;

    private static final int BOARD_SIZE = 9;

    public Player(String name, Position startPosition, int wallsRemaining) {
        this.name           = name;
        this.position       = startPosition;
        this.wallsRemaining = wallsRemaining;
        this.legalMoves     = new ArrayList<>();
    }

    public String   getName()           { return name; }
    public Position getPosition()       { return position; }
    public int      getWallsRemaining() { return wallsRemaining; }

    public void setPosition(Position newPosition) { this.position = newPosition; }
    public void useWall()                         { wallsRemaining--; }

    private void addIfLegalMove(List<Move> forbiddenMoves, Move candidate) {
        if (Move.isLegalMove(candidate, forbiddenMoves))
            legalMoves.add(candidate);
    }

    public void getLegalMoves(List<Move> forbiddenMoves) {
        legalMoves.clear();

        if (position.y < BOARD_SIZE - 1)
            addIfLegalMove(forbiddenMoves, new Move(position, new Position(position.x, position.y + 1)));

        if (position.y > 0)
            addIfLegalMove(forbiddenMoves, new Move(position, new Position(position.x, position.y - 1)));

        if (position.x > 0)
            addIfLegalMove(forbiddenMoves, new Move(position, new Position(position.x - 1, position.y)));

        if (position.x < BOARD_SIZE - 1)
            addIfLegalMove(forbiddenMoves, new Move(position, new Position(position.x + 1, position.y)));
    }
}
