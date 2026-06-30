import java.util.ArrayList;
import java.util.List;

class Player {
    private Position position;
    List<Move> possibleMoves;

    private static final int BOARD_SIZE = 9;


    public Player(Position position) {
        /*
        This function initializes the player with the given x and y coordinates.
        @param: int x, int y
        @return: none
        */
        this.position = position;
        List<Move> possibleMoves = new ArrayList<>();

    }

    public void addMoveIfValid(List<Move> forbiddenMoves, Move move) {
        if (Move.IsValidMove(move, forbiddenMoves)) {
            this.possibleMoves.add(move);
        }

    }
    public void GetPossibleMoves(List<Move> forbiddenMoves) {
        //Neighbors
        if (this.position.y < BOARD_SIZE - 1)
            addMoveIfValid(forbiddenMoves,new Move(this.position, new Position(this.position.x, this.position.y + 1)));

        if (this.position.y > 0)
            addMoveIfValid(forbiddenMoves,new Move(this.position, new Position(this.position.x, this.position.y - 1)));

        if (this.position.x > 0)
            addMoveIfValid(forbiddenMoves,new Move(this.position, new Position(this.position.x-1, this.position.y - 1)));

        if (this.position.x < BOARD_SIZE - 1)
            addMoveIfValid(forbiddenMoves,new Move(this.position, new Position(this.position.x+1, this.position.y - 1)));
    }
}