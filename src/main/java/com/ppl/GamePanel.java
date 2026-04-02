package com.ppl;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * 游戏面板 - 核心游戏逻辑和渲染
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {
    
    // 游戏尺寸常量
    public static final int WIDTH = 640;
    public static final int HEIGHT = 720;
    public static final int GRID_ROWS = 16;  // 16 行
    public static final int GRID_COLS = 14;  // 14 列
    
    private Timer timer;
    private Player player;  // 玩家 1（单人模式或双人模式的玩家 1）
    private Player player2;  // 玩家 2（仅双人模式）
    private boolean twoPlayerMode;  // 是否为双人模式
    
    // === 单人模式的游戏状态 ===
    private List<Bubble> singleBubbles = new ArrayList<>();
    private int[][] singleGrid;
    private int score = 0;
    
    // === 双人模式玩家 1 的独立游戏状态 ===
    private List<Bubble> p1Bubbles = new ArrayList<>();
    private int[][] p1Grid;
    private int p1Score = 0;
    
    // === 双人模式玩家 2 的独立游戏状态 ===
    private List<Bubble> p2Bubbles = new ArrayList<>();
    private int[][] p2Grid;
    private int p2Score = 0;
    
    // === 共享的颜色序列（用于双人模式） ===
    // 预先完整生成所有要发射的泡泡颜色序列
    private List<Bubble.BubbleColor> sharedColorSequence = new ArrayList<>();
    
    // 玩家 1 和玩家 2 各自的取球索引
    private int p1NextIndex = 0;  // 玩家 1 下一个要取的球的索引
    private int p2NextIndex = 0;  // 玩家 2 下一个要取的球的索引
    private static final int COLOR_SEQUENCE_SIZE = 50; // 预先生成 50 个颜色
    
    // 公共
    private List<Bubble> animatingBubbles = new ArrayList<>();
    private boolean gameOver = false;
    private String message = "";
    
    public GamePanel(boolean twoPlayerMode) {
        this.twoPlayerMode = twoPlayerMode;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(30, 30, 50));
        setFocusable(true);
        
        // 初始化音效
        SoundManager.initSounds();
        
        initGame();
        
        addKeyListener(this);
        
        // 强制设置焦点
        setFocusTraversalKeysEnabled(false);
        requestFocusInWindow();
        
        timer = new Timer(16, this);
    }
    
    private void initGame() {
        // 清空所有状态
        animatingBubbles.clear();
        
        if (twoPlayerMode) {
            // 双人模式：完全独立的两个游戏系统
            // 先生成统一的颜色布局
            Bubble.BubbleColor[][] sharedColors = generateBubbleColors();
            
            // 生成共享的颜色序列（预先生成完整的颜色序列）
            sharedColorSequence.clear();
            p1NextIndex = 0;
            p2NextIndex = 0;
            for (int i = 0; i < COLOR_SEQUENCE_SIZE; i++) {
                sharedColorSequence.add(getRandomBubbleColor());
            }
            
            initPlayer1System(sharedColors);
            initPlayer2System(sharedColors);
        } else {
            // 单人模式
            initSingleSystem();
        }
        
        // 创建玩家对象
        if (twoPlayerMode) {
            int halfWidth = WIDTH / 2;
            
            // 从共享序列中取出前两个颜色给双方（索引 0 和 1）
            Bubble.BubbleColor currentColor1 = sharedColorSequence.get(0);
            Bubble.BubbleColor nextColor1 = sharedColorSequence.get(1);
            Bubble.BubbleColor currentColor2 = currentColor1; // 玩家 2 的当前球与玩家 1 相同
            Bubble.BubbleColor nextColor2 = nextColor1;     // 玩家 2 的 NEXT 球与玩家 1 相同
            
            // 创建玩家 1
            player = new Player(halfWidth - 150, HEIGHT - 50, new Color(100, 200, 255), 
                               10, halfWidth - 10, currentColor1, nextColor1);
            
            // 创建玩家 2，使用相同的颜色
            player2 = new Player(halfWidth + 150, HEIGHT - 50, new Color(255, 200, 100), 
                                halfWidth + 10, WIDTH - 10, currentColor2, nextColor2);
            
            p1Score = 0;
            p2Score = 0;
        } else {
            player = new Player(WIDTH / 2, HEIGHT - 50);
            player2 = null;
            score = 0;
        }
        
        gameOver = false;
        message = "";
    }
    
    // 初始化单人游戏系统
    private void initSingleSystem() {
        singleBubbles.clear();
        singleGrid = new int[GRID_ROWS][GRID_COLS];
        for (int[] row : singleGrid) Arrays.fill(row, -1);
        
        // 初始化顶部 6 行泡泡
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int x = getGridX(col, row);
                int y = 50 + row * (Bubble.DIAMETER - 5);
                
                Bubble.BubbleColor[] colors = Bubble.BubbleColor.values();
                Bubble.BubbleColor color = colors[(int) (Math.random() * colors.length)];
                Bubble bubble = new Bubble(x, y, color);
                
                singleBubbles.add(bubble);
                singleGrid[row][col] = singleBubbles.size() - 1;
            }
        }
    }
    
    // 初始化双人模式玩家 1 的系统
    private void initPlayer1System(Bubble.BubbleColor[][] colors) {
        p1Bubbles.clear();
        p1Grid = new int[GRID_ROWS][GRID_COLS / 2];
        for (int[] row : p1Grid) Arrays.fill(row, -1);
        
        // 使用共享的颜色布局
        // 玩家 1 的泡泡（左半部分）
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < GRID_COLS / 2; col++) {
                int offsetXForRow = (row % 2 == 1) ? (Bubble.DIAMETER / 2) : 0;
                int x = 10 + offsetXForRow + col * Bubble.DIAMETER + Bubble.RADIUS;
                int y = 50 + row * (Bubble.DIAMETER - 5);
                
                Bubble bubble = new Bubble(x, y, colors[row][col]);
                p1Bubbles.add(bubble);
                p1Grid[row][col] = p1Bubbles.size() - 1;
            }
        }
        
        // 注意：玩家 1 的发射器对象在 initGame() 中统一创建，使用共享颜色序列
    }
    
    // 获取随机泡泡颜色
    private Bubble.BubbleColor getRandomBubbleColor() {
        Bubble.BubbleColor[] colors = Bubble.BubbleColor.values();
        return colors[(int) (Math.random() * colors.length)];
    }
    
    // 初始化双人模式玩家 2 的系统
    private void initPlayer2System(Bubble.BubbleColor[][] colors) {
        p2Bubbles.clear();
        p2Grid = new int[GRID_ROWS][GRID_COLS / 2];
        for (int[] row : p2Grid) Arrays.fill(row, -1);
        
        // 使用与玩家 1 相同的颜色布局
        // 玩家 2 的泡泡（右半部分）
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < GRID_COLS / 2; col++) {
                int offsetXForRow = (row % 2 == 1) ? (Bubble.DIAMETER / 2) : 0;
                int x = WIDTH / 2 + 10 + offsetXForRow + col * Bubble.DIAMETER + Bubble.RADIUS;
                int y = 50 + row * (Bubble.DIAMETER - 5);
                
                Bubble bubble = new Bubble(x, y, colors[row][col]);
                p2Bubbles.add(bubble);
                p2Grid[row][col] = p2Bubbles.size() - 1;
            }
        }
    }
    
    // 生成泡泡颜色布局
    private Bubble.BubbleColor[][] generateBubbleColors() {
        Bubble.BubbleColor[][] colors = new Bubble.BubbleColor[6][GRID_COLS / 2];
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < GRID_COLS / 2; col++) {
                Bubble.BubbleColor[] allColors = Bubble.BubbleColor.values();
                colors[row][col] = allColors[(int) (Math.random() * allColors.length)];
            }
        }
        return colors;
    }
    
    private void update() {
        if (gameOver) return;
        
        if (twoPlayerMode) {
            // 双人模式：完全独立更新两个玩家
            updatePlayerBubble(player, p1Bubbles, p1Grid);
            updatePlayerBubble(player2, p2Bubbles, p2Grid);
        } else {
            // 单人模式
            updatePlayerBubble(player, singleBubbles, singleGrid);
        }
        
        checkGameOver();
    }
    
    // 更新单个玩家的泡泡
    private void updatePlayerBubble(Player p, List<Bubble> bubbles, int[][] grid) {
        if (!p.isShooting()) return;
        
        if (checkCollision(p)) {
            placeBubble(p, bubbles, grid);
        } else if (p.updateShooting()) {
            placeBubble(p, bubbles, grid);
        }
    }
    
    // 检查玩家泡泡碰撞
    private boolean checkCollision(Player p) {
        Bubble bubble = p.getShootingBubble();
        if (bubble == null) return false;
        
        List<Bubble> targetBubbles = (p == player2) ? p2Bubbles : 
                                     (twoPlayerMode ? p1Bubbles : singleBubbles);
        
        for (Bubble b : targetBubbles) {
            if (b != null) {
                double dist = Math.sqrt(
                    Math.pow(bubble.getX() - b.getX(), 2) + 
                    Math.pow(bubble.getY() - b.getY(), 2)
                );
                if (dist < Bubble.DIAMETER * 0.9) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // 放置玩家泡泡到网格
    private void placeBubble(Player p, List<Bubble> bubbles, int[][] grid) {
        p.setShooting(false);
        Bubble bubble = p.getAndClearShootingBubble();
        if (bubble == null) return;
        
        int[] pos = findNearestEmptyPosition(bubble, grid);
        int row = pos[0], col = pos[1];
        
        if (row >= 0 && row < GRID_ROWS && col >= 0 && col < grid[0].length) {
            // 计算坐标
            int x, y;
            if (p == player2) {
                int offsetX = (row % 2 == 1) ? (Bubble.DIAMETER / 2) : 0;
                x = WIDTH / 2 + 10 + offsetX + col * Bubble.DIAMETER + Bubble.RADIUS;
            } else {
                int offsetX = (row % 2 == 1) ? (Bubble.DIAMETER / 2) : 0;
                x = p == player ? (10 + offsetX + col * Bubble.DIAMETER + Bubble.RADIUS) : 
                                 getGridX(col, row);
            }
            y = 50 + row * (Bubble.DIAMETER - 5);
            
            bubble.setX(x);
            bubble.setY(y);
            bubbles.add(bubble);
            grid[row][col] = bubbles.size() - 1;
            
            checkMatches(row, col, p, grid, bubbles);
            
            // 双人模式下同步双方的下一个泡泡颜色
            if (twoPlayerMode) {
                syncNextBubbleColors(p);
            }
        }
    }
    
    // 同步双人模式下双方的下一个泡泡颜色（从共享序列中按索引获取）
    private void syncNextBubbleColors(Player p) {
        // 确定是哪个玩家在发射
        boolean isPlayer1 = (p == player);
        int currentIndex = isPlayer1 ? p1NextIndex : p2NextIndex;
        
        // 当前发射的球的索引
        int shootIndex = currentIndex;
        // 下一个球的索引
        int nextIndex = currentIndex + 1;
        
        if (nextIndex >= sharedColorSequence.size()) {
            // 如果序列不够用了，补充新的颜色
            for (int i = 0; i < COLOR_SEQUENCE_SIZE; i++) {
                sharedColorSequence.add(getRandomBubbleColor());
            }
        }
        
        // 获取下一个球的颜色（固定不变！）
        Bubble.BubbleColor nextColor = sharedColorSequence.get(nextIndex);
        
        // 关键修改：同时更新发射玩家的 currentBubble 和 nextBubble
        // currentBubble 已经是原来的 nextBubble，我们只需要更新 nextBubble 的颜色
        p.setNextBubbleColor(nextColor);
        
        // 更新索引
        if (isPlayer1) {
            p1NextIndex++;
        } else {
            p2NextIndex++;
        }
    }
    
    // 查找最近的空位
    private int[] findNearestEmptyPosition(Bubble bubble, int[][] grid) {
        int bestRow = -1, bestCol = -1;
        double minDist = Double.MAX_VALUE;
        
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] == -1) {
                    int offsetX = (row % 2 == 1) ? (Bubble.DIAMETER / 2) : 0;
                    int gridX = (grid == p2Grid) ? 
                               (WIDTH / 2 + 10 + offsetX + col * Bubble.DIAMETER + Bubble.RADIUS) :
                               (10 + offsetX + col * Bubble.DIAMETER + Bubble.RADIUS);
                    int gridY = 50 + row * (Bubble.DIAMETER - 5);
                    
                    double dist = Math.sqrt(Math.pow(bubble.getX() - gridX, 2) + 
                                          Math.pow(bubble.getY() - gridY, 2));
                    if (dist < minDist) {
                        minDist = dist;
                        bestRow = row;
                        bestCol = col;
                    }
                }
            }
        }
        return new int[]{bestRow, bestCol};
    }
    
    // 计算网格 X 坐标（奇数行偏移）
    private int getGridX(int col, int row) {
        int offsetX = (row % 2 == 1) ? (Bubble.DIAMETER / 2) : 0;
        // 居中显示，左右各留 20 像素边距
        int totalWidth = GRID_COLS * Bubble.DIAMETER;
        int startX = (WIDTH - totalWidth) / 2;
        return startX + offsetX + col * Bubble.DIAMETER + Bubble.RADIUS;
    }
    
    // 检查发射的泡泡是否与其他泡泡碰撞
    // 返回 true 表示发生碰撞
    private boolean checkBubbleCollision() {
        if (twoPlayerMode) {
            // 双人模式需要检查两个玩家的飞行泡泡
            boolean p1Collision = false;
            boolean p2Collision = false;
            
            if (player.getShootingBubble() != null) {
                Bubble shootingBubble = player.getShootingBubble();
                p1Collision = checkSingleBubbleCollision(shootingBubble, p1Bubbles, p1Grid);
            }
            if (player2.getShootingBubble() != null) {
                Bubble shootingBubble = player2.getShootingBubble();
                p2Collision = checkSingleBubbleCollision(shootingBubble, p2Bubbles, p2Grid);
            }
            return p1Collision || p2Collision;
        } else {
            Bubble shootingBubble = player.getShootingBubble();
            return checkSingleBubbleCollision(shootingBubble, singleBubbles, singleGrid);
        }
    }
    
    // 检查单个泡泡的碰撞
    private boolean checkSingleBubbleCollision(Bubble shootingBubble, List<Bubble> targetBubbles, int[][] targetGrid) {
        if (shootingBubble == null) {
            return false;
        }
                
        // 检查是否与现有泡泡碰撞
        for (int i = 0; i < targetBubbles.size(); i++) {
            Bubble bubble = targetBubbles.get(i);
            if (bubble != null) {
                double dist = Math.sqrt(
                    Math.pow(shootingBubble.getX() - bubble.getX(), 2) + 
                    Math.pow(shootingBubble.getY() - bubble.getY(), 2)
                );
                            
                // 如果距离小于两个泡泡半径之和，说明发生碰撞
                if (dist < Bubble.DIAMETER * 0.9) { // 使用直径的 90% 作为阈值
                    return true;
                }
            }
        }
        return false;
    }

    
    // 检查匹配并消除
    private void checkMatches(int startRow, int startCol, Player shooter, int[][] targetGrid, List<Bubble> targetBubbles) {
        if (startRow < 0 || startRow >= GRID_ROWS || startCol < 0 || startCol >= targetGrid[0].length) {
            return;
        }
        
        if (targetGrid[startRow][startCol] == -1) {
            return;
        }
        
        int startIndex = targetGrid[startRow][startCol];
        
        if (startIndex < 0 || startIndex >= targetBubbles.size()) {
            return;
        }
        
        Bubble targetBubble = targetBubbles.get(startIndex);
        if (targetBubble == null) {
            return;
        }
        
        List<Point> matched = new ArrayList<>();
        Set<Point> visited = new HashSet<>();
        
        // DFS 查找相同颜色的泡泡
        dfsMatch(startRow, startCol, targetBubble.getColor(), matched, visited, targetGrid);
        
        if (matched.size() >= 3) {
            // 播放消除音效
            SoundManager.playPopSound();
            
            // 立即从网格和列表中移除泡泡
            for (Point p : matched) {
                int index = targetGrid[p.y][p.x];
                if (index != -1 && targetBubbles.get(index) != null) {
                    animatingBubbles.add(targetBubbles.get(index));
                    targetGrid[p.y][p.x] = -1;  // remove from grid immediately
                    targetBubbles.set(index, null);  // set to null in list, allow new bubble to occupy
                }
            }
            
            // 启动动画计时器
            Timer animationTimer = new Timer(50, e -> {
                staticAnimStep++;
                if (staticAnimStep >= 10) {  // animation lasts 10 frames (~0.5 sec)
                    ((Timer)e.getSource()).stop();
                    staticAnimStep = 0;
                    animatingBubbles.clear();
                }
                repaint();
            });
            animationTimer.start();
            
            // 计算得分并分配给发射的玩家
            int matchScore = matched.size() * 10;
            if (matched.size() > 3) {
                matchScore += (matched.size() - 3) * 20; // bonus
            }
            
            if (shooter == player) {
                p1Score += matchScore;
            } else {
                p2Score += matchScore;
            }
            
            // 消除后，检查是否有泡泡变成悬浮状态
            checkFloatingBubbles(targetGrid, targetBubbles);
        }
    }
    // 用于动画计数的静态变量
    private static int staticAnimStep = 0;
    
    // DFS 查找匹配的泡泡
    private void dfsMatch(int row, int col, Bubble.BubbleColor color, 
                         List<Point> matched, Set<Point> visited, int[][] targetGrid) {
        // 首先检查边界
        int colsInGrid = targetGrid[0].length;  // 获取当前网格的列数
        if (row < 0 || row >= GRID_ROWS || col < 0 || col >= colsInGrid) return;
        if (visited.contains(new Point(col, row))) return;
        if (targetGrid[row][col] == -1) return;
        
        // 根据网格选择正确的泡泡列表
        List<Bubble> targetBubbles;
        if (targetGrid == p2Grid) {
            targetBubbles = p2Bubbles;
        } else if (targetGrid == p1Grid) {
            targetBubbles = p1Bubbles;
        } else {
            targetBubbles = singleBubbles;
        }
        
        int index = targetGrid[row][col];
        if (index < 0 || index >= targetBubbles.size()) return;
        
        Bubble bubble = targetBubbles.get(index);
        if (bubble == null || bubble.getColor() != color) return;
        
        visited.add(new Point(col, row));
        matched.add(new Point(col, row));
        
        // 定义六个方向的邻居（六边形网格）
        int[][] neighbors;
        if (row % 2 == 1) {
            neighbors = new int[][]{
                {-1, 0}, {-1, 1},  // 左上，右上
                {0, -1}, {0, 1},   // 左，右
                {1, 0}, {1, 1}     // 左下，右下
            };
        } else {
            neighbors = new int[][]{
                {-1, -1}, {-1, 0},  // 左上，右上
                {0, -1}, {0, 1},    // 左，右
                {1, -1}, {1, 0}     // 左下，右下
            };
        }
        
        // 递归检查所有邻居
        for (int[] neighbor : neighbors) {
            int newRow = row + neighbor[0];
            int newCol = col + neighbor[1];
            dfsMatch(newRow, newCol, color, matched, visited, targetGrid);
        }
    }
    
    // 检查悬浮的泡泡（没有连接到顶部的泡泡）
    private void checkFloatingBubbles(int[][] targetGrid, List<Bubble> targetBubbles) {
        Set<Point> connected = new HashSet<>();
        int colsInGrid = targetGrid[0].length;
        
        // 从顶部开始 BFS，标记所有连接到顶部的泡泡
        Queue<Point> queue = new LinkedList<>();
        for (int col = 0; col < colsInGrid; col++) {
            if (targetGrid[0][col] != -1) {
                queue.offer(new Point(col, 0));
                connected.add(new Point(col, 0));
            }
        }
        
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            
            // 获取邻居
            List<Point> neighbors = getNeighbors(current.y, current.x, targetGrid);
            for (Point neighbor : neighbors) {
                if (!connected.contains(neighbor)) {
                    connected.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
        
        // 移除未连接的泡泡
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < colsInGrid; col++) {
                if (targetGrid[row][col] != -1 && !connected.contains(new Point(col, row))) {
                    int index = targetGrid[row][col];
                    targetBubbles.set(index, null);
                    targetGrid[row][col] = -1;
                    
                    if (targetGrid == p2Grid) {
                        p2Score += 50; // 掉落奖励
                    } else {
                        p1Score += 50;
                    }
                }
            }
        }
    }
    
    // 获取邻居位置
    private List<Point> getNeighbors(int row, int col, int[][] targetGrid) {
        List<Point> neighbors = new ArrayList<>();
        int colsInGrid = targetGrid[0].length;
        
        // 定义六个方向的邻居（六边形网格）
        int[][] neighborOffsets;
        if (row % 2 == 1) {
            // 奇数行
            neighborOffsets = new int[][]{
                {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, 0}, {1, 1}
            };
        } else {
            // 偶数行
            neighborOffsets = new int[][]{
                {-1, -1}, {-1, 0},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}
            };
        }
        
        for (int[] offset : neighborOffsets) {
            int newRow = row + offset[0];
            int newCol = col + offset[1];
            
            if (newRow >= 0 && newRow < GRID_ROWS && 
                newCol >= 0 && newCol < colsInGrid &&
                targetGrid[newRow][newCol] != -1) {
                neighbors.add(new Point(newCol, newRow));
            }
        }
        
        return neighbors;
    }
    
    // 检查游戏结束
    private void checkGameOver() {
        int dangerLine = GRID_ROWS - 1;  // 最后一行（向下移动 3 格）
        
        if (twoPlayerMode) {
            boolean p1Lost = false;
            boolean p2Lost = false;
            
            // 检查玩家 1
            for (int col = 0; col < GRID_COLS / 2; col++) {
                if (p1Grid[dangerLine][col] != -1) {
                    p1Lost = true;
                    break;
                }
            }
            
            // 检查玩家 2
            for (int col = 0; col < GRID_COLS / 2; col++) {
                if (p2Grid[dangerLine][col] != -1) {
                    p2Lost = true;
                    break;
                }
            }
            
            if (p1Lost && !p2Lost) {
                gameOver = true;
                message = "玩家 2 获胜！";
            } else if (p2Lost && !p1Lost) {
                gameOver = true;
                message = "玩家 1 获胜！";
            } else if (p1Lost && p2Lost) {
                gameOver = true;
                message = "平局！";
            }
            
            if (gameOver) {
                message += String.format(" P1:%d P2:%d", p1Score, p2Score);
            }
        } else {
            // 单人模式
            for (int col = 0; col < GRID_COLS; col++) {
                if (singleGrid[dangerLine][col] != -1) {
                    gameOver = true;
                    message = "游戏结束！得分：" + score;
                    break;
                }
            }
        }
        
        // 检查胜利条件
        if (!gameOver && twoPlayerMode) {
            // 双人模式：检查双方剩余的泡泡数量
            int p1BubbleCount = 0;
            int p2BubbleCount = 0;
            
            // 统计玩家 1 的泡泡
            for (int[] row : p1Grid) {
                for (int cell : row) {
                    if (cell != -1) {
                        p1BubbleCount++;
                    }
                }
            }
            
            // 统计玩家 2 的泡泡
            for (int[] row : p2Grid) {
                for (int cell : row) {
                    if (cell != -1) {
                        p2BubbleCount++;
                    }
                }
            }
            
            // 如果一方泡泡全部消除，游戏结束
            if (p1BubbleCount == 0 && p2BubbleCount > 0) {
                gameOver = true;
                message = "玩家 1 获胜！";
                message += String.format(" P1:%d P2:%d", p1Score, p2Score);
            } else if (p2BubbleCount == 0 && p1BubbleCount > 0) {
                gameOver = true;
                message = "玩家 2 获胜！";
                message += String.format(" P1:%d P2:%d", p1Score, p2Score);
            } else if (p1BubbleCount == 0 && p2BubbleCount == 0) {
                // 同时消除完（极罕见情况）
                gameOver = true;
                message = "平局！";
                message += String.format(" P1:%d P2:%d", p1Score, p2Score);
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        
        // 绘制危险线（游戏结束线）- 在最后一行位置
        int dangerY = 50 + (GRID_ROWS - 1) * (Bubble.DIAMETER - 5);  // 第 15 行
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(255, 100, 100, 100));
        g2d.setStroke(new java.awt.BasicStroke(2, java.awt.BasicStroke.CAP_BUTT, 
                                              java.awt.BasicStroke.JOIN_BEVEL, 0, 
                                              new float[]{5, 5}, 0));
        g2d.drawLine(0, dangerY, WIDTH, dangerY);
        
        // 绘制危险线文字提示
        g2d.setColor(new Color(255, 100, 100, 150));
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("DANGER LINE", WIDTH - 90, dangerY - 5);
        
        // 双人模式下绘制中间隔板
        if (twoPlayerMode) {
            // 绘制粗的中间分隔线
            g2d.setColor(new Color(80, 80, 80, 200));
            g2d.setStroke(new java.awt.BasicStroke(6));
            g2d.drawLine(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
            
            // 绘制分隔区域的背景
            g2d.setColor(new Color(50, 50, 60, 100));
            g2d.fillRect(WIDTH / 2 - 3, 0, 6, HEIGHT);
            
            // 绘制 P1 和 P2 标识
            g2d.setColor(new Color(100, 200, 255, 180));
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("PLAYER 1", WIDTH / 4 - 35, HEIGHT - 20);
            
            g2d.setColor(new Color(255, 200, 100, 180));
            g2d.drawString("PLAYER 2", WIDTH * 3 / 4 - 35, HEIGHT - 20);
        }
        
        // 绘制所有泡泡
        if (twoPlayerMode) {
            // 双人模式：绘制两个玩家的泡泡
            for (Bubble bubble : p1Bubbles) {
                if (bubble != null) {
                    bubble.render(g);
                }
            }
            for (Bubble bubble : p2Bubbles) {
                if (bubble != null) {
                    bubble.render(g);
                }
            }
        } else {
            // 单人模式
            for (Bubble bubble : singleBubbles) {
                if (bubble != null) {
                    bubble.render(g);
                }
            }
        }
        
        // 绘制正在消除的泡泡（带动画效果）
        for (Bubble bubble : animatingBubbles) {
            if (bubble != null) {
                // 计算缩放比例和透明度（0-1）
                double scale = 1.0 + staticAnimStep * 0.2;  // 逐渐放大
                float alpha = 1.0f - (staticAnimStep / 12.0f);  // 逐渐透明（12 帧）
                
                // 创建临时的 Graphics2D 对象用于动画
                Graphics2D animG = (Graphics2D) g.create();
                animG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                animG.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, Math.max(0, alpha)));
                
                // 应用缩放变换
                int size = (int)(Bubble.DIAMETER * scale);
                int x = bubble.getX() - size / 2;
                int y = bubble.getY() - size / 2;
                
                // 绘制渐变的半透明泡泡（无黑边）
                Color baseColor = bubble.getColor().getColor();
                RadialGradientPaint gradient = new RadialGradientPaint(
                    new java.awt.geom.Point2D.Double(x + size/4, y + size/4),
                    size/2,
                    new float[] {0.0f, 0.5f, 1.0f},
                    new Color[] {
                        baseColor.brighter().brighter(),
                        baseColor,
                        baseColor.darker()
                    }
                );
                animG.setPaint(gradient);
                animG.fillOval(x, y, size, size);
                
                // 添加白色高光
                animG.setColor(new Color(255, 255, 255, (int)(200 * alpha)));
                animG.fillOval(x + size/4, y + size/4, (int)(size * 0.25), (int)(size * 0.25));
                
                animG.dispose();
            }
        }
        
        // 绘制玩家
        player.render(g);
        if (twoPlayerMode && player2 != null) {
            player2.render(g);
            
            // 显示玩家标识
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("P1", player.getX() - 15, player.getY() - 40);
            g.drawString("P2", player2.getX() - 15, player2.getY() - 40);
        }
        
        // 绘制分数
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        if (twoPlayerMode) {
            g.drawString("P1: " + p1Score, 10, 25);
            g.drawString("P2: " + p2Score, WIDTH - 100, 25);
        } else {
            g.drawString("分数：" + score, 10, 25);
        }
        
        // 绘制游戏结束消息
        if (gameOver) {
            // 半透明黑色背景
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            
            // 游戏结束标题
            g.setColor(new Color(255, 215, 0)); // 金色
            g.setFont(new Font("Arial Black", Font.BOLD, 48));
            String gameOverTitle = "游戏结束";
            g.drawString(gameOverTitle, WIDTH / 2 - g.getFontMetrics().stringWidth(gameOverTitle) / 2, HEIGHT / 2 - 80);
            
            // 获胜者信息（更大更醒目）
            g.setColor(new Color(100, 255, 100)); // 亮绿色
            g.setFont(new Font("Arial Black", Font.BOLD, 36));
            int winnerY = HEIGHT / 2 - 20;
            g.drawString(message, WIDTH / 2 - g.getFontMetrics().stringWidth(message) / 2, winnerY);
            
            // 显示双方得分（如果是双人模式）
            if (twoPlayerMode) {
                g.setColor(new Color(100, 200, 255)); // 蓝色
                g.setFont(new Font("Arial", Font.BOLD, 24));
                String p1ScoreText = "玩家 1 得分：" + p1Score;
                String p2ScoreText = "玩家 2 得分：" + p2Score;
                g.drawString(p1ScoreText, WIDTH / 2 - g.getFontMetrics().stringWidth(p1ScoreText) / 2, HEIGHT / 2 + 40);
                g.drawString(p2ScoreText, WIDTH / 2 - g.getFontMetrics().stringWidth(p2ScoreText) / 2, HEIGHT / 2 + 75);
            } else {
                // 单人模式显示最终得分
                g.setColor(new Color(255, 200, 100)); // 橙色
                g.setFont(new Font("Arial", Font.BOLD, 24));
                String finalScoreText = "最终得分：" + score;
                g.drawString(finalScoreText, WIDTH / 2 - g.getFontMetrics().stringWidth(finalScoreText) / 2, HEIGHT / 2 + 40);
            }
            
            // 重新开始提示（带闪烁效果）
            int blink = (int)(System.currentTimeMillis() / 500 % 2);
            if (blink == 1) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                String restartMsg = "按 R 键重新开始";
                g.drawString(restartMsg, WIDTH / 2 - g.getFontMetrics().stringWidth(restartMsg) / 2, HEIGHT / 2 + 130);
            }
            
            // 装饰性边框
            Graphics2D borderG = (Graphics2D) g.create();
            borderG.setColor(new Color(255, 215, 0, 100));
            borderG.setStroke(new java.awt.BasicStroke(3));
            borderG.drawRect(50, HEIGHT / 2 - 120, WIDTH - 100, 240);
            borderG.dispose();
        }
    }
    
    public void startGame() {
        timer.start();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }
    
    // 键盘事件处理
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // 打开按键设置对话框 (F1 键)
        if (keyCode == KeyEvent.VK_F1 || keyCode == KeyEvent.VK_P) {
            openKeySettings();
            return;
        }
        
        // 双人模式：两个玩家独立控制
        // 玩家 1: A/D 键控制方向，W 键发射
        if (keyCode == java.awt.event.KeyEvent.VK_A) {
            player.adjustAngleByKeyboard(-1);
        }
        if (keyCode == java.awt.event.KeyEvent.VK_D) {
            player.adjustAngleByKeyboard(1);
        }
        if (keyCode == java.awt.event.KeyEvent.VK_W) {
            if (!gameOver) {
                player.shoot();
            }
        }
        
        // 玩家 2: 使用配置的按键
        if (keyCode == KeyBindingsConfig.getP2LeftKey()) {
            player2.adjustAngleByKeyboard(-1);
        }
        if (keyCode == KeyBindingsConfig.getP2RightKey()) {
            player2.adjustAngleByKeyboard(1);
        }
        if (keyCode == KeyBindingsConfig.getP2ShootKey()) {
            if (!gameOver) {
                player2.shoot();
            }
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    // 键盘控制 - 添加重置功能
    public void resetGame() {
        initGame();
    }
    
    /**
     * 获取是否为双人模式
     */
    public boolean isTwoPlayerMode() {
        return twoPlayerMode;
    }
    
    /**
     * 打开按键设置对话框
     */
    public void openKeySettings() {
        // 暂停游戏
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        
        // 获取顶层窗口
        Window window = SwingUtilities.getWindowAncestor(this);
        JFrame parentFrame = (window instanceof JFrame) ? (JFrame) window : null;
        
        if (parentFrame == null) {
            // 如果没有找到父窗口，创建一个新的
            parentFrame = new JFrame("泡泡龙游戏");
        }
        
        // 创建并显示按键设置对话框
        KeySettingsDialog dialog = new KeySettingsDialog(parentFrame);
        dialog.setVisible(true);
        
        // 恢复游戏
        if (timer != null && !timer.isRunning()) {
            timer.start();
        }
        
        requestFocusInWindow();
    }
}
