package com.ppl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;

/**
 * 按键设置对话框 - 允许玩家在游戏中自定义按键绑定
 * 支持单人和双人模式的按键设置
 */
public class KeySettingsDialog extends JDialog {
    
    // 玩家 1 按键按钮
    private JButton p1LeftButton;
    private JButton p1RightButton;
    private JButton p1ShootButton;
    
    // 玩家 2 按键按钮
    private JButton p2LeftButton;
    private JButton p2RightButton;
    private JButton p2ShootButton;
    
    private JButton saveButton;
    private JButton cancelButton;
    private JButton defaultButton;
    
    // 当前正在设置的按键
    private JButton currentSettingButton = null;
    
    // 临时存储新的按键配置
    private int newP1LeftKey = KeyBindingsConfig.getSingleLeftKey();
    private int newP1RightKey = KeyBindingsConfig.getSingleRightKey();
    private int newP1ShootKey = KeyBindingsConfig.getSingleShootKey();
    
    private int newP2LeftKey = KeyBindingsConfig.getP2LeftKey();
    private int newP2RightKey = KeyBindingsConfig.getP2RightKey();
    private int newP2ShootKey = KeyBindingsConfig.getP2ShootKey();
    
    // 键盘事件分发器 - 用于全局捕获按键
    private KeyEventDispatcher dispatcher;
    
    public KeySettingsDialog(JFrame parent) {
        super(parent, "按键设置", true);
        initialize();
    }
    
