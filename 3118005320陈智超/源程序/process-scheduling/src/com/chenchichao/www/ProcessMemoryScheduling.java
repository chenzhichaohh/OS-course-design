package com.chenchichao.www;

import java.util.LinkedList;
import java.util.Random;

/**
 * @author ChenZhichao
 * @mail chenzhichaohh@163.com
 * @create 2020-06-25
 */
public class ProcessMemoryScheduling {

    //进程数
    private static int PROCESS_NUM = 10;
    // double型变量小数点后精确的尾数
    private static int EXACT_DIGIT = 1;
    //允许并发的进程数量，小于这个数时从后备队列调入进程
    private static int MAX_OCCURS = 5;
    // 阻塞进程的标识，表示是否有进程属于阻塞状态
    private int blocked = 0;
    // 阻塞进程的下标，没有阻塞进程则为-1
    private int blockIndex = -1;

    // 进程数组，存放所有进程
    private PCB[] pcb = new PCB[PROCESS_NUM];
    private LinkedList<MemoryBlock> memoryBlockList = new LinkedList<>();

    // 时间片的大小
    private double timeSlice;
    // 当前运行的进程的下标（索引），没有则为-1
    private int runningIndex;
    // 每次调度程序都记录当前时间
    private long lastTime;
    // 程序开始运行的时间
    private long beginTime;

    /**
     * 判断所有进程是否运行完
     * @return
     */
    public boolean isFinishALLProcess() {
        for (int i = 0; i < PROCESS_NUM; i++) {
            if (pcb[i].status != "Finish") {
                return false;
            }
        }
        return true;
    }

    /**
     * 初始化测试数据
     */
    public void initData() {
        runningIndex = -1;
        Random r = new Random();
        for (int n = 0; n < PROCESS_NUM; n++) {
            pcb[n] = new PCB();
            pcb[n].name = "P" + Integer.toString(n + 1);
            // 随机产生服务时间， 4 - 12秒
            pcb[n].needTime = r.nextDouble() * 4 + 12;
            // 随机产生需要的内存 ，38 - 150
            pcb[n].needMemory = r.nextInt(122) + 38;


            if (n == 0) {
                pcb[n].arriveTime = 0;
                pcb[0].needMemory = 50;
            } else {
                pcb[n].arriveTime = r.nextDouble() * 5 + 1;
                // 随机产生需要的内存 ，38 - 150
                pcb[n].needMemory = r.nextInt(122) + 38;
            }
        }
        for (int n = 0; n < PROCESS_NUM; n++) {
            pcb[n].hasUsedTime = 0;
            pcb[n].address = 0;
            pcb[n].status = "U";
        }

        // 设置用户内存在 0 -1024 之间
        MemoryBlock firstBlock = new MemoryBlock(0, 1024);
        memoryBlockList.add(firstBlock);
        print();
    }

    /**
     * 精确度位数的截图
     * @param d
     * @return
     */
    public String d2s(double d) {
        String tmp = d + "";
        int index = tmp.indexOf(".");
        return tmp.substring(0, index + EXACT_DIGIT + 1);
    }


    /**
     * 一次划分算法，为快排做准备
     * @param i
     * @param j
     * @return
     */
    int getStandard( int i, int j) {
        //基准数据
        PCB key = pcb[i];
        while (i < j) {
            //因为默认基准是从左边开始，所以从右边开始比较
            //当队尾的元素大于等于基准数据 时,就一直向前挪动 j 指针
            while (i < j && pcb[j].arriveTime >= key.arriveTime) {
                j--;
            }
            //当找到比 array[i] 小的时，就把后面的值 array[j] 赋给它
            if (i < j) {
                pcb[i] = pcb[j];
            }
            //当队首元素小于等于基准数据 时,就一直向后挪动 i 指针
            while (i < j && pcb[i].arriveTime <= key.arriveTime) {
                i++;
            }
            //当找到比 array[j] 大的时，就把前面的值 array[i] 赋给它
            if (i < j) {
                pcb[j] = pcb[i];
            }
        }
        //跳出循环时 i 和 j 相等,此时的 i 或 j 就是 key 的正确索引位置
        //把基准数据赋给正确位置
        pcb[i] = key;
        return i;
    }

    /**
     * 根据到达时间使用快排算法进行排序
     * @param low
     * @param high
     */
    public void quickSortByArriTime(int low, int high) {
        //开始默认基准为 low
        if (low < high) {
            //分段位置下标
            int standard = getStandard( low, high);
            //递归调用排序
            //左边排序
            quickSortByArriTime( low, standard - 1);
            //右边排序
            quickSortByArriTime(standard + 1, high);
        }
    }



