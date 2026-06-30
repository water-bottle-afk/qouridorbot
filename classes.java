import java.util.ArrayList;
import java.util.List;

//@author: Nadav


public class classes {

    public static int return3 (){
        /*
        This function returns the integer 3.
        @param: none
        @return: int
        */
        return 3;
    }
    public static void main(String[] args) {
        System.out.println("Hello, Nadav!");
    }
}


public class Board {
    int[][] board;

    public Board() {
        /*
        This function initializes the board to be a 9x9 array of integers.
        @param: none
        @return: none
        */
        this.board = new int[9][9];
    }
}
public class Wall {
    private List<int[]> X;
    private List<int[]> Y;
    private is_horizontal;

    public Wall(List<int[]> X, List<int[]> Y, boolean is_horizontal, List<Move> forbiddenMoves) {
        /*
        This function initializes the wall with the given X and Y coordinates and orientation.
        @param: List<int[]> X, List<int[]> Y, boolean is_horizontal
        @return: none
        */
        this.X = X;
        this.Y = Y;
        this.is_horizontal = is_horizontal;
        this.forbiddenMoves = forbiddenMoves;
    }

    public ClaculateForbiddenMoves() {
        if this.is_horizontal {
            // Calculate forbidden moves for horizontal wall
            Move m1 = new Move(new Position(X.get(0), Y.get(0)), new Position(X.get(0), Y.get(1)));
            Move m2 = m1.ReserveMove();

            Move m3 = new Move(new Position(X.get(1), Y.get(0)), new Position(X.get(1), Y.get(1)));
            Move m4 = m3.ReserveMove();

        } else {
            // Calculate forbidden moves for vertical wall
            Move m1 = new Move(new Position(X.get(0), Y.get(0)), new Position(X.get(1), Y.get(0)));
            Move m2 = m1.ReserveMove();

            Move m3 = new Move(new Position(X.get(0), Y.get(1)), new Position(X.get(1), Y.get(1)));
            Move m4 = m3.ReserveMove();
        }
        this.forbiddenMoves.add(m1, m2, m3, m4);
    }
}

class Player {
    private int x;
    private int y;

    public Player(int x, int y) {
        /*
        This function initializes the player with the given x and y coordinates.
        @param: int x, int y
        @return: none
        */
        this.x = x;
        this.y = y;
    }

    public List<Move> GetPossibleMoves(List<Move> forbiddenMoves) {
        //Neighbors

        List<Move> possibleMoves = new ArrayList<>();
        Move up = new Move(new Position(x, y), new Position(x, y + 1));
        Move down = new Move(new Position(x, y), new Position(x, y - 1));
        Move left = new Move(new Position(x, y), new Position(x - 1, y));
        Move right = new Move(new Position(x, y), new Position(x + 1, y));

        // Add logic to calculate possible moves based on current position and forbidden moves
        return addMoveIfValid(forbiddenMoves, up, down, left, right);
    }

    public static addMoveIfValid(List<Move> forbiddenMoves, Move ...moves) {
        list<Move> possibleMoves = new ArrayList<>();
        for (Move m: moves) {
            if (Move.IsValidMove(m, forbiddenMoves)) {
                possibleMoves.add(m);
            }
        }
        return PossibleMoves;
    }
}

public class Move{

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
        return new Move(move.final_pos, move.init_pos);
    }

    public static boolean IsValidMove(Move m1, List<Move> forbiddenMoves){ {
    
        Move reversedMove = ,m1.ReserveMove();
        if (forbiddenMoves.contains(move) or forbiddenMoves.contains(reversedMove)) {
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

public class Position{
    private int x;
    private int y;

    public Position(int x, int y) {
        /*
        This function initializes the position with the given x and y coordinates.
        @param: int x, int y
        @return: none
        */
        this.x = x;
        this.y = y;
    }

    public boolean equals(Position other) {
        /*
        This function checks if two Position objects are equal based on their x and y coordinates.
        @param: Position other
        @return: boolean
        */
        return this.x == other.x && this.y == other.y;
    }
}