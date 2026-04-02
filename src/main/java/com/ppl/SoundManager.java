package com.ppl;

import javax.sound.sampled.*;

/**
 * 游戏音效管理器（增强版）
 */
public class SoundManager {
    
    private static boolean soundEnabled = true;  // 音效开关
    
    /**
     * 初始化所有音效
     */
    public static void initSounds() {
        // 音效系统已初始化
    }
    
    /**
     * 播放发射音效（优化的"咻"声，更有力度）
     */
    public static void playShootSound() {
        if (!soundEnabled) return;
        // 使用滑音效果，从高频快速降到低频
        playSlideTone(800, 200, 300, -10);
    }
    
    /**
     * 播放弹射音效（清脆的"叮"声，带回响）
     */
    public static void playBounceSound() {
        if (!soundEnabled) return;
        // 高频短音 + 轻微的回响效果
        playToneWithDecay(1200, 150, -5);
    }
    
    /**
     * 播放消除音效（欢快的"噗噗"声，更丰富）
     */
    public static void playPopSound() {
        if (!soundEnabled) return;
        // 三个音调组合，模拟泡泡破裂的清脆感
        playToneSequence(new int[]{500, 400, 300}, new int[]{80, 80, 100});
    }
    
    /**
     * 播放落地音效（新增：沉闷的"咚"声）
     */
    public static void playLandSound() {
        if (!soundEnabled) return;
        // 低频短音，模拟碰撞
        playToneWithDecay(200, 200, -15);
    }
    
    /**
     * 播放单个音调（带衰减）
     * @param frequency 频率 (Hz)
     * @param duration 持续时间 (毫秒)
     * @param volumeDb 音量 (-60 到 0 dB)
     */
    private static void playToneWithDecay(int frequency, int duration, int volumeDb) {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            
            byte[] buffer = generateDecayWave(frequency, duration, volumeDb);
            line.write(buffer, 0, buffer.length);
            
            new Thread(() -> {
                line.drain();
                line.close();
            }).start();
        } catch (Exception e) {
            // 忽略错误
        }
    }
    
    /**
     * 播放滑音效果（频率从一个值滑到另一个值）
     */
    private static void playSlideTone(int startFreq, int endFreq, int duration, int volumeDb) {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            
            byte[] buffer = generateSlideWave(startFreq, endFreq, duration, volumeDb);
            line.write(buffer, 0, buffer.length);
            
            new Thread(() -> {
                line.drain();
                line.close();
            }).start();
        } catch (Exception e) {
            // 忽略错误
        }
    }
    
    /**
     * 播放音调序列
     */
    private static void playToneSequence(int[] frequencies, int[] durations) {
        new Thread(() -> {
            for (int i = 0; i < frequencies.length && i < durations.length; i++) {
                playToneWithDecay(frequencies[i], durations[i], -20);
                try {
                    Thread.sleep(durations[i] + 30);  // 音调间短暂间隔
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
    
    /**
     * 生成带衰减的声波数据（指数衰减）
     */
    private static byte[] generateDecayWave(int frequency, int durationMs, int volumeDb) {
        int sampleRate = 44100;
        int samples = (sampleRate * durationMs) / 1000;
        byte[] buffer = new byte[samples * 2];
        
        double amplitude = Math.pow(10, volumeDb / 20.0) * Short.MAX_VALUE;
        double decayRate = 0.05;  // 衰减速率
        
        for (int i = 0; i < samples; i++) {
            double time = (double) i / sampleRate;
            
            // 指数衰减包络
            double envelope = Math.exp(-decayRate * i / samples);
            
            // 起始快速衰减
            if (i < samples * 0.1) {
                envelope *= (0.5 + 0.5 * Math.cos(Math.PI * i / (samples * 0.1)));
            }
            
            double sample = amplitude * envelope * Math.sin(2 * Math.PI * frequency * time);
            
            short s = (short) sample;
            buffer[i * 2] = (byte) (s & 0xFF);
            buffer[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }
        
        return buffer;
    }
    
    /**
     * 生成滑音乐曲数据
     */
    private static byte[] generateSlideWave(int startFreq, int endFreq, int durationMs, int volumeDb) {
        int sampleRate = 44100;
        int samples = (sampleRate * durationMs) / 1000;
        byte[] buffer = new byte[samples * 2];
        
        double amplitude = Math.pow(10, volumeDb / 20.0) * Short.MAX_VALUE;
        
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;  // 0 到 1
            
            // 频率从 startFreq 线性变化到 endFreq
            double currentFreq = startFreq + (endFreq - startFreq) * t;
            
            // 使用平滑的包络线（淡入 + 淡出）
            double envelope;
            if (t < 0.1) {
                envelope = t / 0.1;  // 快速淡入
            } else if (t > 0.7) {
                envelope = (1.0 - t) / 0.3;  // 缓慢淡出
            } else {
                envelope = 1.0;
            }
            
            double sample = amplitude * envelope * Math.sin(2 * Math.PI * currentFreq * (double)i / sampleRate);
            
            short s = (short) sample;
            buffer[i * 2] = (byte) (s & 0xFF);
            buffer[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }
        
        return buffer;
    }
    
    /**
     * 开启/关闭音效
     */
    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }
    
    /**
     * 检查音效是否启用
     */
    public static boolean isSoundEnabled() {
        return soundEnabled;
    }
}
