package com.chenchichao.www;

import java.util.LinkedList;
import java.util.Random;

/**
 * @author ChenZhichao
 * @mail chenzhichaohh@163.com
 * @create 2020-06-25
 */
public class ProcessMemoryScheduling {

    //������
    private static int PROCESS_NUM = 10;
    // double�ͱ���С�����ȷ��β��
    private static int EXACT_DIGIT = 1;
    //�������Ľ���������С�������ʱ�Ӻ󱸶��е������
    private static int MAX_OCCURS = 5;
    // �������̵ı�ʶ����ʾ�Ƿ��н�����������״̬
    private int blocked = 0;
    // �������̵��±꣬û������������Ϊ-1
    private int blockIndex = -1;

    // �������飬������н���
    private PCB[] pcb = new PCB[PROCESS_NUM];
    private LinkedList<MemoryBlock> memoryBlockList = new LinkedList<>();

    // ʱ��Ƭ�Ĵ�С
    private double timeSlice;
    // ��ǰ���еĽ��̵��±꣨��������û����Ϊ-1
    private int runningIndex;
    // ÿ�ε��ȳ��򶼼�¼��ǰʱ��
    private long lastTime;
    // ����ʼ���е�ʱ��
    private long beginTime;

    /**
     * �ж����н����Ƿ�������
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
     * ��ʼ����������
     */
    public void initData() {
        runningIndex = -1;
        Random r = new Random();
        for (int n = 0; n < PROCESS_NUM; n++) {
            pcb[n] = new PCB();
            pcb[n].name = "P" + Integer.toString(n + 1);
            // �����������ʱ�䣬 4 - 12��
            pcb[n].needTime = r.nextDouble() * 4 + 12;
            // ���������Ҫ���ڴ� ��38 - 150
            pcb[n].needMemory = r.nextInt(122) + 38;


            if (n == 0) {
                pcb[n].arriveTime = 0;
                pcb[0].needMemory = 50;
            } else {
                pcb[n].arriveTime = r.nextDouble() * 5 + 1;
                // ���������Ҫ���ڴ� ��38 - 150
                pcb[n].needMemory = r.nextInt(122) + 38;
            }
        }
        for (int n = 0; n < PROCESS_NUM; n++) {
            pcb[n].hasUsedTime = 0;
            pcb[n].address = 0;
            pcb[n].status = "U";
        }

        // �����û��ڴ��� 0 -1024 ֮��
        MemoryBlock firstBlock = new MemoryBlock(0, 1024);
        memoryBlockList.add(firstBlock);
        print();
    }

    /**
     * ��ȷ��λ���Ľ�ͼ
     * @param d
     * @return
     */
    public String d2s(double d) {
        String tmp = d + "";
        int index = tmp.indexOf(".");
        return tmp.substring(0, index + EXACT_DIGIT + 1);
    }


    /**
     * һ�λ����㷨��Ϊ������׼��
     * @param i
     * @param j
     * @return
     */
    int getStandard( int i, int j) {
        //��׼����
        PCB key = pcb[i];
        while (i < j) {
            //��ΪĬ�ϻ�׼�Ǵ���߿�ʼ�����Դ��ұ߿�ʼ�Ƚ�
            //����β��Ԫ�ش��ڵ��ڻ�׼���� ʱ,��һֱ��ǰŲ�� j ָ��
            while (i < j && pcb[j].arriveTime >= key.arriveTime) {
                j--;
            }
            //���ҵ��� array[i] С��ʱ���ͰѺ����ֵ array[j] ������
            if (i < j) {
                pcb[i] = pcb[j];
            }
            //������Ԫ��С�ڵ��ڻ�׼���� ʱ,��һֱ���Ų�� i ָ��
            while (i < j && pcb[i].arriveTime <= key.arriveTime) {
                i++;
            }
            //���ҵ��� array[j] ���ʱ���Ͱ�ǰ���ֵ array[i] ������
            if (i < j) {
                pcb[j] = pcb[i];
            }
        }
        //����ѭ��ʱ i �� j ���,��ʱ�� i �� j ���� key ����ȷ����λ��
        //�ѻ�׼���ݸ�����ȷλ��
        pcb[i] = key;
        return i;
    }

    /**
     * ���ݵ���ʱ��ʹ�ÿ����㷨��������
     * @param low
     * @param high
     */
    public void quickSortByArriTime(int low, int high) {
        //��ʼĬ�ϻ�׼Ϊ low
        if (low < high) {
            //�ֶ�λ���±�
            int standard = getStandard( low, high);
            //�ݹ��������
            //�������
            quickSortByArriTime( low, standard - 1);
            //�ұ�����
            quickSortByArriTime(standard + 1, high);
        }
    }



