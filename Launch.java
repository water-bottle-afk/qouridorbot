import javax.swing.*;

public class Launch {
    private static final int WALLS_PER_PLAYER = 10;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Player player1 = new Player("Player 1", new Position(4, 0), WALLS_PER_PLAYER);
            Player player2 = new Player("Player 2", new Position(4, 8), WALLS_PER_PLAYER);

            UI boardUI = new UI(player1, player2);

            JFrame frame = new JFrame("Quoridor");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);
            frame.add(boardUI);
            frame.pack();
            frame.setMinimumSize(frame.getSize()); // enforce base size as minimum
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            boardUI.requestFocusInWindow();
        });
    }
}
