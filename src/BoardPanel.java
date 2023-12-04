import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.Random;

/**Graphically represents the Board class*/
public class BoardPanel extends JPanel {
    private final Color boardColor = new Color(255, 205, 145);
    private final Color boardLineColor = new Color(166, 121, 55);
    private final Color[] blueStonePalette = {
            new Color(0, 10, 212, 200),
            new Color(63, 163, 255, 200),
            new Color(255, 255, 255)
    };
    private final Color[] redStonePalette = {
            new Color(150, 0, 0, 200),
            new Color(255, 100, 100, 200),
            new Color(255, 255, 255)
    };
    private final Color[] greenStonePalette = {
            new Color(0, 114, 0, 199),
            new Color(84, 208, 84, 199),
            new Color(218, 255, 218)
    };
    private final Color[] whiteStonePalette = {
            new Color(173, 173, 173, 200),
            new Color(234, 234, 234, 200),
            new Color(255, 255, 255)
    };
    private final int BOARD_PIXEL_LENGTH = 512;
    private final int PIXELS_BTWN_LINES = 32;
    private Player[] players = new Player[3]; // Size three to make less confusing
    private Player currentPlayer;
    private Board board;
    private Graphics brush;
    private boolean gameActive = true;
    private GUI gui;
    private int hoverRow = -1;
    private int hoverCol = -1;

    BoardPanel() {
        this(null, new HumanPlayer(), new ComputerPlayer());
    }

    BoardPanel(GUI gui, Player player1, Player player2) {
        initMouseHoverEffect();
        initMouseClickFunctionality();
        setPreferredSize(new Dimension(BOARD_PIXEL_LENGTH, BOARD_PIXEL_LENGTH));
        this.board = new Board();
        this.gui = gui;
        players[1] = player1;
        players[2] = player2;
        setCurrentPlayer();
        gui.setHeaderLabel(currentPlayer.getName() + " goes first!");
        if (currentPlayer.isComputer())
            computerMakeMove();
    }

