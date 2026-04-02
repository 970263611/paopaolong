package com.ppl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * 泡泡龙游戏主类
 */
public class BubbleDragonGame {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 直接进入双人模式
            boolean twoPlayerMode = true;
            
            JFrame frame = new JFrame("泡泡龙游戏 - 双人模式");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            GamePanel gamePanel = new GamePanel(twoPlayerMode);
            frame.add(gamePanel);
            frame.pack();
            
            // 设置窗口居中
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            // 创建菜单栏
            createMenuBar(frame, gamePanel);
            
            // 启动游戏
            gamePanel.startGame();
        });
    }
    
    /**
     * 创建游戏菜单栏
     */
    private static void createMenuBar(JFrame frame, GamePanel gamePanel) {
        JMenuBar menuBar = new JMenuBar();
        
        // 游戏菜单
        JMenu gameMenu = new JMenu("游戏");
        gameMenu.setMnemonic(KeyEvent.VK_G);
        
        // 重新开始
        JMenuItem restartItem = new JMenuItem("重新开始");
        restartItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
        restartItem.addActionListener(e -> gamePanel.resetGame());
        gameMenu.add(restartItem);
        
        gameMenu.addSeparator();
        
        // 退出
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
        exitItem.addActionListener(e -> System.exit(0));
        gameMenu.add(exitItem);
        
        menuBar.add(gameMenu);
        
        // 设置菜单
        JMenu settingsMenu = new JMenu("设置");
        settingsMenu.setMnemonic(KeyEvent.VK_S);
        
        // 按键设置
        JMenuItem keySettingsItem = new JMenuItem("按键设置...");
        keySettingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        keySettingsItem.addActionListener(e -> gamePanel.openKeySettings());
        settingsMenu.add(keySettingsItem);
        
        // 显示当前按键配置
        JMenuItem showCurrentKeysItem = new JMenuItem("查看当前按键");
        showCurrentKeysItem.addActionListener(e -> showCurrentKeyBindings(frame));
        settingsMenu.add(showCurrentKeysItem);
        
        menuBar.add(settingsMenu);
        
        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        // 操作说明
        JMenuItem helpItem = new JMenuItem("操作说明");
        helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        helpItem.addActionListener(e -> showHelpDialog(frame, gamePanel.isTwoPlayerMode()));
        helpMenu.add(helpItem);
        
        // 关于
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> showAboutDialog(frame));
        helpMenu.add(aboutItem);
        
        menuBar.add(helpMenu);
        
        frame.setJMenuBar(menuBar);
    }
    
    /**
     * 显示当前按键绑定
     */
    private static void showCurrentKeyBindings(JFrame frame) {
        String message = String.format(
            "当前按键配置：\n\n" +
            "玩家 1 - 向左旋转：%s\n" +
            "玩家 1 - 向右旋转：%s\n" +
            "玩家 1 - 发射泡泡：%s\n\n" +
            "玩家 2 - 向左旋转：%s\n" +
            "玩家 2 - 向右旋转：%s\n" +
            "玩家 2 - 发射泡泡：%s\n\n" +
            "提示：按 F1 或 P 键可以修改这些按键",
            getKeyText(KeyBindingsConfig.getSingleLeftKey()),
            getKeyText(KeyBindingsConfig.getSingleRightKey()),
            getKeyText(KeyBindingsConfig.getSingleShootKey()),
            getKeyText(KeyBindingsConfig.getP2LeftKey()),
            getKeyText(KeyBindingsConfig.getP2RightKey()),
            getKeyText(KeyBindingsConfig.getP2ShootKey())
        );
        
        JOptionPane.showMessageDialog(frame, message, "当前按键配置", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 显示操作说明对话框
     */
    private static void showHelpDialog(JFrame frame, boolean twoPlayerMode) {
        StringBuilder helpText = new StringBuilder();
        helpText.append("<html><div style='padding: 10px;'>");
        helpText.append("<h2>泡泡龙游戏 - 操作说明</h2>");
        
        if (twoPlayerMode) {
            helpText.append("<h3>双人模式</h3>");
            helpText.append("<b>玩家 1（左侧）：</b><br>");
            helpText.append("&nbsp;&nbsp;A 键 - 向左旋转<br>");
            helpText.append("&nbsp;&nbsp;D 键 - 向右旋转<br>");
            helpText.append("&nbsp;&nbsp;W 键 - 发射泡泡<br><br>");
            
            helpText.append("<b>玩家 2（右侧）：</b><br>");
            helpText.append("&nbsp;&nbsp;").append(getKeyText(KeyBindingsConfig.getLeftKey())).append(" - 向左旋转<br>");
            helpText.append("&nbsp;&nbsp;").append(getKeyText(KeyBindingsConfig.getRightKey())).append(" - 向右旋转<br>");
            helpText.append("&nbsp;&nbsp;").append(getKeyText(KeyBindingsConfig.getShootKey())).append(" - 发射泡泡<br><br>");
        }
        
        helpText.append("<b>通用操作：</b><br>");
        helpText.append("&nbsp;&nbsp;F1 或 P - 打开按键设置<br><br>");
        
        helpText.append("<b>游戏规则：</b><br>");
        helpText.append("&nbsp;&nbsp;• 将 3 个或更多相同颜色的泡泡连在一起即可消除<br>");
        helpText.append("&nbsp;&nbsp;• 泡泡触底则游戏结束<br>");
        helpText.append("&nbsp;&nbsp;• 双人模式下，先将对方区域填满者获胜<br>");
        
        helpText.append("</div></html>");
        
        JOptionPane.showMessageDialog(frame, helpText.toString(), "操作说明", JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     * 显示关于对话框
     */
    private static void showAboutDialog(JFrame frame) {
        String message = "<html><div style='padding: 10px;'>" +
                "<h2>泡泡龙游戏</h2>" +
                "<p>版本：1.0</p>" +
                "<p>经典泡泡龙游戏，支持双人对战模式</p>" +
                "<p>技术栈：Java Swing</p>" +
                "</div></html>";
        
        JOptionPane.showMessageDialog(frame, message, "关于", JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     * 将键码转换为可读文本
     */
    private static String getKeyText(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT: return "← 左箭头";
            case KeyEvent.VK_RIGHT: return "→ 右箭头";
            case KeyEvent.VK_UP: return "↑ 上箭头";
            case KeyEvent.VK_DOWN: return "↓ 下箭头";
            case KeyEvent.VK_SPACE: return "空格键";
            case KeyEvent.VK_ENTER: return "回车键";
            case KeyEvent.VK_W: return "W";
            case KeyEvent.VK_A: return "A";
            case KeyEvent.VK_S: return "S";
            case KeyEvent.VK_D: return "D";
            case KeyEvent.VK_R: return "R";
            default: return KeyEvent.getKeyText(keyCode);
        }
    }
}
