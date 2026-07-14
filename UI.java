import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class UI extends JPanel {

    // Rendering-only wall descriptor
    public static class WallData {
        public final int startCol, startRow;
        public final boolean isHorizontal;
        public WallData(int startCol, int startRow, boolean isHorizontal) {
            this.startCol = startCol; this.startRow = startRow; this.isHorizontal = isHorizontal;
        }
    }

    private enum GameState { PLAYING, PLAYER1_WIN, PLAYER2_WIN, DRAW }

    // ── Base layout (un-scaled pixel values) ───────────
    private static final int CELL_SIZE     = 64;
    private static final int WALL_GAP      = 10;
    private static final int BOARD_PAD     = 28;
    private static final int BOARD_CELLS   = 9;
    private static final int LEFT_PANEL_W  = 132;
    private static final int RIGHT_PANEL_W = 168;
    private static final int INFO_HEIGHT   = 72;

    private static final int BOARD_AREA_W =
        BOARD_CELLS * CELL_SIZE + (BOARD_CELLS - 1) * WALL_GAP + 2 * BOARD_PAD;
    private static final int BOARD_AREA_H = BOARD_AREA_W;
    private static final int BOARD_X      = LEFT_PANEL_W + BOARD_PAD;
    private static final int BOARD_Y      = BOARD_PAD;

    // Total base canvas size (before scaling)
    private static final int BASE_W = LEFT_PANEL_W + BOARD_AREA_W + RIGHT_PANEL_W;
    private static final int BASE_H = BOARD_AREA_H + INFO_HEIGHT;

    // ── Color palette ──────────────────────────────────
    private static final Color BOARD_LIGHT     = new Color(222, 185, 132);
    private static final Color BOARD_DARK      = new Color(195, 158, 106);
    private static final Color SIDE_TOP        = new Color(55,  46,  32);
    private static final Color SIDE_BOT        = new Color(34,  27,  18);
    private static final Color CELL_BASE       = new Color(240, 210, 160);
    private static final Color CELL_SHINE      = new Color(255, 245, 162);
    private static final Color COORD_COLOR     = new Color(148, 113,  72);
    private static final Color WALL_BROWN      = new Color(68,  34,   5);
    private static final Color TOKEN_USED      = new Color(82,  72,  58);
    private static final Color P1_BLUE         = new Color(38,  118, 222);
    private static final Color P2_RED          = new Color(215,  48,  33);
    private static final Color MOVE_RING       = new Color(55,  208,  88);
    private static final Color GHOST_OK        = new Color(55,  215,  90, 195);
    private static final Color GHOST_BAD       = new Color(228,  52,  38, 195);
    private static final Color ASTAR_CYAN      = new Color(0,   205, 238, 162);
    private static final Color INFO_BG         = new Color(24,  24,  24);
    private static final Color BTN_IDLE        = new Color(52,   96,  52);
    private static final Color BTN_ON          = new Color(42,  172,  78);
    private static final Color BTN_DISABLED    = new Color(52,   52,  52);

    // ── Game state ─────────────────────────────────────
    private final Player         player1, player2;
    private final List<WallData> boardWalls;
    private final List<Move>     forbiddenMoves;
    private final List<Position> legalMoveTargets;
    private final List<Position> astarPath;
    private final Map<String, Integer> stateHistory;

    private Player    selectedPlayer;
    private int       currentTurn;
    private GameState gameState;

    private boolean   wallPlacementMode;
    private boolean   ghostIsHorizontal;
    private WallData  ghostWall;
    private boolean   showingAstar;

    // Button rects in BASE coordinates
    private final Rectangle turnWallBtn;
    private final Rectangle showAstarBtn;

    // ── Constructor ────────────────────────────────────
    public UI(Player player1, Player player2) {
        this.player1             = player1;
        this.player2             = player2;
        this.boardWalls          = new ArrayList<>();
        this.forbiddenMoves      = new ArrayList<>();
        this.legalMoveTargets    = new ArrayList<>();
        this.astarPath           = new ArrayList<>();
        this.stateHistory        = new HashMap<>();
        this.currentTurn         = 1;
        this.gameState           = GameState.PLAYING;
        this.ghostIsHorizontal   = true;

        setPreferredSize(new Dimension(BASE_W, BASE_H));
        setBackground(SIDE_BOT.darker());

        int btnX = LEFT_PANEL_W + BOARD_AREA_W + 12;
        int btnW = RIGHT_PANEL_W - 24;
        turnWallBtn = new Rectangle(btnX, 238, btnW, 40);
        showAstarBtn = new Rectangle(btnX, 292, btnW, 40);

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && wallPlacementMode)
                    cancelWallPlacement();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                Point b = toBase(e.getX(), e.getY());
                handleClick(b.x, b.y);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                Point b = toBase(e.getX(), e.getY());
                handleHover(b.x, b.y);
            }
        });

        recordState();
    }

    // ── Scale helpers ──────────────────────────────────

    private double computeScale() {
        double sx = (double) getWidth()  / BASE_W;
        double sy = (double) getHeight() / BASE_H;
        return Math.max(1.0, Math.min(sx, sy));
    }

    private int[] computeOffset(double scale) {
        int ox = Math.max(0, (int)((getWidth()  - BASE_W * scale) / 2));
        int oy = Math.max(0, (int)((getHeight() - BASE_H * scale) / 2));
        return new int[]{ox, oy};
    }

    // Convert screen coordinates to base canvas coordinates
    private Point toBase(int screenX, int screenY) {
        double scale = computeScale();
        int[] off = computeOffset(scale);
        return new Point((int)((screenX - off[0]) / scale),
                         (int)((screenY - off[1]) / scale));
    }

    // ── Board coordinate helpers ───────────────────────
    private int cellLeft(int col) { return BOARD_X + col * (CELL_SIZE + WALL_GAP); }
    private int cellTop(int row)  { return BOARD_Y + row * (CELL_SIZE + WALL_GAP); }

    private boolean isOverBoard(int bx, int by) {
        int r = BOARD_X + BOARD_CELLS * CELL_SIZE + (BOARD_CELLS - 1) * WALL_GAP;
        int b = BOARD_Y + BOARD_CELLS * CELL_SIZE + (BOARD_CELLS - 1) * WALL_GAP;
        return bx >= BOARD_X && bx < r && by >= BOARD_Y && by < b;
    }

    private int mouseToWallCol(int bx) {
        return Math.max(0, Math.min(7, (bx - BOARD_X) / (CELL_SIZE + WALL_GAP)));
    }
    private int mouseToWallRow(int by) {
        return Math.max(0, Math.min(7, (by - BOARD_Y) / (CELL_SIZE + WALL_GAP)));
    }

    private boolean hitsCell(int bx, int by, Position pos) {
        int l = cellLeft(pos.x), t = cellTop(pos.y);
        return bx >= l && bx < l + CELL_SIZE && by >= t && by < t + CELL_SIZE;
    }

    // ── Wall conflict checks ───────────────────────────
    private boolean wallConflicts(WallData c) {
        for (WallData e : boardWalls) if (overlaps(c, e)) return true;
        return false;
    }

    private boolean overlaps(WallData a, WallData b) {
        if (a.isHorizontal == b.isHorizontal)
            return a.isHorizontal
                ? a.startRow == b.startRow && Math.abs(a.startCol - b.startCol) <= 1
                : a.startCol == b.startCol && Math.abs(a.startRow - b.startRow) <= 1;
        WallData h = a.isHorizontal ? a : b, v = a.isHorizontal ? b : a;
        return h.startCol == v.startCol && h.startRow == v.startRow;
    }

    private boolean isGhostWallValid() {
        return ghostWall != null
            && !wallConflicts(ghostWall)
            && Logic.wallKeepsBothPathsOpen(
                player1.getPosition(), player2.getPosition(), forbiddenMoves,
                ghostWall.startCol, ghostWall.startRow, ghostWall.isHorizontal);
    }

    // ── Threefold repetition ───────────────────────────
    private String stateKey() {
        List<String> wk = new ArrayList<>();
        for (WallData w : boardWalls)
            wk.add(w.startCol + "," + w.startRow + (w.isHorizontal ? "H" : "V"));
        Collections.sort(wk);
        return player1.getPosition().x + "," + player1.getPosition().y
             + "|" + player2.getPosition().x + "," + player2.getPosition().y
             + "|" + currentTurn + "|" + String.join(";", wk);
    }

    private void recordState() {
        int count = stateHistory.merge(stateKey(), 1, Integer::sum);
        if (count >= 3 && gameState == GameState.PLAYING)
            gameState = GameState.DRAW;
    }

    private void checkWin(Player mover) {
        int goalRow = (mover == player1) ? BOARD_CELLS - 1 : 0;
        if (mover.getPosition().y == goalRow)
            gameState = (mover == player1) ? GameState.PLAYER1_WIN : GameState.PLAYER2_WIN;
    }

    // ── Event handlers ─────────────────────────────────
    private void handleHover(int bx, int by) {
        if (gameState != GameState.PLAYING || !wallPlacementMode) return;
        ghostWall = isOverBoard(bx, by)
            ? new WallData(mouseToWallCol(bx), mouseToWallRow(by), ghostIsHorizontal)
            : null;
        repaint();
    }

    private void handleClick(int bx, int by) {
        if (gameState != GameState.PLAYING) return;
        requestFocusInWindow();

        if (turnWallBtn.contains(bx, by)) {
            if (wallPlacementMode) {
                ghostIsHorizontal = !ghostIsHorizontal;
                if (ghostWall != null)
                    ghostWall = new WallData(ghostWall.startCol, ghostWall.startRow, ghostIsHorizontal);
                repaint();
            }
            return;
        }
        if (showAstarBtn.contains(bx, by)) { toggleAstar(); return; }

        if (bx < LEFT_PANEL_W)                      { handleSidePanelClick(1); return; }
        if (bx >= LEFT_PANEL_W + BOARD_AREA_W)      { handleSidePanelClick(2); return; }

        if (wallPlacementMode) {
            if (isGhostWallValid()) commitWall(ghostWall);
            return;
        }
        if (selectedPlayer != null) {
            for (Position t : legalMoveTargets) {
                if (hitsCell(bx, by, t)) { commitMove(selectedPlayer, t); return; }
            }
        }
        Player active = activePlayer();
        if (hitsCell(bx, by, active.getPosition())) toggleSelection(active);
        else clearSelection();
    }

    private void handleSidePanelClick(int panelOwner) {
        if (wallPlacementMode) { cancelWallPlacement(); return; }
        Player owner = (panelOwner == 1) ? player1 : player2;
        if (currentTurn == panelOwner && owner.getWallsRemaining() > 0) {
            wallPlacementMode = true;
            ghostIsHorizontal = true;
            clearSelection();
        }
    }

    // ── Game actions ───────────────────────────────────
    private void cancelWallPlacement() {
        wallPlacementMode = false;
        ghostWall = null;
        repaint();
    }

    private void commitWall(WallData wall) {
        boardWalls.add(wall);
        new Wall(Arrays.asList(wall.startCol, wall.startCol + 1),
                 Arrays.asList(wall.startRow, wall.startRow + 1),
                 wall.isHorizontal, forbiddenMoves);
        activePlayer().useWall();
        wallPlacementMode = false;
        ghostWall = null;
        astarPath.clear();
        showingAstar = false;
        currentTurn = (currentTurn == 1) ? 2 : 1;
        recordState();
        repaint();
    }

    private void toggleSelection(Player player) {
        if (selectedPlayer == player) { clearSelection(); return; }
        selectedPlayer = player;
        refreshMoveTargets();
        repaint();
    }

    private void clearSelection() {
        selectedPlayer = null;
        legalMoveTargets.clear();
        repaint();
    }

    private void refreshMoveTargets() {
        selectedPlayer.getLegalMoves(forbiddenMoves);
        legalMoveTargets.clear();
        Position opponentPos = (selectedPlayer == player1) ? player2.getPosition() : player1.getPosition();
        for (Move move : selectedPlayer.legalMoves) {
            Position target = move.getTargetPos();
            if (!target.equals(opponentPos))
                legalMoveTargets.add(target);
        }
    }

    private void commitMove(Player player, Position target) {
        player.setPosition(target);
        selectedPlayer = null;
        legalMoveTargets.clear();
        checkWin(player);
        if (gameState == GameState.PLAYING) {
            astarPath.clear();
            showingAstar = false;
            currentTurn = (currentTurn == 1) ? 2 : 1;
            recordState();
        }
        repaint();
    }

    private void toggleAstar() {
        if (showingAstar) {
            showingAstar = false;
            astarPath.clear();
        } else {
            // Always uses active player's CURRENT position
            Player active   = activePlayer();
            Player opponent = (currentTurn == 1) ? player2 : player1;
            int goalRow     = (currentTurn == 1) ? BOARD_CELLS - 1 : 0;
            astarPath.clear();
            astarPath.addAll(Logic.aStarPath(
                active.getPosition(), goalRow, forbiddenMoves, opponent.getPosition()));
            showingAstar = true;
        }
        repaint();
    }

    private Player activePlayer() { return (currentTurn == 1) ? player1 : player2; }

    // ── Painting: scale + center the base canvas ───────
    @Override
    protected void paintComponent(Graphics g) {
        // Background fill (visible in margins when scaling)
        g.setColor(SIDE_BOT.darker());
        g.fillRect(0, 0, getWidth(), getHeight());

        double scale = computeScale();
        int[] off    = computeOffset(scale);

        Graphics2D gfx = (Graphics2D) g.create();
        gfx.translate(off[0], off[1]);
        gfx.scale(scale, scale);
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
        gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                             RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        paintBaseCanvas(gfx);
        gfx.dispose();
    }

    private void paintBaseCanvas(Graphics2D gfx) {
        paintBoardSection(gfx);
        paintLeftPanel(gfx);
        paintRightPanel(gfx);
        paintInfoBar(gfx);
        if (gameState != GameState.PLAYING) paintGameOverlay(gfx);
    }

    // ── Board section ──────────────────────────────────
    private void paintBoardSection(Graphics2D gfx) {
        paintBoardBackground(gfx);
        paintCoordinateLabels(gfx);
        paintGoalRowGlow(gfx);
        paintCells(gfx);
        paintAstarPath(gfx);
        paintBoardWalls(gfx);
        paintGhostWall(gfx);
        paintLegalMoves(gfx);
        paintPlayerPiece(gfx, player1, P1_BLUE);
        paintPlayerPiece(gfx, player2, P2_RED);
    }

    private void paintBoardBackground(Graphics2D gfx) {
        gfx.setPaint(new GradientPaint(LEFT_PANEL_W, 0, BOARD_LIGHT,
                                        LEFT_PANEL_W, BOARD_AREA_H, BOARD_DARK));
        gfx.fillRect(LEFT_PANEL_W, 0, BOARD_AREA_W, BOARD_AREA_H);
    }

    private void paintCoordinateLabels(Graphics2D gfx) {
        gfx.setFont(new Font("SansSerif", Font.BOLD, 11));
        gfx.setColor(COORD_COLOR);
        FontMetrics fm = gfx.getFontMetrics();
        for (int col = 0; col < BOARD_CELLS; col++) {
            String lbl = String.valueOf(col + 1);
            gfx.drawString(lbl,
                cellLeft(col) + (CELL_SIZE - fm.stringWidth(lbl)) / 2, BOARD_Y - 10);
        }
        for (int row = 0; row < BOARD_CELLS; row++) {
            String lbl = String.valueOf((char)('A' + row));
            gfx.drawString(lbl,
                BOARD_X - 17, cellTop(row) + (CELL_SIZE + fm.getAscent()) / 2 - 1);
        }
    }

    private void paintGoalRowGlow(Graphics2D gfx) {
        gfx.setColor(new Color(P1_BLUE.getRed(), P1_BLUE.getGreen(), P1_BLUE.getBlue(), 38));
        for (int c = 0; c < BOARD_CELLS; c++)
            gfx.fillRoundRect(cellLeft(c), cellTop(BOARD_CELLS - 1), CELL_SIZE, CELL_SIZE, 8, 8);
        gfx.setColor(new Color(P2_RED.getRed(), P2_RED.getGreen(), P2_RED.getBlue(), 38));
        for (int c = 0; c < BOARD_CELLS; c++)
            gfx.fillRoundRect(cellLeft(c), cellTop(0), CELL_SIZE, CELL_SIZE, 8, 8);
    }

    private void paintCells(Graphics2D gfx) {
        for (int row = 0; row < BOARD_CELLS; row++) {
            for (int col = 0; col < BOARD_CELLS; col++) {
                boolean isSel = selectedPlayer != null
                    && selectedPlayer.getPosition().x == col
                    && selectedPlayer.getPosition().y == row;
                gfx.setColor(isSel ? CELL_SHINE : CELL_BASE);
                gfx.fillRoundRect(cellLeft(col), cellTop(row), CELL_SIZE, CELL_SIZE, 8, 8);
                gfx.setColor(new Color(255, 255, 255, 22));
                gfx.fillRoundRect(cellLeft(col) + 2, cellTop(row) + 2,
                    CELL_SIZE - 4, CELL_SIZE / 2 - 2, 6, 6);
            }
        }
    }

    private void paintAstarPath(Graphics2D gfx) {
        if (!showingAstar || astarPath.isEmpty()) return;
        int total = astarPath.size();
        for (int i = 0; i < total; i++) {
            Position cell  = astarPath.get(i);
            int alpha = 125 + (int)((float) i / Math.max(1, total - 1) * 80);
            gfx.setColor(new Color(0, 205, 238, alpha));
            gfx.fillRoundRect(cellLeft(cell.x), cellTop(cell.y), CELL_SIZE, CELL_SIZE, 8, 8);
            if (i > 0) {
                gfx.setFont(new Font("Arial", Font.BOLD, 11));
                gfx.setColor(new Color(255, 255, 255, 200));
                String step = String.valueOf(i);
                FontMetrics fm = gfx.getFontMetrics();
                gfx.drawString(step,
                    cellLeft(cell.x) + (CELL_SIZE - fm.stringWidth(step)) / 2,
                    cellTop(cell.y)  + (CELL_SIZE + fm.getAscent()) / 2 - 2);
            }
        }
    }

    private void paintBoardWalls(Graphics2D gfx) {
        for (WallData w : boardWalls) renderWall(gfx, w, WALL_BROWN);
    }

    private void paintGhostWall(Graphics2D gfx) {
        if (!wallPlacementMode || ghostWall == null) return;
        renderWall(gfx, ghostWall, isGhostWallValid() ? GHOST_OK : GHOST_BAD);
    }

    private void renderWall(Graphics2D gfx, WallData wall, Color color) {
        gfx.setColor(color);
        gfx.setStroke(new BasicStroke(WALL_GAP - 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (wall.isHorizontal) {
            int cy = cellTop(wall.startRow) + CELL_SIZE + WALL_GAP / 2;
            gfx.drawLine(cellLeft(wall.startCol), cy, cellLeft(wall.startCol + 1) + CELL_SIZE, cy);
        } else {
            int cx = cellLeft(wall.startCol) + CELL_SIZE + WALL_GAP / 2;
            gfx.drawLine(cx, cellTop(wall.startRow), cx, cellTop(wall.startRow + 1) + CELL_SIZE);
        }
    }

    private void paintLegalMoves(Graphics2D gfx) {
        if (legalMoveTargets.isEmpty()) return;
        int pad = 10, size = CELL_SIZE - 2 * pad;
        for (Position t : legalMoveTargets) {
            int px = cellLeft(t.x) + pad, py = cellTop(t.y) + pad;
            gfx.setColor(new Color(55, 208, 88, 55));
            gfx.fillOval(px, py, size, size);
            gfx.setColor(MOVE_RING);
            gfx.setStroke(new BasicStroke(3.2f));
            gfx.drawOval(px, py, size, size);
        }
    }

    private void paintPlayerPiece(Graphics2D gfx, Player player, Color pieceColor) {
        int pad  = 7;
        int left = cellLeft(player.getPosition().x) + pad;
        int top  = cellTop(player.getPosition().y)  + pad;
        int size = CELL_SIZE - 2 * pad;

        // Shadow
        gfx.setColor(new Color(0, 0, 0, 85));
        gfx.fillOval(left + 3, top + 5, size, size);

        // Radial gradient body
        gfx.setPaint(new RadialGradientPaint(
            new Point2D.Float(left + size * 0.36f, top + size * 0.28f),
            size * 0.78f, new float[]{0f, 1f},
            new Color[]{pieceColor.brighter(), pieceColor.darker().darker()}));
        gfx.fillOval(left, top, size, size);

        // Specular
        gfx.setColor(new Color(255, 255, 255, 55));
        gfx.fillOval(left + size / 5, top + size / 8, size / 3, size / 4);

        // Border
        boolean isSel = (player == selectedPlayer);
        gfx.setPaint(isSel ? Color.YELLOW : new Color(255, 255, 255, 170));
        gfx.setStroke(new BasicStroke(isSel ? 3.2f : 1.8f));
        gfx.drawOval(left, top, size, size);

        // Label
        gfx.setPaint(Color.WHITE);
        gfx.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = gfx.getFontMetrics();
        String lbl = String.valueOf(player.getName().charAt(player.getName().length() - 1));
        gfx.drawString(lbl,
            left + (size - fm.stringWidth(lbl)) / 2,
            top  + (size + fm.getAscent() - fm.getDescent()) / 2);
    }

    // ── Side panels ────────────────────────────────────
    private void paintLeftPanel(Graphics2D gfx) {
        paintSideBg(gfx, 0, LEFT_PANEL_W);
        boolean active = (currentTurn == 1 && gameState == GameState.PLAYING);
        if (active) { gfx.setColor(P1_BLUE); gfx.fillRect(LEFT_PANEL_W - 4, 0, 4, BOARD_AREA_H); }
        paintPanelHeader(gfx, "Player 1", 0, LEFT_PANEL_W, P1_BLUE, active);
        paintWallTokens(gfx, player1, 0, LEFT_PANEL_W, P1_BLUE);
        if (active && !wallPlacementMode && player1.getWallsRemaining() > 0)
            paintHint(gfx, "click panel to place wall", 0, LEFT_PANEL_W, BOARD_AREA_H - 12);
    }

    private void paintRightPanel(Graphics2D gfx) {
        int rx = LEFT_PANEL_W + BOARD_AREA_W;
        paintSideBg(gfx, rx, RIGHT_PANEL_W);
        boolean active = (currentTurn == 2 && gameState == GameState.PLAYING);
        if (active) { gfx.setColor(P2_RED); gfx.fillRect(rx, 0, 4, BOARD_AREA_H); }
        paintPanelHeader(gfx, "Player 2", rx, RIGHT_PANEL_W, P2_RED, active);
        paintWallTokens(gfx, player2, rx, RIGHT_PANEL_W, P2_RED);
        if (active && !wallPlacementMode && player2.getWallsRemaining() > 0)
            paintHint(gfx, "click panel to place wall", rx, RIGHT_PANEL_W, BOARD_AREA_H - 30);
        paintButton(gfx, turnWallBtn, "Turn Wall", wallPlacementMode ? BTN_ON : BTN_DISABLED);
        paintButton(gfx, showAstarBtn, "Show A*",  showingAstar      ? BTN_ON : BTN_IDLE);
        if (wallPlacementMode) {
            gfx.setFont(new Font("Arial", Font.ITALIC, 10));
            gfx.setColor(new Color(155, 145, 125));
            String hint = "ESC or click panel to cancel";
            FontMetrics fm = gfx.getFontMetrics();
            gfx.drawString(hint,
                rx + (RIGHT_PANEL_W - fm.stringWidth(hint)) / 2,
                turnWallBtn.y + turnWallBtn.height + 16);
        }
    }

    private void paintSideBg(Graphics2D gfx, int px, int pw) {
        gfx.setPaint(new GradientPaint(px, 0, SIDE_TOP, px, BOARD_AREA_H, SIDE_BOT));
        gfx.fillRect(px, 0, pw, BOARD_AREA_H);
    }

    private void paintPanelHeader(Graphics2D gfx, String name, int px, int pw,
                                   Color activeColor, boolean isActive) {
        gfx.setFont(new Font("Arial", Font.BOLD, 15));
        gfx.setColor(isActive ? activeColor : new Color(112, 102, 88));
        FontMetrics fm = gfx.getFontMetrics();
        gfx.drawString(name, px + (pw - fm.stringWidth(name)) / 2, 26);

        gfx.setFont(new Font("Arial", Font.PLAIN, 11));
        gfx.setColor(new Color(100, 90, 76));
        String sub = "Walls";
        fm = gfx.getFontMetrics();
        gfx.drawString(sub, px + (pw - fm.stringWidth(sub)) / 2, 40);
    }

    private void paintWallTokens(Graphics2D gfx, Player player, int px, int pw, Color activeColor) {
        int tokenW = pw - 30, tokenH = 10;
        int startY = 52, spacing = 17, tokenX = px + 15;

        for (int i = 0; i < 10; i++) {
            int ty = startY + i * spacing;
            boolean avail = i < player.getWallsRemaining();
            if (avail)
                gfx.setPaint(new GradientPaint(tokenX, ty, activeColor.brighter(),
                    tokenX, ty + tokenH, activeColor.darker()));
            else
                gfx.setColor(TOKEN_USED);
            gfx.fillRoundRect(tokenX, ty, tokenW, tokenH, 4, 4);
        }

        int countY = startY + 10 * spacing + 8;
        gfx.setFont(new Font("Arial", Font.BOLD, 12));
        gfx.setColor(new Color(140, 130, 115));
        String label = player.getWallsRemaining() + " / 10";
        FontMetrics fm = gfx.getFontMetrics();
        gfx.drawString(label, px + (pw - fm.stringWidth(label)) / 2, countY);
    }

    private void paintHint(Graphics2D gfx, String text, int px, int pw, int y) {
        gfx.setFont(new Font("Arial", Font.ITALIC, 10));
        gfx.setColor(new Color(150, 143, 100));
        FontMetrics fm = gfx.getFontMetrics();
        gfx.drawString(text, px + (pw - fm.stringWidth(text)) / 2, y);
    }

    private void paintButton(Graphics2D gfx, Rectangle r, String label, Color bg) {
        gfx.setPaint(new GradientPaint(r.x, r.y, bg.brighter(), r.x, r.y + r.height, bg.darker()));
        gfx.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        gfx.setPaint(new Color(255, 255, 255, 35));
        gfx.setStroke(new BasicStroke(1));
        gfx.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        gfx.setPaint(Color.WHITE);
        gfx.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = gfx.getFontMetrics();
        gfx.drawString(label,
            r.x + (r.width  - fm.stringWidth(label)) / 2,
            r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
    }

    // ── Info bar ───────────────────────────────────────
    private void paintInfoBar(Graphics2D gfx) {
        int barY = BOARD_AREA_H, barW = BASE_W;
        gfx.setColor(INFO_BG);
        gfx.fillRect(0, barY, barW, INFO_HEIGHT);
        gfx.setColor(new Color(60, 50, 35));
        gfx.setStroke(new BasicStroke(1));
        gfx.drawLine(0, barY, barW, barY);

        int textX = LEFT_PANEL_W + BOARD_PAD;

        if (gameState != GameState.PLAYING) {
            String msg;
            Color  col;
            if (gameState == GameState.DRAW) {
                msg = "Draw  —  board position repeated 3 times";
                col = new Color(218, 182, 58);
            } else {
                String winner = (gameState == GameState.PLAYER1_WIN) ? player1.getName() : player2.getName();
                msg = winner + " wins!  Reached the opposite side.";
                col = (gameState == GameState.PLAYER1_WIN) ? P1_BLUE : P2_RED;
            }
            gfx.setFont(new Font("Arial", Font.BOLD, 18));
            gfx.setColor(col);
            gfx.drawString(msg, textX, barY + 36);
            return;
        }

        String topLine;
        Color  topColor;
        if (wallPlacementMode) {
            topLine  = "Placing Wall  —  " + (ghostIsHorizontal ? "Horizontal" : "Vertical")
                     + "   (click board to place)";
            topColor = new Color(222, 182, 58);
        } else {
            topLine  = "Player Turn:   " + activePlayer().getName();
            topColor = (currentTurn == 1) ? P1_BLUE : P2_RED;
        }
        gfx.setFont(new Font("Arial", Font.BOLD, 17));
        gfx.setColor(topColor);
        gfx.drawString(topLine, textX, barY + 26);

        gfx.setFont(new Font("Arial", Font.PLAIN, 14));
        gfx.setColor(new Color(118, 108, 96));
        gfx.drawString(
            player1.getName() + " walls: " + player1.getWallsRemaining()
          + "    |    " + player2.getName() + " walls: " + player2.getWallsRemaining(),
            textX, barY + 50);
    }

    // ── Game-over overlay ──────────────────────────────
    private void paintGameOverlay(Graphics2D gfx) {
        gfx.setColor(new Color(0, 0, 0, 175));
        gfx.fillRect(LEFT_PANEL_W, 0, BOARD_AREA_W, BOARD_AREA_H);

        int cx = LEFT_PANEL_W + BOARD_AREA_W / 2;
        int cy = BOARD_AREA_H / 2;

        String headline, subline;
        Color  headColor;
        if (gameState == GameState.DRAW) {
            headline  = "DRAW";
            subline   = "Board position repeated 3 times";
            headColor = new Color(220, 185, 58);
        } else {
            String winner = (gameState == GameState.PLAYER1_WIN) ? player1.getName() : player2.getName();
            headline  = winner + " Wins!";
            subline   = "Reached the opposite side";
            headColor = (gameState == GameState.PLAYER1_WIN) ? P1_BLUE : P2_RED;
        }

        gfx.setFont(new Font("Arial", Font.BOLD, 58));
        FontMetrics fm = gfx.getFontMetrics();
        // Shadow
        gfx.setColor(new Color(0, 0, 0, 210));
        gfx.drawString(headline, cx - fm.stringWidth(headline) / 2 + 3, cy - 16 + 3);
        // Text
        gfx.setColor(headColor);
        gfx.drawString(headline, cx - fm.stringWidth(headline) / 2, cy - 16);

        gfx.setFont(new Font("Arial", Font.PLAIN, 20));
        fm = gfx.getFontMetrics();
        gfx.setColor(new Color(218, 218, 218));
        gfx.drawString(subline, cx - fm.stringWidth(subline) / 2, cy + 32);
    }
}
