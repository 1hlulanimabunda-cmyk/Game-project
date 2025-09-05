package com.mycompany.graphicalmazegameenhanced;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import java.util.ArrayList;

/**
 * Graphical Maze Game: The Cursed Labyrinth (Enhanced Graphics & Level 2 Exit)
 * 
 * Updates:
 * - Level 2: Added exit door ('E') at (8,7) to transition to Level 3 after sealing Altar ('S').
 * - Level 2 Environment: Lighter forest green ground (50,150,50), brighter vine walls.
 * - Graphics: Subtle ground patterns (grass/roots/stars), 3D brick walls, textured vines, glowing stones, detailed sprites (belt, staff, eyes), enhanced glow (8 particles, outer ring), distinct exit door (golden frame).
 * - Storyline: Same as before, with updated story log for Level 2 exit.
 * - Controls: WASD/Arrows, SPACE to interact, H for help, V to save, L to load.
 */
public class GraphicalMazeGameEnhanced extends JFrame implements ActionListener {

    private static final int CELL_SIZE = 50;
    private static final int ROWS = 10;
    private static final int COLS = 10;
    private static final int MONSTER_MOVE_DELAY = 300;
    private static final int GLOW_ANIMATION_SPEED = 80;

    private int currentLevel = 1;
    private final int MAX_LEVEL = 3;

