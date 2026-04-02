package com.ppl;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;

/**
 * 泡泡类
 */
public class Bubble {
    public static final int RADIUS = 20;
    public static final int DIAMETER = RADIUS * 2;
    
    // 泡泡颜色枚举
    public enum BubbleColor {
        RED(new Color(255, 100, 100)),
        BLUE(new Color(100, 100, 255)),
        GREEN(new Color(100, 255, 100)),
        YELLOW(new Color(255, 255, 100)),
        PURPLE(new Color(200, 100, 255));
        
        private final Color color;
        
        BubbleColor(Color color) {
            this.color = color;
        }
        
        public Color getColor() {
            return color;
        }
    }
    
    private int x, y;
    private BubbleColor color;
    private boolean marked = false; // 用于标记需要消除的泡泡
    
    public Bubble(int x, int y, BubbleColor color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }
    
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 1. 绘制泡泡本体（渐变效果）
        Color baseColor = color.getColor();
        RadialGradientPaint gradient = new RadialGradientPaint(
            new Point2D.Double(x - RADIUS/3, y - RADIUS/3),
            RADIUS,
            new float[] {0.0f, 0.3f, 1.0f},
            new Color[] {
                baseColor.brighter().brighter(),  // 中心最亮
                baseColor,                         // 中间本色
                baseColor.darker()                 // 边缘稍暗
            }
        );
        g2d.setPaint(gradient);
        g2d.fillOval(x - RADIUS, y - RADIUS, DIAMETER, DIAMETER);
        
        // 2. 绘制白色高光（更自然的高光效果）
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.fillOval(x - RADIUS + 6, y - RADIUS + 6, 10, 10);
        
        // 3. 添加第二层高光（增强立体感）
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.fillOval(x - RADIUS + 10, y - RADIUS + 10, 5, 5);
    }
    
    public boolean containsPoint(int px, int py) {
        int dx = x - px;
        int dy = y - py;
        return dx * dx + dy * dy <= RADIUS * RADIUS;
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x - RADIUS, y - RADIUS, DIAMETER, DIAMETER);
    }
    
    // Getters and Setters
    public int getX() { return x; }
    public int getY() { return y; }
    public BubbleColor getColor() { return color; }
    public boolean isMarked() { return marked; }
    public void setMarked(boolean marked) { this.marked = marked; }
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}