    private void initialize() {
        setSize(500, 480);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 标题
        JLabel titleLabel = new JLabel("双人模式按键设置", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 内容面板（双方并排）
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // 玩家 1 面板
        JPanel p1Panel = createPlayer1Panel();
        p1Panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 200, 255), 2),
            "玩家 1 (左侧)",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            new Color(100, 200, 255)
        ));
        
        // 玩家 2 面板
        JPanel p2Panel = createPlayer2Panel();
        p2Panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 200, 100), 2),
            "玩家 2 (右侧)",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            new Color(255, 200, 100)
        ));
        
        contentPanel.add(p1Panel);
        contentPanel.add(p2Panel);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 底部说明
        JLabel hintLabel = new JLabel("提示：点击按钮后按下想要设置的按键（Esc 取消）", SwingConstants.CENTER);
        hintLabel.setForeground(Color.GRAY);
        mainPanel.add(hintLabel, BorderLayout.SOUTH);
        
        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        defaultButton = new JButton("恢复默认");
        defaultButton.setFont(new Font("Arial", Font.BOLD, 14));
        defaultButton.setPreferredSize(new Dimension(130, 40));
        defaultButton.addActionListener(e -> restoreDefaults());
        buttonPanel.add(defaultButton);
        
        saveButton = new JButton("保 存 设 置");
        saveButton.setFont(new Font("Arial", Font.BOLD, 16));
        saveButton.setPreferredSize(new Dimension(150, 45));
        saveButton.setBackground(new Color(76, 175, 80));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        saveButton.addActionListener(e -> saveSettings());
        buttonPanel.add(saveButton);
        
        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        
        add(mainPanel);
        
        // 创建全局键盘事件监听器
        dispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && currentSettingButton != null) {
                    handleKeyPress(e.getKeyCode());
                    return true; // 消耗这个事件
                }
                return false;
            }
        };
        
        // 注册键盘事件监听器
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(dispatcher);
    }
    
    private JPanel createPlayer1Panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // 向左旋转
        p1LeftButton = createKeyButton("向左旋转:", KeyBindingsConfig.getSingleLeftKey());
        p1LeftButton.addActionListener(e -> startSetting(p1LeftButton));
        panel.add(p1LeftButton, gbc);
        gbc.gridy++;
        
        // 向右旋转
        p1RightButton = createKeyButton("向右旋转:", KeyBindingsConfig.getSingleRightKey());
        p1RightButton.addActionListener(e -> startSetting(p1RightButton));
        panel.add(p1RightButton, gbc);
        gbc.gridy++;
        
        // 发射泡泡
        p1ShootButton = createKeyButton("发射泡泡:", KeyBindingsConfig.getSingleShootKey());
        p1ShootButton.addActionListener(e -> startSetting(p1ShootButton));
        panel.add(p1ShootButton, gbc);
        gbc.gridy++;
        
        return panel;
    }
    
    private JPanel createPlayer2Panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // 向左旋转
        p2LeftButton = createKeyButton("向左旋转:", KeyBindingsConfig.getP2LeftKey());
        p2LeftButton.addActionListener(e -> startSetting(p2LeftButton));
        panel.add(p2LeftButton, gbc);
        gbc.gridy++;
        
        // 向右旋转
        p2RightButton = createKeyButton("向右旋转:", KeyBindingsConfig.getP2RightKey());
        p2RightButton.addActionListener(e -> startSetting(p2RightButton));
        panel.add(p2RightButton, gbc);
        gbc.gridy++;
        
        // 发射泡泡
        p2ShootButton = createKeyButton("发射泡泡:", KeyBindingsConfig.getP2ShootKey());
        p2ShootButton.addActionListener(e -> startSetting(p2ShootButton));
        panel.add(p2ShootButton, gbc);
        gbc.gridy++;
        
        return panel;
    }
    
    private JButton createKeyButton(String label, int keyCode) {
        JButton button = new JButton(label + " " + getKeyText(keyCode));
        button.setFont(new Font("Arial", Font.PLAIN, 13));
        button.setPreferredSize(new Dimension(200, 35));
        button.setFocusPainted(false);
        return button;
    }
    
    private void startSetting(JButton button) {
        // 重置所有按钮状态
        resetButtonColors();
        
        currentSettingButton = button;
        button.setText(button.getText().split(":")[0] + ": 请按新按键...");
        button.setBackground(new Color(255, 255, 200));
    }
    
    private void handleKeyPress(int keyCode) {
        if (currentSettingButton == null) return;
        
        // Esc 键取消
        if (keyCode == KeyEvent.VK_ESCAPE) {
            cancelSetting();
            return;
        }
        
        // 检查冲突
        if (isKeyConflict(keyCode)) {
            JOptionPane.showMessageDialog(this, 
                "该按键已被使用，请选择其他按键！", 
                "按键冲突", 
                JOptionPane.WARNING_MESSAGE);
            cancelSetting();
            return;
        }
        
        // 更新按键
        updateBinding(keyCode);
        currentSettingButton = null;
        resetButtonColors();
    }
    
    private boolean isKeyConflict(int keyCode) {
        // 玩家 1 面板冲突检测
        if (currentSettingButton == p1LeftButton) {
            return keyCode == newP1RightKey || keyCode == newP1ShootKey;
        } else if (currentSettingButton == p1RightButton) {
            return keyCode == newP1LeftKey || keyCode == newP1ShootKey;
        } else if (currentSettingButton == p1ShootButton) {
            return keyCode == newP1LeftKey || keyCode == newP1RightKey;
        }
        // 玩家 2 面板冲突检测
        else if (currentSettingButton == p2LeftButton) {
            return keyCode == newP2RightKey || keyCode == newP2ShootKey;
        } else if (currentSettingButton == p2RightButton) {
            return keyCode == newP2LeftKey || keyCode == newP2ShootKey;
        } else if (currentSettingButton == p2ShootButton) {
            return keyCode == newP2LeftKey || keyCode == newP2RightKey;
        }
        return false;
    }
    
    private void updateBinding(int keyCode) {
        String labelPrefix = currentSettingButton.getText().split(":")[0];
        
        if (currentSettingButton == p1LeftButton) {
            newP1LeftKey = keyCode;
        } else if (currentSettingButton == p1RightButton) {
            newP1RightKey = keyCode;
        } else if (currentSettingButton == p1ShootButton) {
            newP1ShootKey = keyCode;
        } else if (currentSettingButton == p2LeftButton) {
            newP2LeftKey = keyCode;
        } else if (currentSettingButton == p2RightButton) {
            newP2RightKey = keyCode;
        } else if (currentSettingButton == p2ShootButton) {
            newP2ShootKey = keyCode;
        }
        
        currentSettingButton.setText(labelPrefix + ": " + getKeyText(keyCode));
    }
    
    private void cancelSetting() {
        if (currentSettingButton == p1LeftButton) {
            p1LeftButton.setText("向左旋转：" + getKeyText(newP1LeftKey));
        } else if (currentSettingButton == p1RightButton) {
            p1RightButton.setText("向右旋转：" + getKeyText(newP1RightKey));
        } else if (currentSettingButton == p1ShootButton) {
            p1ShootButton.setText("发射泡泡：" + getKeyText(newP1ShootKey));
        } else if (currentSettingButton == p2LeftButton) {
            p2LeftButton.setText("向左旋转：" + getKeyText(newP2LeftKey));
        } else if (currentSettingButton == p2RightButton) {
            p2RightButton.setText("向右旋转：" + getKeyText(newP2RightKey));
        } else if (currentSettingButton == p2ShootButton) {
            p2ShootButton.setText("发射泡泡：" + getKeyText(newP2ShootKey));
        }
        currentSettingButton = null;
        resetButtonColors();
    }
    
    private void resetButtonColors() {
        p1LeftButton.setBackground(new Color(240, 240, 240));
        p1RightButton.setBackground(new Color(240, 240, 240));
        p1ShootButton.setBackground(new Color(240, 240, 240));
        p2LeftButton.setBackground(new Color(240, 240, 240));
        p2RightButton.setBackground(new Color(240, 240, 240));
        p2ShootButton.setBackground(new Color(240, 240, 240));
    }
    
    private void restoreDefaults() {
        newP1LeftKey = 37;
        newP1RightKey = 39;
        newP1ShootKey = 32;
        newP2LeftKey = 37;
        newP2RightKey = 39;
        newP2ShootKey = 38;
        
        p1LeftButton.setText("向左旋转：" + getKeyText(newP1LeftKey));
        p1RightButton.setText("向右旋转：" + getKeyText(newP1RightKey));
        p1ShootButton.setText("发射泡泡：" + getKeyText(newP1ShootKey));
        p2LeftButton.setText("向左旋转：" + getKeyText(newP2LeftKey));
        p2RightButton.setText("向右旋转：" + getKeyText(newP2RightKey));
        p2ShootButton.setText("发射泡泡：" + getKeyText(newP2ShootKey));
    }
    
    private void saveSettings() {
        // 直接更新 KeyBindingsConfig 的静态变量
        KeyBindingsConfig.SINGLE_LEFT_KEY = newP1LeftKey;
        KeyBindingsConfig.SINGLE_RIGHT_KEY = newP1RightKey;
        KeyBindingsConfig.SINGLE_SHOOT_KEY = newP1ShootKey;
        KeyBindingsConfig.P2_LEFT_KEY = newP2LeftKey;
        KeyBindingsConfig.P2_RIGHT_KEY = newP2RightKey;
        KeyBindingsConfig.P2_SHOOT_KEY = newP2ShootKey;
        
        JOptionPane.showMessageDialog(this, "按键设置已保存（本次游戏有效）！", "提示", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
    
    private String getKeyText(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT: return "←左";
            case KeyEvent.VK_RIGHT: return "→右";
            case KeyEvent.VK_UP: return "↑上";
            case KeyEvent.VK_DOWN: return "↓下";
            case KeyEvent.VK_SPACE: return "空格";
            case KeyEvent.VK_W: return "W";
            case KeyEvent.VK_A: return "A";
            case KeyEvent.VK_S: return "S";
            case KeyEvent.VK_D: return "D";
            case KeyEvent.VK_R: return "R";
            default: return KeyEvent.getKeyText(keyCode);
        }
    }
    
    @Override
    public void dispose() {
        // 移除键盘事件监听器
        if (dispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .removeKeyEventDispatcher(dispatcher);
        }
        super.dispose();
    }
}
