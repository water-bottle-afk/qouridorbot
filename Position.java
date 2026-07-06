public class Position {
    public int x;
    public int y;

    public Position(int x, int y) {
        if (x < 0 || x > 8 || y < 0 || y > 8)
            throw new IllegalArgumentException("Position coordinates must be between 0 and 8.");
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return 9 * x + y;
    }
}
