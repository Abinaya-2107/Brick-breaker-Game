import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

public class BreakoutGame extends JPanel {

    // Game constants
    private static final int SCREEN_WIDTH = 600;
    private static final int SCREEN_HEIGHT = 400;
    private static final int PADDLE_WIDTH_INITIAL = 100;
    private static final int PADDLE_HEIGHT = 20;
    private static final int BALL_SIZE = 20;
    private static final int BRICK_WIDTH = 60;
    private static final int BRICK_HEIGHT = 30;

    // Game variables
    private int paddleX = SCREEN_WIDTH / 2 - PADDLE_WIDTH_INITIAL / 2;
    private int paddleWidth = PADDLE_WIDTH_INITIAL;
    private int ballX = SCREEN_WIDTH / 2 - BALL_SIZE / 2;
    private int ballY = SCREEN_HEIGHT / 2 - BALL_SIZE / 2;
    private int ballSpeedX = 5;
    private int ballSpeedY = 5;
    private boolean gameOver = false;
    private int score = 0;
    private int lives = 3;
    private int level = 1;

    // Brick array
    private boolean[][] bricks = new boolean[5][10];

    // Power-ups
    private ArrayList<Rectangle> powerUps = new ArrayList<>();

    public BreakoutGame() {
        // Initialize brick array
        initBricks();

        // Set up game loop
        Timer timer = new Timer(1000 / 60, e -> {
            updateGame();
            repaint();
        });
        timer.start();

        // Set up keyboard listener
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    paddleX -= 15;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    paddleX += 15;
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER && gameOver) {
                    resetGame();
                }
            }
        });
        setFocusable(true);
    }

    private void initBricks() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                bricks[i][j] = true;
            }
        }
    }

    private void updateGame() {
        if (gameOver) return;

        // Update ball position
        ballX += ballSpeedX;
        ballY += ballSpeedY;

        // Paddle boundaries
        if (paddleX < 0) paddleX = 0;
        if (paddleX > SCREEN_WIDTH - paddleWidth) paddleX = SCREEN_WIDTH - paddleWidth;

        // Ball collisions
        if (ballX < 0 || ballX > SCREEN_WIDTH - BALL_SIZE) {
            ballSpeedX = -ballSpeedX;
        }
        if (ballY < 0) {
            ballSpeedY = -ballSpeedY;
        }

        // Ball hits paddle
        if (ballY > SCREEN_HEIGHT - PADDLE_HEIGHT - BALL_SIZE - 20 &&
                ballX + BALL_SIZE > paddleX && ballX < paddleX + paddleWidth) {
            ballSpeedY = -ballSpeedY;
        }

        // Ball hits bricks
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                if (bricks[i][j] && ballX > j * (BRICK_WIDTH + 5) && ballX < j * (BRICK_WIDTH + 5) + BRICK_WIDTH &&
                        ballY > i * (BRICK_HEIGHT + 5) && ballY < i * (BRICK_HEIGHT + 5) + BRICK_HEIGHT) {
                    bricks[i][j] = false;
                    ballSpeedY = -ballSpeedY;
                    score += 10;
                    if (Math.random() < 0.2) { // 20% chance of a power-up
                        spawnPowerUp(j * (BRICK_WIDTH + 5) + BRICK_WIDTH / 2, i * (BRICK_HEIGHT + 5));
                    }
                }
            }
        }

        // Ball falls below paddle
        if (ballY > SCREEN_HEIGHT - BALL_SIZE) {
            lives--;
            if (lives == 0) {
                gameOver = true;
            } else {
                resetBall();
            }
        }

        // Check for power-up collisions
        checkPowerUpCollision();

        // Check level completion
        if (isLevelComplete()) {
            nextLevel();
        }
    }

    private boolean isLevelComplete() {
        for (boolean[] row : bricks) {
            for (boolean brick : row) {
                if (brick) return false;
            }
        }
        return true;
    }

    private void nextLevel() {
        level++;
        ballSpeedX++;
        ballSpeedY++;
        paddleWidth = Math.max(50, paddleWidth - 10); // Reduce paddle size but ensure it doesn't go below 50
        initBricks();
        resetBall();
    }

    private void resetBall() {
        ballX = SCREEN_WIDTH / 2 - BALL_SIZE / 2;
        ballY = SCREEN_HEIGHT / 2 - BALL_SIZE / 2;
        ballSpeedX = 5;
        ballSpeedY = 5;
    }

    private void resetGame() {
        gameOver = false;
        score = 0;
        lives = 3;
        level = 1;
        paddleWidth = PADDLE_WIDTH_INITIAL;
        ballSpeedX = 5;
        ballSpeedY = 5;
        initBricks();
        resetBall();
    }

    private void spawnPowerUp(int x, int y) {
        powerUps.add(new Rectangle(x, y, 20, 20));
    }

    private void checkPowerUpCollision() {
        Iterator<Rectangle> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            Rectangle powerUp = iterator.next();
            powerUp.y += 3; // Power-up falls

            if (powerUp.intersects(new Rectangle(paddleX, SCREEN_HEIGHT - PADDLE_HEIGHT - 20, paddleWidth, PADDLE_HEIGHT))) {
                paddleWidth += 20; // Example power-up effect: enlarge paddle
                iterator.remove();
            }

            if (powerUp.y > SCREEN_HEIGHT) {
                iterator.remove();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Paddle
        g.setColor(Color.WHITE);
        g.fillRect(paddleX, SCREEN_HEIGHT - PADDLE_HEIGHT - 20, paddleWidth, PADDLE_HEIGHT);

        // Ball
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        // Bricks
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                if (bricks[i][j]) {
                    g.setColor(Color.GREEN);
                    g.fillRect(j * (BRICK_WIDTH + 5), i * (BRICK_HEIGHT + 5), BRICK_WIDTH, BRICK_HEIGHT);
                }
            }
        }

        // Power-ups
        g.setColor(Color.YELLOW);
        for (Rectangle powerUp : powerUps) {
            g.fillRect(powerUp.x, powerUp.y, powerUp.width, powerUp.height);
        }

        // Score and lives
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Lives: " + lives, 10, 50);
        g.drawString("Level: " + level, SCREEN_WIDTH - 100, 20);

        // Game over message
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Game Over!", SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Press Enter to Restart", SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 + 30);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Breakout Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        frame.add(new BreakoutGame());
        frame.setVisible(true);
    }
}

