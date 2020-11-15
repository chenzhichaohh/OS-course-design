package com.chenchichao.www;

/**
 * @author ChenZhichao
 * @mail chenzhichaohh@163.com
 * @create 2020-06-25
 */
public class PCB {
    // 进程名
    String name;
    // 所需内存
    int needMemory;
    // 主存起始位置
    int address;
    // 到达时间
    double arriveTime;
    // 需要运行时间
    double needTime;
    // 已用时间
    double hasUsedTime;
    // 进程状态：运行中、就绪、阻塞(Running、 Waiting、 Blocking)
    String status;
}
