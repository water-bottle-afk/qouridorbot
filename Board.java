
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

    public void PrintBoard() {
        /*
        This function prints the current state of the board.
        @param: none
        @return: none
        */
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    System.out.print("[ ]");
                } else {
                    System.out.print("[" + board[i][j] + "]");
                }
            }
            System.out.println();
        }
    }
    public static void main(String[] args) {
        System.out.println("Hello, Nadav!");
        Board board = new Board();
        board.PrintBoard();
    }
}