public class Game {
    public Board board;
    public Player player1;
    public Player player2;
    
    public static void main(String[] args) {
        System.out.println("Hello, Nadav!");
        Board board = new Board();
        board.PrintBoard();
    }
}