     public void print() {
        System.out.println("\n-----------------------------------------------------------------------------");
        System.out.println("内存分区表(Busy:占用 Free:空闲)：");
        System.out.println("起址(K)  长度（KB）    状态");
        for (MemoryBlock memoryBlock : memoryBlockList) {
            System.out.printf("%-8d  %-10d  %-6s\n", memoryBlock.address, memoryBlock.length, memoryBlock.status);
        }

         // 打印后备队列中的作业
         System.out.println("\n后备队列中的作业:");
         for (int i = 0; i < PROCESS_NUM; i++) {
             if (pcb[i].status == "U") {
                 System.out.print(pcb[i].name + "\t");
             }
         }

        System.out.println("\n当前各进程PCB信息：");
        System.out.printf("进程名  到达时间/s  需要时间/s  已用时间/s  所需内存/KB      内存起址      进程状态\n");
        // 打印就绪队列、运行中、阻塞状态、完成调度这几个状态的进程
        for (int i = 0; i < PROCESS_NUM; i++) {
            if (pcb[i].status != "U") {
                System.out.printf("%-6s  %-10s  %-10s  %-10s  %-11d  %-8d  %-8s\n", pcb[i].name, d2s(pcb[i].arriveTime)
                        , d2s(pcb[i].needTime), d2s(pcb[i].hasUsedTime), pcb[i].needMemory, pcb[i].address, pcb[i].status);
            }
        }


        System.out.println("-----------------------------------------------------------------------------");
         // 更新上次调度时间
        lastTime = System.currentTimeMillis();
    }


    public int getNextWaiting(int curr) {
        int p = curr;
        while ((p = ++p % PROCESS_NUM) != curr) {
            if (pcb[p].status.equals( "Waiting") ) {
                return p;
            }
        }
        return -1;
    }

    // 从后备队列中选择作业并申请内存
    public void getNextOnDisk(double allTime) {
        int memoryProcessNum = 0;//在内存中的进程个数
        for (int i = 0; i < pcb.length; i++) {
            if (pcb[i].status.equals("Waiting") || pcb[i].status.equals("Running") || pcb[i].status.equals("Blocking")) {
                memoryProcessNum++;
            }
        }
        if (memoryProcessNum < MAX_OCCURS) {
            // 如果内存中程序数少于最大并发数，从后备队列找到最先到达的进程调入内存
            for (int p = 0; p < pcb.length && memoryProcessNum < MAX_OCCURS; p++) {
                if (pcb[p].status.equals("U") && allTime >= pcb[p].arriveTime) {
                    int address = applyMemory(p);
                    if (address != -2) {//分配内存成功才修改状态
                        pcb[p].address = address;
                        pcb[p].status = "Waiting";
                        memoryProcessNum++;
                    }
                }
            }
        }
    }

    /**
     * 申请内存
     * @param curr 进程的索引（下标）
     * @return
     */
    public int applyMemory(int curr) {
        if (pcb[curr].status != "U") {
            return -2;
        }

        MemoryBlock target = null;
        int needMemoryry = pcb[curr].needMemory;
        for (MemoryBlock memoryBlock : memoryBlockList) {
            if (memoryBlock.status.equals("Free") && memoryBlock.length >= needMemoryry) {
                target = memoryBlock;
                break;
            }
        }
        if (target == null) {//找不到合适的内存块用于分配
            return -2;
        } else if (target.length == needMemoryry) {
            // 申请的内存和内存分区刚好相等，直接将整块分区分给该进程
            target.status = "Busy";
            return target.address;
        } else {
            target.status = "Busy";
            MemoryBlock block = new MemoryBlock(target.address + needMemoryry, target.length - needMemoryry);
            target.length -= block.length;
            // 将新建的空闲分区插入到分区连中
            memoryBlockList.add(memoryBlockList.indexOf(target) + 1, block);
            return target.address;

        }
    }

