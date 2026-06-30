import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Wall {
    private List<Integer> X;
    private List<Integer> Y;
    private List<Move> forbiddenMoves;
    private boolean is_horizontal;

    public Wall(List<Integer> X, List<Integer> Y, boolean is_horizontal, List<Move> forbiddenMoves) {
        /*
        This function initializes the wall with the given X and Y coordinates and orientation.
        @param: List<int[]> X, List<int[]> Y, boolean is_horizontal
        @return: none
        */
        this.X = X;
        this.Y = Y;
        this.is_horizontal = is_horizontal;
        this.forbiddenMoves = forbiddenMoves;
        UpdateForbiddenMoves();

    }
    
    public boolean IsValidWallPlacement() {
        /*
        This function checks if the wall placement is valid based on the current board state and forbidden moves.
        @param: none
        @return: boolean
        */
        // Add logic to check if the wall placement is valid A* has at least one path
        //
        return true;
    }
    public void UpdateForbiddenMoves() {
        Move m1, m2, m3, m4;
        if (this.is_horizontal) {
            // Calculate forbidden moves for horizontal wall
            m1 = new Move(new Position(X.get(0), Y.get(0)), new Position(X.get(0), Y.get(1)));
            m2 = m1.ReserveMove();
            m3 = new Move(new Position(X.get(1), Y.get(0)), new Position(X.get(1), Y.get(1)));
            m4 = m3.ReserveMove();

        } else {
            // Calculate forbidden moves for vertical wall
            m1 = new Move(new Position(X.get(0), Y.get(0)), new Position(X.get(1), Y.get(0)));
            m2 = m1.ReserveMove();

            m3 = new Move(new Position(X.get(0), Y.get(1)), new Position(X.get(1), Y.get(1)));
            m4 = m3.ReserveMove();
        }
        Collections.addAll(this.forbiddenMoves,m1, m2, m3, m4);

    }
}