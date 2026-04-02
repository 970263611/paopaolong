#!/bin/bash

# 泡泡龙游戏运行脚本

# 创建输出目录
mkdir -p out

# 编译 Java 文件
echo "正在编译游戏..."
javac -d out src/main/java/com/ppl/*.java

if [ $? -eq 0 ]; then
    echo "编译成功！正在启动游戏..."
    # 运行游戏
    java -cp out com.ppl.BubbleDragonGame
else
    echo "编译失败，请检查错误信息"
    exit 1
fi

