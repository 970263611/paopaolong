package com.ppl;

/**
 * 按键配置管理器（已移除配置文件支持）
 */
public class KeyBindingsConfig {
    
    // 玩家 1 默认按键配置
    public static int SINGLE_LEFT_KEY = 65;      // A 键 (VK_A)
    public static int SINGLE_RIGHT_KEY = 68;     // D 键 (VK_D)
    public static int SINGLE_SHOOT_KEY = 87;     // W 键 (VK_W)
    
    // 玩家 2 默认按键配置
    public static int P2_LEFT_KEY = 74;          // J 键 (VK_J)
    public static int P2_RIGHT_KEY = 76;         // L 键 (VK_L)
    public static int P2_SHOOT_KEY = 73;         // I 键 (VK_I)
    
    static {
        // 初始化默认配置（不再从文件加载）
    }
    
    /**
     * 获取玩家 1 向左旋转按键
     */
    public static int getLeftKey() {
        return SINGLE_LEFT_KEY;  // 向后兼容
    }
    
    /**
     * 获取向右旋转按键的键码
     */
    public static int getRightKey() {
        return SINGLE_RIGHT_KEY;  // 向后兼容
    }
    
    /**
     * 获取发射按键的键码
     */
    public static int getShootKey() {
        return SINGLE_SHOOT_KEY;  // 向后兼容
    }
    
    // ===== 新增的单人模式 getter 方法 =====
    
    /**
     * 获取单人模式向左旋转按键
     */
    public static int getSingleLeftKey() {
        return SINGLE_LEFT_KEY;
    }
    
    /**
     * 获取单人模式向右旋转按键
     */
    public static int getSingleRightKey() {
        return SINGLE_RIGHT_KEY;
    }
    
    /**
     * 获取单人模式发射按键
     */
    public static int getSingleShootKey() {
        return SINGLE_SHOOT_KEY;
    }
    
    // ===== 新增的双人模式玩家 2 getter 方法 =====
    
    /**
     * 获取玩家 2 向左旋转按键
     */
    public static int getP2LeftKey() {
        return P2_LEFT_KEY;
    }
    
    /**
     * 获取玩家 2 向右旋转按键
     */
    public static int getP2RightKey() {
        return P2_RIGHT_KEY;
    }
    
    /**
     * 获取玩家 2 发射按键
     */
    public static int getP2ShootKey() {
        return P2_SHOOT_KEY;
    }
}