    /**
     * 进程运行完之后释放内存
     * @param address 进程的起始地址
     */
    public void releaseMemory(int address) {
        MemoryBlock target = null;
        for (MemoryBlock memoryBlock : memoryBlockList) {
            if (memoryBlock.address == address) {
                target = memoryBlock;
                break;
            }
        }
        if (target == null){
            return;
        }

        if(memoryBlockList.size()==1){
            target.status = "Free";
            return;
        }


        int index = memoryBlockList.indexOf(target);

        if (index == 0) {
            MemoryBlock memoryBlock = memoryBlockList.get(1);
            if (memoryBlock.status.equals( "Free")) {
                target.length += memoryBlock.length;
                memoryBlockList.remove(memoryBlock);
            }
            target.status = "Free";
        } else if (index == memoryBlockList.size() - 1) {
            MemoryBlock m = memoryBlockList.get(index - 1);
            if (m.status.equals("Free") ) {
                m.length += target.length;
                memoryBlockList.remove(target);
            }
            target.status = "Free";
        } else {
            MemoryBlock preMemoryBlock= memoryBlockList.get(index - 1);
            MemoryBlock nextMemoryBlock = memoryBlockList.get(index + 1);
            if (preMemoryBlock.status.equals("Free")  && nextMemoryBlock.status.equals( "Free")) {
                preMemoryBlock.length += target.length;
                preMemoryBlock.length += nextMemoryBlock.length;
                memoryBlockList.remove(target);
                memoryBlockList.remove(nextMemoryBlock);
            } else if (preMemoryBlock.status.equals("Free")  && nextMemoryBlock.status.equals("Busy") ) {
                preMemoryBlock.length += target.length;
                memoryBlockList.remove(target);
            } else if (preMemoryBlock.status.equals("Busy")  && nextMemoryBlock.status.equals("Free") ) {
                target.length += nextMemoryBlock.length;
                target.status = "Free";
                memoryBlockList.remove(nextMemoryBlock);
            } else {
                target.status = "Free";
            }
        }


    }

    /**
     * 调度方法
     * @throws InterruptedException
     */
    public void dispatch() throws InterruptedException {
        if (isFinishALLProcess()) {
            return;
        }

        long now = System.currentTimeMillis();
        double passedTime = (now - lastTime) / 1000.0; //距离上次调度经过的时间
        double allTime = (now - beginTime) / 1000.0;

        // 从后备队列调入进程
        getNextOnDisk(allTime);

        if (runningIndex != -1) {
            double oldhasUsedTime = pcb[runningIndex].hasUsedTime;

            if (passedTime >= timeSlice) {
                if (oldhasUsedTime + passedTime >= pcb[runningIndex].needTime) {
                    pcb[runningIndex].hasUsedTime = pcb[runningIndex].needTime;
                    pcb[runningIndex].status = "Finish";
                    releaseMemory(pcb[runningIndex].address);
                } else {
                    pcb[runningIndex].hasUsedTime = oldhasUsedTime + timeSlice;
                    pcb[runningIndex].status = "Waiting";
                }
                // 从就绪队列中选择下一个进程
                int next = getNextWaiting(runningIndex);
                if (next != -1) {
                    pcb[next].status = "Running";
                    runningIndex = next;
                    print();
                }
            }

        } else {
            pcb[0].status = "Running";
            runningIndex = 0;
            print();
        }

        // 随机阻塞进程
        Random random = new Random();
        int randomNum = random.nextInt(3);

        // 符合此条件的进程将被阻塞
        if (randomNum == 2 && (blocked == 0 ) ) {
            blockIndex = runningIndex;
            pcb[runningIndex].status = "Blocking";
            blocked = 1;
            runningIndex = getNextWaiting(runningIndex);
        }
        // 唤醒进程
        if (blocked == 1) {
            long time = System.currentTimeMillis();
            double intervalTime = (time - lastTime) / 1000.0;
            if (runningIndex == -1) {
                // 只剩下最后一个阻塞进程未执行完，则该进程休眠1秒后唤醒
                Thread.sleep(1000);
                runningIndex = blockIndex;
                pcb[runningIndex].status = "Waiting";
                blocked = 0;
            } else if (intervalTime > 2.0) {
                // 有进程属于阻塞状态时，满足阻塞时间大于2s则唤醒进程
                if (blockIndex != -1) {
                    pcb[blockIndex].status = "Waiting";
                    blocked = 0;
                }
            }


        }
    }

    /**
     * 启动进程调度
     * @throws InterruptedException
     */
    public void run() throws InterruptedException {
        System.out.printf("                       \n");
        System.out.printf("* * * * * * * * 内存分配与进程调度 * * * * * * * *\n\n");
        initData();
        timeSlice=2;

        // 使用快速排序算法，按照先来先服务的原则排序
        quickSortByArriTime(0, PROCESS_NUM - 1);
        beginTime = System.currentTimeMillis();
        lastTime = System.currentTimeMillis();

        while (true) {
            dispatch();
            // 如果所有进程都已经完成调度，则退出循环
            if (isFinishALLProcess()) {
                break;
            }
        }
        print();
        System.out.printf("* * * * * * * * 所有进程运行结束 * * * * * * * *\n\n");
    }



}