import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class CatchTheApplesGame extends JFrame {
    private int basketX = 350;
    private final int basketY = 550;
    private final int basketWidth = 100;
    private final int basketHeight = 20;
    private final int itemSize = 20;
    private final int windowWidth = 800;
    private final int windowHeight = 600;
    private final List<Apple> apples = new ArrayList<>();
    private final List<Bomb> bombs = new ArrayList<>();
    private int score = 0;
    private boolean gameOver = false;
    private Timer timer;
    private Clip backgroundMusicClip;
    private JDialog gameOverDialog;

    public CatchTheApplesGame() {
        setTitle("Catch the Apples Game");
        setSize(windowWidth, windowHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gameOver) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT && basketX > 0) {
                        basketX -= 20;
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && basketX < windowWidth - basketWidth) {
                        basketX += 20;
                    }
                }
            }
        });

        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGame();
                gamePanel.repaint();
            }
        });
        timer.start();

        playBackgroundMusic("background_music.wav");
    }

    private void updateGame() {
        if (!gameOver) {
            Random random = new Random();
            if (random.nextInt(20) == 0) {
                apples.add(new Apple(random.nextInt(windowWidth - itemSize), 0, itemSize, itemSize));
            }
            if (random.nextInt(100) == 0) {
                bombs.add(new Bomb(random.nextInt(windowWidth - itemSize), 0, itemSize, itemSize));
            }

            List<Apple> caughtApples = new ArrayList<>();
            for (Apple apple : apples) {
                apple.y += 5;
                if (apple.y > windowHeight) {
                    caughtApples.add(apple);
                } else if (apple.y + itemSize >= basketY && apple.x + itemSize >= basketX && apple.x <= basketX + basketWidth) {
                    caughtApples.add(apple);
                    score++;
                    playSound("apple-106213.mp3");
                }
            }
            apples.removeAll(caughtApples);

            List<Bomb> caughtBombs = new ArrayList<>();
            for (Bomb bomb : bombs) {
                bomb.y += 5;
                if (bomb.y > windowHeight) {
                    caughtBombs.add(bomb);
                } else if (bomb.y + itemSize >= basketY && bomb.x + itemSize >= basketX && bomb.x <= basketX + basketWidth) {
                    caughtBombs.add(bomb);
                    gameOver = true;
                    playSound("explosion-80108.mp3");
                    stopBackgroundMusic();
                    showGameOverDialog();
                }
            }
            bombs.removeAll(caughtBombs);
        }
    }

    private void playSound(String soundFile) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundFile).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void playBackgroundMusic(String musicFile) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(musicFile).getAbsoluteFile());
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioInputStream);
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }

    private void showGameOverDialog() {
        SwingUtilities.invokeLater(() -> {
            gameOverDialog = new JDialog(this, "Game Over", true);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JLabel messageLabel = new JLabel("Game Over! Your score: " + score);
            messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(messageLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());

            JButton restartButton = new JButton("Restart");
            restartButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    restartGame();
                    gameOverDialog.dispose();
                }
            });

            JButton exitButton = new JButton("Exit");
            exitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

            buttonPanel.add(restartButton);
            buttonPanel.add(exitButton);

            panel.add(buttonPanel);

            gameOverDialog.getContentPane().add(panel);
            gameOverDialog.pack();
            gameOverDialog.setLocationRelativeTo(this);
            gameOverDialog.setVisible(true);
        });
    }

    private void restartGame() {
        apples.clear();
        bombs.clear();
        score = 0;
        gameOver = false;
        basketX = 350;
        playBackgroundMusic("floating-abstract-142819.mp3");
        timer.start();
    }

    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!gameOver) {
                g.setColor(Color.BLUE);
                g.fillRect(basketX, basketY, basketWidth, basketHeight);

                g.setColor(Color.RED);
                for (Apple apple : apples) {
                    g.fillOval(apple.x, apple.y, apple.width, apple.height);
                }

                g.setColor(Color.BLACK);
                for (Bomb bomb : bombs) {
                    g.fillOval(bomb.x, bomb.y, bomb.width, bomb.height);
                }

                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString("Score: " + score, 10, 20);
            } else {
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 36));
                g.drawString("Game Over", 300, 300);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString("Final Score: " + score, 350, 350);
            }
        }
    }

    private static class Apple extends Rectangle {
        public Apple(int x, int y, int width, int height) {
            super(x, y, width, height);
        }
    }

    private static class Bomb extends Rectangle {
        public Bomb(int x, int y, int width, int height) {
            super(x, y, width, height);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CatchTheApplesGame game = new CatchTheApplesGame();
            game.setVisible(true);
        });
    }
}
