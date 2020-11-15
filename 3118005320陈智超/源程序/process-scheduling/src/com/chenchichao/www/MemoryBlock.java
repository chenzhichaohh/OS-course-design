package com.chenchichao.www;

/**
 * @author ChenZhichao
 * @mail chenzhichaohh@163.com
 * @create 2020-06-25
 */
public class MemoryBlock {
    int address = 0;
    int length = 0;

    // Busy或 Free
    String status = "Free";

    public MemoryBlock(int address, int length) {
        this.address = address;
        this.length = length;
    }
}
