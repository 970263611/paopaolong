package com.ppl;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * 玩家发射器类 - 控制泡泡的发射
 */
public class Player {
    private int x, y;
    private double angle; // 发射角度（弧度）
    private double shootAngle; // 发射时的角度（固定）
    private Bubble currentBubble; // 当前待发射的泡泡
    private Bubble nextBubble; // 下一个泡泡
    private boolean isShooting = false;
    private Bubble shootingBubble; // 正在飞行的泡泡
    private double shootSpeed = 15; // 发射速度
    private Color pointerColor = new Color(255, 255, 255, 100); // 指针颜色，默认白色
    private int leftBound = 0;  // 左边界
    private int rightBound = GamePanel.WIDTH;  // 右边界
    
    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.angle = -Math.PI / 2; // 初始向上（负 Y 轴方向）
        this.currentBubble = createRandomBubble(x, y);
        this.nextBubble = createRandomBubble(0, 0);
    }
    
    public Player(int x, int y, Color pointerColor) {
        this.x = x;
        this.y = y;
        this.pointerColor = pointerColor;
        this.angle = -Math.PI / 2; // 初始向上（负 Y 轴方向）
        this.currentBubble = createRandomBubble(x, y);
        this.nextBubble = createRandomBubble(0, 0);
    }
    
    public Player(int x, int y, Color pointerColor, int leftBound, int rightBound) {
        this.x = x;
        this.y = y;
        this.pointerColor = pointerColor;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.angle = -Math.PI / 2; // 初始向上（负 Y 轴方向）
        this.currentBubble = createRandomBubble(x, y);
        this.nextBubble = createRandomBubble(0, 0);
    }
    
    public Player(int x, int y, Color pointerColor, int leftBound, int rightBound, 
                  Bubble.BubbleColor currentColor, Bubble.BubbleColor nextColor) {
        this.x = x;
        this.y = y;
        this.pointerColor = pointerColor;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        // 根据玩家位置设置初始角度
        // 如果玩家在右侧（x > WIDTH/2），初始角度稍微向左；否则向上
        if (x > GamePanel.WIDTH / 2) {
            this.angle = -Math.PI / 2 - Math.PI / 12; // 右侧玩家，向左倾斜 15 度
        } else {
            this.angle = -Math.PI / 2; // 左侧玩家，向上
        }
        // 使用指定的颜色创建泡泡
        this.currentBubble = new Bubble(x, y, currentColor);
        this.nextBubble = new Bubble(0, 0, nextColor);
    }
    
    private Bubble createRandomBubble(int x, int y) {
        Bubble.BubbleColor[] colors = Bubble.BubbleColor.values();
        Bubble.BubbleColor randomColor = colors[(int) (Math.random() * colors.length)];
        return new Bubble(x, y, randomColor);
    }
    
    /**
     * 通过键盘调整角度
     * @param direction -1 表示向左，1 表示向右
     */
    public void adjustAngleByKeyboard(int direction) {
        double rotationSpeed = 0.12; // 每次旋转的角度（弧度），进一步加快速度
        angle += direction * rotationSpeed;
        // 限制角度范围在 -π 到 0 之间（上半部分，不能向下）
        if (angle < -Math.PI) angle = -Math.PI;
        if (angle > 0) angle = 0;
    }
    
    public void shoot() {
        if (!isShooting && currentBubble != null) {
            isShooting = true;
            shootingBubble = currentBubble;
            shootingBubble.setX(x);
            shootingBubble.setY(y);
            shootAngle = angle; // 保存发射角度
            
            // 播放发射音效
            SoundManager.playShootSound();
            
            // 关键修改：双人模式下，currentBubble 移动后，nextBubble 会自动成为新的 currentBubble
            // 但 nextBubble 本身保持不变（不创建新球），由 GamePanel 来更新它
            currentBubble = nextBubble;
            // nextBubble 保持原样，等待 GamePanel 更新
        }
    }
    
    public boolean updateShooting() {
        if (isShooting && shootingBubble != null) {
            // 使用固定的发射角度计算移动
            int dx = (int) (Math.cos(shootAngle) * shootSpeed);
            int dy = (int) (Math.sin(shootAngle) * shootSpeed);
            
            int newX = shootingBubble.getX() + dx;
            int newY = shootingBubble.getY() + dy;
            
            // 检查是否碰到左右墙壁 - 反弹
            if (newX - Bubble.RADIUS <= leftBound) {
                // 碰到左墙，反弹
                shootingBubble.setX(leftBound + Bubble.RADIUS);
                shootAngle = Math.PI - shootAngle;  // 镜像反射（使用 shootAngle）
                return false;  // 继续飞行
            } else if (newX + Bubble.RADIUS >= rightBound) {
                // 碰到右墙，反弹
                shootingBubble.setX(rightBound - Bubble.RADIUS);
                shootAngle = Math.PI - shootAngle;  // 镜像反射（使用 shootAngle）
                return false;  // 继续飞行
            }
            
            // 检查是否碰到顶部边界
            if (newY - Bubble.RADIUS <= 0) {
                isShooting = false;
                return true; // 需要处理碰撞
            }
            
            // 更新位置
            shootingBubble.setX(newX);
            shootingBubble.setY(newY);
            
            return false;
        }
        return false;
    }
    
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // 保存原始变换
        AffineTransform originalTransform = g2d.getTransform();
        
        // 1. 绘制发射器底座（美化版）
        // 底座阴影
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(x - 30, y + 15, 60, 35, 10, 10);
        
        // 底座主体（带渐变效果）
        GradientPaint baseGradient = new GradientPaint(
            x - 25, y + 10,
            pointerColor.brighter(),
            x + 25, y + 40,
            pointerColor.darker()
        );
        g2d.setPaint(baseGradient);
        g2d.fillRoundRect(x - 25, y + 10, 50, 30, 8, 8);
        
        // 底座边框
        g2d.setColor(pointerColor.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x - 25, y + 10, 50, 30, 8, 8);
        
        // 2. 绘制旋转的指针（箭头样式 - 美化版）
        int arrowLength = 65;
        int arrowWidth = 18;
        
        // 计算箭头尖端方向（沿着 angle 方向）
        double tipX = x + Math.cos(angle) * arrowLength;
        double tipY = y + Math.sin(angle) * arrowLength;
        
        // 计算箭尾中心点（在相反方向）
        double tailCenterX = x - Math.cos(angle) * 10;
        double tailCenterY = y - Math.sin(angle) * 10;
        
        // 计算垂直于发射方向的两个点
        double perpAngle1 = angle + Math.PI / 2;
        double perpAngle2 = angle - Math.PI / 2;
        double tailX1 = tailCenterX + Math.cos(perpAngle1) * arrowWidth;
        double tailY1 = tailCenterY + Math.sin(perpAngle1) * arrowWidth;
        double tailX2 = tailCenterX + Math.cos(perpAngle2) * arrowWidth;
        double tailY2 = tailCenterY + Math.sin(perpAngle2) * arrowWidth;
        
        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制箭头阴影（柔和的阴影）
        g2d.setColor(new Color(0, 0, 0, 80));
        int[] shadowXPoints = {(int)(tipX+2), (int)(tailX1+2), (int)(tailX2+2)};
        int[] shadowYPoints = {(int)(tipY+2), (int)(tailY1+2), (int)(tailY2+2)};
        g2d.fillPolygon(shadowXPoints, shadowYPoints, 3);
        
        // 绘制箭头主体渐变效果
        GradientPaint arrowGradient = new GradientPaint(
            (float)x, (float)y, pointerColor.brighter(),
            (float)tipX, (float)tipY, pointerColor.darker()
        );
        g2d.setPaint(arrowGradient);
        int[] xPoints = {(int)tipX, (int)tailX1, (int)tailX2};
        int[] yPoints = {(int)tipY, (int)tailY1, (int)tailY2};
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // 箭头边框（银色，更精致）
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawPolygon(xPoints, yPoints, 3);
        
        // 在箭头中心添加一个白色圆点，指示焦点
        g2d.setColor(Color.WHITE);
        g2d.fillOval((int)(x + Math.cos(angle) * 25) - 3, 
                    (int)(y + Math.sin(angle) * 25) - 3, 6, 6);
        
        // 3. 绘制中心圆点（装饰）
        g2d.setColor(pointerColor.brighter());
        g2d.fillOval(x - 8, y - 8, 16, 16);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x - 8, y - 8, 16, 16);
        
        // 恢复变换
        g2d.setTransform(originalTransform);
        g2d.setStroke(new BasicStroke(1));
        
        // 4. 绘制当前泡泡
        if (currentBubble != null) {
            currentBubble.setX(x);
            currentBubble.setY(y);
            currentBubble.render(g);
        }
        
        // 5. 绘制下一个泡泡预览（美化版）
        if (nextBubble != null) {
            // 绘制背景框（带圆角）
            g2d.setColor(new Color(50, 50, 80, 200));
            g2d.fillRoundRect(x + 50, y - 35, 70, 60, 15, 15);
            g2d.setColor(new Color(150, 150, 200));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x + 50, y - 35, 70, 60, 15, 15);
            
            // 绘制 NEXT 文字
            g2d.setColor(new Color(255, 255, 100));
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("NEXT", x + 62, y - 15);
            
            // 绘制 Next 泡泡（带阴影效果）
            nextBubble.setX(x + 85);
            nextBubble.setY(y + 5);
            
            // 绘制阴影
            g.setColor(new Color(0, 0, 0, 100));
            g.fillOval(x + 88, y + 8, Bubble.DIAMETER, Bubble.DIAMETER);
            
            // 绘制泡泡本体
            nextBubble.render(g);
        }
        
        // 6. 绘制发射中的泡泡
        if (shootingBubble != null) {
            shootingBubble.render(g);
        }
    }
    
    // Getters and Setters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getLeftBound() { return leftBound; }
    public int getRightBound() { return rightBound; }
    public Bubble getCurrentBubble() { return currentBubble; }
    public Bubble getNextBubble() { return nextBubble; }
    public Bubble getShootingBubble() { return shootingBubble; }
    public boolean isShooting() { return isShooting; }
    public void setShooting(boolean shooting) { isShooting = shooting; }
    
    // 设置下一个泡泡的颜色（用于双人模式同步）
    public void setNextBubbleColor(Bubble.BubbleColor color) {
        if (nextBubble != null) {
            this.nextBubble = new Bubble(0, 0, color);
        }
    }
    
    public Bubble getAndClearShootingBubble() {
        Bubble bubble = shootingBubble;
        shootingBubble = null;
        return bubble;
    }
}