    private char[][] level1Maze = {
        {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
        {'#', 'P', '.', '.', '#', '.', '.', '.', '.', '#'},
        {'#', '.', '#', '.', '.', 'M', '#', '#', '.', '#'},
        {'#', '.', '#', '#', '#', '.', '#', '.', '.', '#'},
        {'#', '.', 'A', 'G', '#', '.', '#', '.', '#', '#'},
        {'#', '#', '.', '.', '#', '.', '.', '.', '.', '#'},
        {'#', '.', '#', '#', '#', '#', '#', '.', '#', '#'},
        {'#', '.', '.', '.', '.', '.', '#', '.', '.', '#'},
        {'#', '#', '#', '#', '#', '.', '#', 'M', 'E', '#'},
        {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#'}
    };

    private char[][] level2Maze = {
        {'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W'},
        {'W', 'P', 'T', '.', 'W', '.', 'T', '.', '.', 'W'},
        {'W', '.', 'W', '.', '.', 'M', 'W', 'W', 'T', 'W'},
        {'W', 'T', 'W', 'W', 'W', '.', 'W', '.', '.', 'W'},
        {'W', '.', '.', 'G', 'W', 'T', 'W', '.', 'W', 'W'},
        {'W', 'W', '.', '.', 'W', '.', '.', 'T', '.', 'W'},
        {'W', '.', 'W', 'W', 'W', 'W', 'W', '.', 'W', 'W'},
        {'W', '.', 'T', '.', '.', '.', 'W', 'M', '.', 'W'},
        {'W', 'W', 'W', 'W', 'W', '.', 'W', 'E', 'S', 'W'},
        {'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W'}
    };

    private char[][] level3Maze = {
        {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
        {'#', 'P', '.', 'T', '#', '.', '.', '.', '.', '#'},
        {'#', '.', '#', '.', '.', 'M', '#', 'T', '.', '#'},
        {'#', 'T', '#', '#', '#', '.', '#', '.', '.', '#'},
        {'#', '.', '.', 'G', '#', 'T', '#', '.', '#', '#'},
        {'#', '#', '.', '.', '#', '.', '.', 'T', '.', '#'},
        {'#', '.', '#', '#', '#', '#', '#', '.', '#', '#'},
        {'#', '.', 'T', '.', '.', '.', '#', 'M', '.', '#'},
        {'#', '#', '#', '#', '#', '.', '#', 'M', 'C', '#'},
        {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#'}
    };

    private char[][] maze;

    private int playerX = 1;
    private int playerY = 1;
    private int playerFacing = 2;
    private ArrayList<int[]> monsters = new ArrayList<>();
    private int[] sagePos = {4, 3};
    private boolean hasObjectiveItem = false;
    private int sageInteractionStage = 0;
    private String currentObjective = "Find the Sage for guidance on the curse.";
    private Random random = new Random();
    private Timer monsterTimer;
    private Timer glowTimer;
    private float glowAlpha = 0.5f;
    private boolean glowIncreasing = true;
    private GamePanel gamePanel;
    private JTextArea storyLog;

    public GraphicalMazeGameEnhanced() {
        setTitle("The Cursed Labyrinth - Enhanced");
        setLayout(new BorderLayout());

        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        storyLog = new JTextArea();
        storyLog.setEditable(false);
        storyLog.setWrapStyleWord(true);
        storyLog.setLineWrap(true);
        storyLog.setFont(new Font("Serif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(storyLog);
        scrollPane.setPreferredSize(new Dimension(COLS * CELL_SIZE, 150));
        add(scrollPane, BorderLayout.SOUTH);

        setSize(COLS * CELL_SIZE + 16, (ROWS * CELL_SIZE + 150) + 39);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
        setFocusable(true);

        loadLevel(1);

        monsterTimer = new Timer(MONSTER_MOVE_DELAY, this);
        monsterTimer.start();
        glowTimer = new Timer(GLOW_ANIMATION_SPEED, e -> {
            if (glowIncreasing) {
                glowAlpha += 0.07f;
                if (glowAlpha >= 0.9f) glowIncreasing = false;
            } else {
                glowAlpha -= 0.07f;
                if (glowAlpha <= 0.3f) glowIncreasing = true;
            }
            gamePanel.repaint();
        });
        glowTimer.start();

        setVisible(true);
    }

    private void loadLevel(int level) {
        try {
            currentLevel = level;
            hasObjectiveItem = false;
            sageInteractionStage = 0;
            monsters.clear();

            maze = new char[ROWS][COLS];
            char[][] sourceMaze = level == 1 ? level1Maze : level == 2 ? level2Maze : level3Maze;
            for (int i = 0; i < ROWS; i++) {
                maze[i] = sourceMaze[i].clone();
            }

            if (level == 1) {
                monsters.add(new int[]{2, 5, 2});
                monsters.add(new int[]{8, 7, 2});
                sagePos = new int[]{4, 3};
                currentObjective = "Find the Sage for guidance on the curse.";
                appendToStoryLog("Level 1: The Cursed Labyrinth\nJournal Entry: I am Elara, seeking the Crystal of Eternity in the Cursed Labyrinth, where cursed guardians roam. A Sage may guide me.\n");
                appendToStoryLog("Controls: WASD/Arrows to move, SPACE to interact, H for help, V to save, L to load.\n");
                appendToStoryLog("Current Objective: " + currentObjective + "\n");
                addRandomDecorations(5);
            } else if (level == 2) {
                monsters.add(new int[]{2, 5, 2});
                monsters.add(new int[]{7, 7, 2});
                monsters.add(new int[]{5, 3, 1});
                sagePos = new int[]{4, 3};
                currentObjective = "Find the Ancient Altar ('S') to seal the curse.";
                appendToStoryLog("Level 2: The Enchanted Forest\nThe Crystal reveals the curse's source: an Ancient Altar in the Enchanted Forest. Seal it and find the exit door to proceed, but beware agile forest spirits and treacherous waters.\n");
                appendToStoryLog("Current Objective: " + currentObjective + "\n");
                addRandomDecorations(10);
            } else if (level == 3) {
                monsters.add(new int[]{2, 5, 2});
                monsters.add(new int[]{7, 7, 2});
                monsters.add(new int[]{5, 3, 1});
                monsters.add(new int[]{3, 8, 3});
                sagePos = new int[]{4, 3};
                currentObjective = "Place the Crystal at the Celestial Spire ('C').";
                appendToStoryLog("Level 3: The Celestial Ruins\nThe Crystal unveils the curse's true origin: a corrupted Celestial Spire in ancient ruins. Place the Crystal there to end the curse and restore cosmic balance, but beware the swift Celestial Wraiths.\n");
                appendToStoryLog("Current Objective: " + currentObjective + "\n");
                addRandomDecorations(8);
            }

            playerX = 1;
            playerY = 1;
            playerFacing = 2;
            gamePanel.repaint();
        } catch (Exception e) {
            appendToStoryLog("Error loading level: " + e.getMessage() + "\n");
        }
    }

    private void addRandomDecorations(int count) {
        try {
            for (int i = 0; i < count; i++) {
                int rx = random.nextInt(ROWS);
                int ry = random.nextInt(COLS);
                if (maze[rx][ry] == '.' && !(rx == 4 && ry == 2) && !(rx == 8 && ry == 8) && !(rx == 8 && ry == 7)) {
                    maze[rx][ry] = 'T';
                }
            }
        } catch (Exception e) {
            appendToStoryLog("Error adding decorations: " + e.getMessage() + "\n");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == monsterTimer) {
            moveMonsters();
            checkStoryTriggers();
            if (isPlayerOnMonster()) {
                loseGame();
            }
            gamePanel.repaint();
        }
    }

    private void handleKeyPress(KeyEvent e) {
        int key = e.getKeyCode();
        int newX = playerX;
        int newY = playerY;
        int newFacing = playerFacing;

        switch (key) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP: newX--; newFacing = 0; break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT: newY--; newFacing = 3; break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN: newX++; newFacing = 2; break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: newY++; newFacing = 1; break;
            case KeyEvent.VK_SPACE: interactWithSage(); return;
            case KeyEvent.VK_V: saveGame(); return;
            case KeyEvent.VK_L: loadGame(); return;
            case KeyEvent.VK_H: showHelp(); return;
            default: return;
        }

        if (isValidMove(newX, newY)) {
            char targetCell = maze[newX][newY];
            if ((currentLevel == 1 && targetCell == 'A') || (currentLevel == 2 && targetCell == 'S') || (currentLevel == 3 && targetCell == 'C')) {
                hasObjectiveItem = true;
                maze[newX][newY] = '.';
                String itemName = currentLevel == 1 ? "Crystal of Eternity" : currentLevel == 2 ? "Ancient Altar Seal" : "Celestial Spire Placement";
                appendToStoryLog("You acquired the " + itemName + "! Power surges through you.\nNew Objective: Find the exit door.\n");
                currentObjective = "Find the exit door.";
            }

            if (targetCell == 'E') {
                if (hasObjectiveItem) {
                    if (currentLevel < MAX_LEVEL) {
                        loadLevel(currentLevel + 1);
                    } else {
                        winGame();
                    }
                    return;
                } else {
                    appendToStoryLog("The exit door is sealed without the required item. Find it first!\n");
                    return;
                }
            }

            if (isMonsterAt(newX, newY)) {
                loseGame();
                return;
            }

            char underlying = maze[playerX][playerY];
            maze[playerX][playerY] = (underlying == 'P') ? '.' : underlying;
            playerX = newX;
            playerY = newY;
            playerFacing = newFacing;
            maze[playerX][playerY] = 'P';

            if (isPlayerOnMonster()) {
                loseGame();
            }

            gamePanel.repaint();
        }
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < ROWS && y >= 0 && y < COLS && maze[x][y] != '#' && maze[x][y] != 'W' && maze[x][y] != 'G';
    }

    private void moveMonsters() {
        try {
            int[][] directions = {{-1, 0, 0}, {0, 1, 1}, {1, 0, 2}, {0, -1, 3}};
            for (int[] monster : monsters) {
                if (maze[monster[0]][monster[1]] == 'M') {
                    maze[monster[0]][monster[1]] = '.';
                }

                int dirIdx = random.nextInt(directions.length);
                int[] dir = directions[dirIdx];
                int newX = monster[0] + dir[0];
                int newY = monster[1] + dir[1];
                int newFacing = dir[2];

                if (isValidMove(newX, newY) && !isMonsterAt(newX, newY) && !isPlayerAt(newX, newY) && maze[newX][newY] != 'G') {
                    monster[0] = newX;
                    monster[1] = newY;
                    monster[2] = newFacing;
                }

                if (maze[monster[0]][monster[1]] != 'A' && maze[monster[0]][monster[1]] != 'S' && maze[monster[0]][monster[1]] != 'C' && maze[monster[0]][monster[1]] != 'E') {
                    maze[monster[0]][monster[1]] = 'M';
                }
            }
        } catch (Exception e) {
            appendToStoryLog("Error moving monsters: " + e.getMessage() + "\n");
        }
    }

    private boolean isMonsterAt(int x, int y) {
        for (int[] monster : monsters) {
            if (monster[0] == x && monster[1] == y) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlayerOnMonster() {
        return isMonsterAt(playerX, playerY);
    }

    private boolean isPlayerAt(int x, int y) {
        return x == playerX && y == playerY;
    }

    private void interactWithSage() {
        int dx = Math.abs(playerX - sagePos[0]);
        int dy = Math.abs(playerY - sagePos[1]);
        if (dx <= 1 && dy <= 1 && (dx + dy > 0)) {
            String message = "";
            switch (sageInteractionStage) {
                case 0:
                    if (currentLevel == 1) {
                        message = "Sage: 'Greetings, Elara. I survived the curse. Kings sealed the Crystal here, cursing seekers. Avoid the guardians.'\nObjective: Seek the Crystal.";
                        currentObjective = "Collect the Crystal of Eternity.";
                    } else if (currentLevel == 2) {
                        message = "Sage's Spirit: 'Elara, the Crystal led you here. Seal the Altar and find the exit door to proceed.'\nObjective: Find the Altar.";
                        currentObjective = "Find the Ancient Altar ('S').";
                    } else {
                        message = "Celestial Sage: 'Elara, the Crystal has brought you to the Celestial Ruins. Place it in the Spire to end the curse.'\nObjective: Find the Spire.";
                        currentObjective = "Place the Crystal at the Celestial Spire ('C').";
                    }
                    sageInteractionStage = 1;
                    break;
                case 1:
                    message = currentLevel == 1 ? "Sage: 'The Crystal weakens the curse. Reach the exit door.'" :
                             currentLevel == 2 ? "Sage's Spirit: 'The Altar is near. Seal it and find the exit door.'" :
                             "Celestial Sage: 'The Spire awaits. Place the Crystal and end this.'";
                    sageInteractionStage = 2;
                    break;
                case 2:
                    message = "Sage: 'You're close, Elara. With the item, find the exit door.'";
                    sageInteractionStage = 3;
                    break;
                case 3:
                    message = "Sage: 'Go now, your destiny awaits.'";
                    break;
            }
            appendToStoryLog(message + "\n");
        }
    }

    private void checkStoryTriggers() {
        int objX = currentLevel == 1 ? 4 : 8;
        int objY = currentLevel == 1 ? 2 : 8;
        if (!hasObjectiveItem && Math.abs(playerX - objX) <= 2 && Math.abs(playerY - objY) <= 2) {
            if (random.nextInt(10) == 0) {
                appendToStoryLog("A radiant glow pulses nearby... the objective is close.\n");
            }
        }
        if (Math.abs(playerX - 8) <= 2 && Math.abs(playerY - (currentLevel == 2 ? 7 : 8)) <= 2) {
            if (random.nextInt(10) == 0) {
                appendToStoryLog("The air hums near the exit door. Cosmic whispers urge you forward.\n");
            }
        }
    }

    private void showHelp() {
        String helpText = "Help Menu:\n" +
                          "Controls: WASD/Arrows to move.\n" +
                          "SPACE: Interact with Sage.\n" +
                          "V: Save game.\n" +
                          "L: Load game.\n" +
                          "H: Show this help.\n\n" +
                          "Story: Elara seeks to end a cosmic curse. Level 1: Find Crystal. Level 2: Seal Altar, find exit. Level 3: Place Crystal in Spire.\n" +
                          "Current Level: " + currentLevel + "\n" +
                          "Current Objective: " + currentObjective + "\n";
        JOptionPane.showMessageDialog(this, helpText);
    }

    private void winGame() {
        appendToStoryLog("Final Epilogue: The Crystal ignites the Celestial Spire, shattering the curse. Light floods the ruins, and the stars align in harmony. Elara, now a cosmic guardian, sees visions of new realms to explore. Her legend will echo through the ages.\n");
        JOptionPane.showMessageDialog(this, "Congratulations! You ended the curse and restored balance. Elara's saga continues...");
        System.exit(0);
    }

    private void loseGame() {
        appendToStoryLog("Tragic End: A wraith's grasp consumes you. The curse claims another soul, and Elara fades into the cosmic void.\n");
        JOptionPane.showMessageDialog(this, "Game Over: You have been cursed.");
        System.exit(0);
    }

    private void appendToStoryLog(String text) {
        storyLog.append(text);
        storyLog.setCaretPosition(storyLog.getDocument().getLength());
    }

    private void saveGame() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("maze_save.txt"))) {
            writer.println(currentLevel);
            writer.println(playerX + "," + playerY + "," + playerFacing);
            writer.println(hasObjectiveItem);
            writer.println(sageInteractionStage);
            writer.println(monsters.size());
            for (int[] monster : monsters) {
                writer.println(monster[0] + "," + monster[1] + "," + monster[2]);
            }
            for (char[] row : maze) {
                for (char cell : row) {
                    writer.print(cell);
                }
                writer.println();
            }
            writer.println(currentObjective);
            JOptionPane.showMessageDialog(this, "Game saved successfully!");
        } catch (IOException ex) {
            appendToStoryLog("Error saving game: " + ex.getMessage() + "\n");
        }
    }

    private void loadGame() {
        try (BufferedReader reader = new BufferedReader(new FileReader("maze_save.txt"))) {
            currentLevel = Integer.parseInt(reader.readLine());
            String[] playerData = reader.readLine().split(",");
            playerX = Integer.parseInt(playerData[0]);
            playerY = Integer.parseInt(playerData[1]);
            playerFacing = Integer.parseInt(playerData[2]);
            hasObjectiveItem = Boolean.parseBoolean(reader.readLine());
            sageInteractionStage = Integer.parseInt(reader.readLine());
            int monsterCount = Integer.parseInt(reader.readLine());
            monsters.clear();
            for (int i = 0; i < monsterCount; i++) {
                String[] monsterData = reader.readLine().split(",");
                monsters.add(new int[]{Integer.parseInt(monsterData[0]), Integer.parseInt(monsterData[1]), Integer.parseInt(monsterData[2])});
            }
            maze = new char[ROWS][COLS];
            char[][] sourceMaze = currentLevel == 1 ? level1Maze : currentLevel == 2 ? level2Maze : level3Maze;
            for (int i = 0; i < ROWS; i++) {
                String line = reader.readLine();
                maze[i] = line.toCharArray();
            }
            currentObjective = reader.readLine();
            JOptionPane.showMessageDialog(this, "Game loaded successfully!");
            gamePanel.repaint();
            appendToStoryLog("Game loaded. Current Level: " + currentLevel + ". Objective: " + currentObjective + "\n");
        } catch (IOException | NumberFormatException ex) {
            appendToStoryLog("Error loading game: " + ex.getMessage() + "\n");
        }
    }

    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    int x = j * CELL_SIZE;
                    int y = i * CELL_SIZE;

                    // Ground with patterns
                    if (currentLevel == 1) {
                        g2d.setPaint(new GradientPaint(x, y, new Color(144, 238, 144), x + CELL_SIZE, y + CELL_SIZE, new Color(100, 200, 100)));
                        g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        g2d.setColor(new Color(80, 160, 80, 100));
                        g2d.fillOval(x + 10, y + 10, 5, 5);
                    } else if (currentLevel == 2) {
                        g2d.setPaint(new GradientPaint(x, y, new Color(50, 150, 50), x + CELL_SIZE, y + CELL_SIZE, new Color(30, 100, 30)));
                        g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        g2d.setColor(new Color(100, 80, 60, 100));
                        g2d.fillRect(x + 15, y + 15, 5, 5);
                    } else {
                        g2d.setPaint(new GradientPaint(x, y, new Color(0, 50, 100), x + CELL_SIZE, y + CELL_SIZE, new Color(0, 20, 50)));
                        g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        g2d.setColor(new Color(255, 255, 255, 100));
                        g2d.fillOval(x + 20, y + 20, 3, 3);
                    }

                    char cell = maze[i][j];
                    if (cell == '#' || cell == 'W') {
                        boolean isBuilding = currentLevel == 1 && (i + j) % 2 == 0;
                        drawWall(g2d, x, y, cell == 'W' ? false : isBuilding);
                    } else if (cell == 'T') {
                        drawDecoration(g2d, x, y);
                    } else if (cell == 'A' || cell == 'S' || cell == 'C') {
                        if (!hasObjectiveItem && Math.abs(playerX - i) <= 2 && Math.abs(playerY - j) <= 2) {
                            g2d.setColor(new Color(1.0f, 1.0f, 0.0f, glowAlpha * 0.5f));
                            g2d.fillOval(x - 20, y - 20, CELL_SIZE + 40, CELL_SIZE + 40);
                            g2d.setColor(new Color(1.0f, 1.0f, 0.0f, glowAlpha));
                            g2d.fillOval(x - 15, y - 15, CELL_SIZE + 30, CELL_SIZE + 30);
                            drawParticles(g2d, x, y);
                        }
                        drawObjectiveItem(g2d, x, y, cell);
                    } else if (cell == 'E') {
                        drawExit(g2d, x, y);
                    } else if (cell == 'G') {
                        drawPerson(g2d, x, y, Color.MAGENTA, 2, true);
                    } else if (cell == 'P') {
                        drawPerson(g2d, x, y, Color.BLUE, playerFacing, false);
                    } else if (cell == 'M') {
                        for (int[] monster : monsters) {
                            if (monster[0] == i && monster[1] == j) {
                                Color monsterColor = currentLevel == 1 ? Color.RED : currentLevel == 2 ? new Color(0, 100, 0) : new Color(0, 150, 255);
                                drawPerson(g2d, x, y, monsterColor, monster[2], false);
                                break;
                            }
                        }
                    }

                    if (cell == '.' || cell == 'T' || cell == 'P' || cell == 'M') {
                        if ((i + j) % 3 == 0) {
                            g2d.setColor(currentLevel == 1 ? new Color(169, 169, 169) : new Color(139, 69, 19));
                            g2d.fillRect(x + 10, y + 20, CELL_SIZE - 20, 10);
                        }
                    }
                }
            }
        }

        private void drawWall(Graphics2D g, int x, int y, boolean isBuilding) {
            if (currentLevel == 2 || !isBuilding) {
                g.setPaint(new GradientPaint(x, y, new Color(60, 160, 60), x + CELL_SIZE, y + CELL_SIZE, new Color(30, 100, 30)));
                g.fillOval(x, y, CELL_SIZE, CELL_SIZE);
                g.setColor(new Color(0, 120, 0, 150));
                g.fillOval(x + 10, y + 10, CELL_SIZE - 20, CELL_SIZE - 20);
                g.setColor(new Color(0, 80, 0, 100));
                g.drawLine(x + 15, y + 15, x + 35, y + 35);
            } else if (currentLevel == 3) {
                g.setPaint(new GradientPaint(x, y, new Color(120, 120, 180), x + CELL_SIZE, y + CELL_SIZE, new Color(70, 70, 120)));
                g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                g.setColor(new Color(200, 200, 255, 150));
                g.fillOval(x + 5, y + 5, 10, 10);
                g.fillOval(x + 35, y + 35, 10, 10);
                g.setColor(new Color(255, 255, 255, 50));
                g.drawRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
            } else {
                g.setPaint(new GradientPaint(x, y, new Color(139, 69, 19), x + CELL_SIZE, y + CELL_SIZE, new Color(100, 50, 10)));
                g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                g.setColor(new Color(80, 40, 0));
                g.drawLine(x + 2, y + 2, x + CELL_SIZE - 2, y + 2);
                g.drawLine(x + 2, y + 2, x + 2, y + CELL_SIZE - 2);
                g.setColor(Color.YELLOW);
                g.fillRect(x + 10, y + 10, 10, 10);
                g.fillRect(x + 30, y + 30, 10, 10);
            }
        }

        private void drawDecoration(Graphics2D g, int x, int y) {
            if (currentLevel == 3) {
                g.setColor(new Color(0, 200, 255, 150));
                g.fillOval(x + 15, y + 15, 20, 20);
                g.setColor(new Color(255, 255, 255, 100));
                g.fillOval(x + 20, y + 20, 10, 10);
            } else {
                g.setColor(new Color(139, 69, 19));
                g.fillRect(x + 20, y + 30, 10, 20);
                g.setColor(Color.GREEN);
                g.fillOval(x + 5, y + 5, 40, 40);
            }
        }

        private void drawObjectiveItem(Graphics2D g, int x, int y, char type) {
            g.setColor(type == 'A' ? Color.YELLOW : type == 'S' ? Color.WHITE : new Color(255, 200, 0));
            int[] xp = {x + 25, x + 10, x + 40};
            int[] yp = {y + 10, y + 40, y + 40};
            g.fillPolygon(xp, yp, 3);
        }

        private void drawExit(Graphics2D g, int x, int y) {
            g.setPaint(new GradientPaint(x, y, new Color(0, 100, 0), x + 40, y + 40, new Color(0, 150, 0)));
            g.fillRect(x + 10, y + 10, 30, 40);
            g.setColor(new Color(255, 215, 0));
            g.drawRect(x + 8, y + 8, 34, 44);
            g.setColor(new Color(255, 255, 0, (int)(glowAlpha * 255)));
            g.fillOval(x + 20, y + 30, 5, 5);
        }

        private void drawPerson(Graphics2D g, int x, int y, Color color, int facing, boolean isSage) {
            g.setColor(color);
            g.fillOval(x + 15, y + 5, 20, 20);
            g.setColor(new Color(255, 220, 200));
            g.fillOval(x + 18, y + 8, 14, 14);
            g.setColor(color);
            g.fillRect(x + 22, y + 25, 6, 15);
            g.drawLine(x + 25, y + 28, x + 15, y + 23);
            g.drawLine(x + 25, y + 28, x + 35, y + 23);
            g.drawLine(x + 24, y + 40, x + 20, y + 45);
            g.drawLine(x + 26, y + 40, x + 30, y + 45);
            if (isSage) {
                g.setColor(new Color(200, 0, 200, 150));
                g.fillPolygon(new int[]{x + 15, x + 25, x + 35}, new int[]{y + 25, y + 40, y + 25}, 3);
                g.setColor(Color.GRAY);
                g.fillRect(x + 23, y + 10, 4, 10);
            } else {
                g.setColor(new Color(150, 150, 150, 150));
                g.fillRect(x + 20, y + 25, 10, 10);
                g.setColor(Color.BLACK);
                g.fillRect(x + 22, y + 30, 6, 2);
            }
            if (currentLevel == 3 && !isSage && color != Color.BLUE) {
                g.setColor(new Color(0, 255, 255, 100));
                g.fillOval(x + 10, y, 30, 30);
                g.setColor(Color.WHITE);
                g.fillOval(x + 20, y + 10, 4, 4);
                g.fillOval(x + 26, y + 10, 4, 4);
            }
            g.setColor(Color.BLACK);
            switch (facing) {
                case 0: g.drawLine(x + 25, y + 15, x + 25, y + 5); break;
                case 1: g.drawLine(x + 25, y + 15, x + 35, y + 15); break;
                case 2: g.drawLine(x + 25, y + 15, x + 25, y + 25); break;
                case 3: g.drawLine(x + 25, y + 15, x + 15, y + 15); break;
            }
        }

        private void drawParticles(Graphics2D g, int x, int y) {
            g.setColor(new Color(1.0f, 1.0f, 0.0f, 0.5f));
            for (int i = 0; i < 8; i++) {
                int px = x + 25 + random.nextInt(20) - 10;
                int py = y + 25 + random.nextInt(20) - 10;
                g.fillOval(px, py, 5, 5);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GraphicalMazeGameEnhanced::new);
    }
}