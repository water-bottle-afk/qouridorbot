
public class Position{
    public int x;
    public int y;

    public Position(int x, int y) {
        /*
        This function initializes the position with the given x and y coordinates.
        @param: int x, int y
        @return: none
        */
       if (x < 0 || x > 8 || y < 0 || y > 8) {
            throw new IllegalArgumentException("Position coordinates must be between 0 and 8.");
        }
        else {
            this.x = x;
            this.y = y;
        }
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