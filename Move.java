import java.util.List;

public class Move{

    private Position init_pos;
    private Position final_pos;

    public Move(Position init_pos, Position final_pos) {
        this.init_pos = init_pos;
        this.final_pos = final_pos;
    }

    public Move ReserveMove() {
        /*
        This function returns a new Move object with the initial and final positions reversed.
        @param: None
        @return: Move
        */
        return new Move(this.final_pos, this.init_pos);
    }

    public static boolean IsValidMove(Move m1, List<Move> forbiddenMoves) {
        if (m1 == null) {
            return false;
        }
        Move reversedMove = m1.ReserveMove();
        if (forbiddenMoves.contains(m1) || forbiddenMoves.contains(reversedMove)) {
            return false;
        }
        return true;
    }

    public boolean equals(Move other) {
        /*
        This function checks if two Move objects are equal based on their initial and final positions.
        @param: Move other
        @return: boolean
        */
        return this.init_pos.equals(other.init_pos) && this.final_pos.equals(other.final_pos);
    }
}