    /**Paints the BoardPanel*/
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        brush = g;
        paintBoardBackground();
        paintBoardLines();
        paintStonesFromBoard();
        paintStoneUnderMouse();
    }

    /**Paints precise pixel-based rectangle*/
    private void paintBoardBackground() {
        brush.setColor(boardColor);
        brush.fillRect(0, 0, BOARD_PIXEL_LENGTH, BOARD_PIXEL_LENGTH);
    }

    /**Paints precise pixel-based lines*/
    private void paintBoardLines() {
        brush.setColor(boardLineColor);

        // Draws horizontal grid lines
        for(int line = 0; line < board.size() + 2; line++){
            int y = line * PIXELS_BTWN_LINES;
            brush.drawLine(0, y, BOARD_PIXEL_LENGTH, y);
        }

        // Draws vertical grid
        for(int line = 0; line < board.size() + 2; line++){
            int x = line * PIXELS_BTWN_LINES;
            brush.drawLine(x, 0, x, BOARD_PIXEL_LENGTH);
        }
    }

    /**Traverses paints stones given by board*/
    private void paintStonesFromBoard() {
        // Traverses Board.cells and paints the players stones
        for (int col = 0; col < board.size(); col++){
            for (int row = 0; row < board.size(); row++) {
                Player currentPlayer = board.getCells()[col][row];
                if (currentPlayer != null) {
                    // Zero-based step-up conversion from array[][] to display grid
                    paintStone(currentPlayer.getStoneColor(), col + 1, row + 1);
                }
            }
        }
    }

    /**Artfully paint a stone using 3-layers of colored ovals. */
    private void paintStone(StoneColor color, int col, int row) {
        // If coordinates are out of bounds or stone is present return
        if (outsideBounds(col, row))
            return;

        // Chooses which pallet to use
        Color[] pallet = whiteStonePalette;
        switch (color) {
            case BLUE -> pallet = blueStonePalette;
            case RED -> pallet = redStonePalette;
            case GREEN -> pallet = greenStonePalette;
        }

        // Artfully paints the dark bottom layer of stone
        int stoneDiameter = 28; // 7/8-ths the width of an intersection
        int stoneRadius = 12;
        int xOffset = (col * PIXELS_BTWN_LINES) - stoneRadius;
        int yOffset = (row * PIXELS_BTWN_LINES) - stoneRadius;
        brush.setColor(pallet[0]);
        brush.fillOval(xOffset, yOffset, stoneDiameter, stoneDiameter);

        // Artfully paints the colorful middle layer of stone
        int middleLayerOffset = stoneDiameter / 10;
        int middleLayerDiameter = stoneDiameter - middleLayerOffset;
        brush.setColor(pallet[1]);
        brush.fillOval(xOffset + middleLayerOffset, yOffset, middleLayerDiameter, middleLayerDiameter);

        // Artfully paints the 'shinny' top layer of stone
        int topLayerWidth = stoneDiameter / 12;         // Top layer is thin oval 1/12th the width of stone
        int topLayerHeight = stoneDiameter / 6;         // Top layer is tall oval 1/6th the height of stone
        int topLayerXOffset = (4 * stoneDiameter) / 5;  // Top layer is to the right 4/5th from left of stone
        int topLayerYOffset = stoneDiameter / 5;        // Top layer is above midline 1/5th from top of stone
        brush.setColor(pallet[2]);
        brush.fillOval(xOffset + topLayerXOffset, yOffset + topLayerYOffset, topLayerWidth, topLayerHeight);
    }

    /**Paints a stone on an intersection nearest to the mouse*/
    private void paintStoneUnderMouse() {
        if (!currentPlayer.isComputer() &&
            gameActive &&
            !outsideBounds(hoverCol, hoverRow) &&
            !board.isCellOccupied(hoverCol - 1, hoverRow - 1))
                paintStone(currentPlayer.getStoneColor(), hoverCol, hoverRow); // Then, paint the hover stone
    }

    /**Adds mouse hover placement effect to BoardPanel*/
    private void initMouseHoverEffect() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!currentPlayer.isComputer() && gameActive)
                    updateHoverPosition(e);
            }
        });
    }

    /** Converts mouse coordinates to the nearest row and column index on the board*/
    private void updateHoverPosition(MouseEvent e) {
        int nearestCol = Math.round((float) ((e.getX() + 16) / PIXELS_BTWN_LINES));
        int nearestRow = Math.round((float) ((e.getY() + 16) / PIXELS_BTWN_LINES));
        if (outsideBounds(nearestCol, nearestRow)) {
            nearestCol = Math.min(Math.max(nearestCol, 1), 15); // Keeps within bounds
            nearestRow = Math.min(Math.max(nearestRow, 1), 15); // Keeps within bounds
        }
        hoverCol = nearestCol;
        hoverRow = nearestRow;
        repaint();
    }

    /**Adds mouse click functionality to place stone */
    private void initMouseClickFunctionality() {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Zero-based step-down conversion from  display grid to array[][]
                if (!currentPlayer.isComputer() && gameActive)
                    evaluateGameState(board.evaluateMove(currentPlayer, hoverCol - 1, hoverRow - 1));
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }

    private void evaluateGameState(State state) {
        switch (state) {
            case STONE_PLACED -> {
                swapCurrentPlayer();
                gui.setHeaderLabel("It's " + currentPlayer.getName() + "'s turn");
                if (currentPlayer.isComputer())
                    computerMakeMove();
            }
            case BOARD_FULL -> {
                gameActive = false;
                gui.setHeaderLabel("Game Over: Board is full");
                // Disable further actions or add restart option
            }
            case PLAYER_WIN -> {
                gameActive = false;
                gui.setHeaderLabel("Game Over: " + currentPlayer.getName() + " wins!");
                // Highlight winning row and disable further actions
            }
        }
        repaint();
    }

    private boolean outsideBounds(int col, int row) {
        return (row < 1 || row > 15) || (col < 1 || col > 15);
    }

    private void swapCurrentPlayer() {
        currentPlayer = currentPlayer.equals(players[1]) ? players[2] : players[1];
    }

    private void setCurrentPlayer() {
        Random random = new Random();
        currentPlayer = random.nextBoolean() ? players[1] : players[2];
    }

    private void computerMakeMove() {
        // Disable hover effect and UI interaction

        // Using Timer pause for a moment while the computer "thinks"
        new Timer(1500, ae -> {
            if (currentPlayer.isComputer()) { // Checks again because
                ComputerPlayer ai = (ComputerPlayer) currentPlayer;
                evaluateGameState(ai.placeRandomEmptyCell(board));
                repaint();
            }
        }).start();
    }

    public static void main(String[] args) {
        // For testing
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ensure the application closes when the frame is closed
        frame.setSize(new Dimension(512,512));
        frame.add(new BoardPanel());    // Adds the BoardPanel before calling pack
        frame.setResizable(false);      // Call setResizable before pack()
        frame.pack();                   // Pack the frame after adding components
        frame.setVisible(true);         // Make the frame visible after packing
    }
}