     public void print() {
        System.out.println("\n-----------------------------------------------------------------------------");
        System.out.println("�ڴ������(Busy:ռ�� Free:����)��");
        System.out.println("��ַ(K)  ���ȣ�KB��    ״̬");
        for (MemoryBlock memoryBlock : memoryBlockList) {
            System.out.printf("%-8d  %-10d  %-6s\n", memoryBlock.address, memoryBlock.length, memoryBlock.status);
        }

         // ��ӡ�󱸶����е���ҵ
         System.out.println("\n�󱸶����е���ҵ:");
         for (int i = 0; i < PROCESS_NUM; i++) {
             if (pcb[i].status == "U") {
                 System.out.print(pcb[i].name + "\t");
             }
         }

        System.out.println("\n��ǰ������PCB��Ϣ��");
        System.out.printf("������  ����ʱ��/s  ��Ҫʱ��/s  ����ʱ��/s  �����ڴ�/KB      �ڴ���ַ      ����״̬\n");
        // ��ӡ�������С������С�����״̬����ɵ����⼸��״̬�Ľ���
        for (int i = 0; i < PROCESS_NUM; i++) {
            if (pcb[i].status != "U") {
                System.out.printf("%-6s  %-10s  %-10s  %-10s  %-11d  %-8d  %-8s\n", pcb[i].name, d2s(pcb[i].arriveTime)
                        , d2s(pcb[i].needTime), d2s(pcb[i].hasUsedTime), pcb[i].needMemory, pcb[i].address, pcb[i].status);
            }
        }


        System.out.println("-----------------------------------------------------------------------------");
         // �����ϴε���ʱ��
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

    // �Ӻ󱸶�����ѡ����ҵ�������ڴ�
    public void getNextOnDisk(double allTime) {
        int memoryProcessNum = 0;//���ڴ��еĽ��̸���
        for (int i = 0; i < pcb.length; i++) {
            if (pcb[i].status.equals("Waiting") || pcb[i].status.equals("Running") || pcb[i].status.equals("Blocking")) {
                memoryProcessNum++;
            }
        }
        if (memoryProcessNum < MAX_OCCURS) {
            // ����ڴ��г�����������󲢷������Ӻ󱸶����ҵ����ȵ���Ľ��̵����ڴ�
            for (int p = 0; p < pcb.length && memoryProcessNum < MAX_OCCURS; p++) {
                if (pcb[p].status.equals("U") && allTime >= pcb[p].arriveTime) {
                    int address = applyMemory(p);
                    if (address != -2) {//�����ڴ�ɹ����޸�״̬
                        pcb[p].address = address;
                        pcb[p].status = "Waiting";
                        memoryProcessNum++;
                    }
                }
            }
        }
    }

    /**
     * �����ڴ�
     * @param curr ���̵��������±꣩
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
        if (target == null) {//�Ҳ������ʵ��ڴ�����ڷ���
            return -2;
        } else if (target.length == needMemoryry) {
            // ������ڴ���ڴ�����պ���ȣ�ֱ�ӽ���������ָ��ý���
            target.status = "Busy";
            return target.address;
        } else {
            target.status = "Busy";
            MemoryBlock block = new MemoryBlock(target.address + needMemoryry, target.length - needMemoryry);
            target.length -= block.length;
            // ���½��Ŀ��з������뵽��������
            memoryBlockList.add(memoryBlockList.indexOf(target) + 1, block);
            return target.address;

        }
    }

    /**
     * ����������֮���ͷ��ڴ�
     * @param address ���̵���ʼ��ַ
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
     * ���ȷ���
     * @throws InterruptedException
     */
    public void dispatch() throws InterruptedException {
        if (isFinishALLProcess()) {
            return;
        }

        long now = System.currentTimeMillis();
        double passedTime = (now - lastTime) / 1000.0; //�����ϴε��Ⱦ�����ʱ��
        double allTime = (now - beginTime) / 1000.0;

        // �Ӻ󱸶��е������
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
                // �Ӿ���������ѡ����һ������
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

        // �����������
        Random random = new Random();
        int randomNum = random.nextInt(3);

        // ���ϴ������Ľ��̽�������
        if (randomNum == 2 && (blocked == 0 ) ) {
            blockIndex = runningIndex;
            pcb[runningIndex].status = "Blocking";
            blocked = 1;
            runningIndex = getNextWaiting(runningIndex);
        }
        // ���ѽ���
        if (blocked == 1) {
            long time = System.currentTimeMillis();
            double intervalTime = (time - lastTime) / 1000.0;
            if (runningIndex == -1) {
                // ֻʣ�����һ����������δִ���꣬��ý�������1�����
                Thread.sleep(1000);
                runningIndex = blockIndex;
                pcb[runningIndex].status = "Waiting";
                blocked = 0;
            } else if (intervalTime > 2.0) {
                // �н�����������״̬ʱ����������ʱ�����2s���ѽ���
                if (blockIndex != -1) {
                    pcb[blockIndex].status = "Waiting";
                    blocked = 0;
                }
            }


        }
    }

    /**
     * �������̵���
     * @throws InterruptedException
     */
    public void run() throws InterruptedException {
        System.out.printf("                       \n");
        System.out.printf("* * * * * * * * �ڴ��������̵��� * * * * * * * *\n\n");
        initData();
        timeSlice=2;

        // ʹ�ÿ��������㷨�����������ȷ����ԭ������
        quickSortByArriTime(0, PROCESS_NUM - 1);
        beginTime = System.currentTimeMillis();
        lastTime = System.currentTimeMillis();

        while (true) {
            dispatch();
            // ������н��̶��Ѿ���ɵ��ȣ����˳�ѭ��
            if (isFinishALLProcess()) {
                break;
            }
        }
        print();
        System.out.printf("* * * * * * * * ���н������н��� * * * * * * * *\n\n");
    }



}