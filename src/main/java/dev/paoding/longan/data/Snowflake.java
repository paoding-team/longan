package dev.paoding.longan.data;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Snowflake {
    /**
     * 开始时间截
     */
    private final static long META_TIMESTAMP = 1564619280000L;

    /**
     * 机器id所占的位数
     */
    private final static long WORKER_ID_BIT_LENGTH = 5L;

    /**
     * 数据标识id所占的位数
     */
    private final static long DATA_CENTER_ID_BIT_LENGTH = 5L;

    /**
     * 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private final static long MAX_WORKER_ID = ~(-1L << WORKER_ID_BIT_LENGTH);

    /**
     * 支持的最大数据标识id，结果是31
     */
    private final static long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BIT_LENGTH);

    /**
     * 序列在id中占的位数
     */
    private final static long SEQUENCE_BIT_LENGTH = 12L;

    /**
     * 机器ID向左移12位
     */
    private final static long WORKER_ID_SHIFT = SEQUENCE_BIT_LENGTH;

    /**
     * 数据标识id向左移17位(12+5)
     */
    private final static long DATA_CENTER_ID_SHIFT = SEQUENCE_BIT_LENGTH + WORKER_ID_BIT_LENGTH;

    /**
     * 时间截向左移22位(5+5+12)
     */
    private final static long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BIT_LENGTH + WORKER_ID_BIT_LENGTH + DATA_CENTER_ID_BIT_LENGTH;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final static long SEQUENCE_MASK = ~(-1L << SEQUENCE_BIT_LENGTH);

    /**
     * 工作机器ID(0~31)
     */
    private final long workerId;

    /**
     * 数据中心ID(0~31)
     */
    private final long dataCenterId;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;


    public Snowflake() {
        this.workerId = getWorkerId();
        this.dataCenterId = getDataCenterId();
    }

    /**
     * 构造函数
     *
     * @param workerId     工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public Snowflake(long datacenterId, long workerId) {
        if (datacenterId > MAX_DATA_CENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        this.workerId = workerId;
        this.dataCenterId = datacenterId;
    }


    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - META_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT) //
               | (dataCenterId << DATA_CENTER_ID_SHIFT) //
               | (workerId << WORKER_ID_SHIFT) //
               | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    private long getWorkerId() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Long.parseLong(runtimeMXBean.getName().split("@")[0]) % 32;
    }

    private long getDataCenterId() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    long id = ((0x000000FF & (long) mac[mac.length - 2])
                               | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
                    return id % 32;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return 0L;
    }
}
