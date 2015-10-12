package com.taobao.tddl.optimizer.costbased.esitimater;

import com.taobao.tddl.common.exception.NotSupportException;
import com.taobao.tddl.common.utils.TddlToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 估算数据获取成本
 *
 * @author sunmengshi.pt
 */
public class Cost implements Comparable {

    /**
     * keyFilter过滤后的行数
     */
    long scanCount;
    /**
     * keyFilter与valueFilter共同过滤后的行数
     */
    private long count;
    private long diskIo;
    private boolean isOnFly;
    private long nc;        // 网络成本

    public long getRowCount() {
        return this.count;
    }

    public Cost setRowCount(long count) {
        this.count = count;
        return this;
    }

    public long getDiskIO() {
        return this.diskIo;
    }

    public Cost setDiskIO(long io) {
        this.diskIo = io;
        return this;
    }

    public boolean isOnFly() {
        return this.isOnFly;
    }

    public Cost setIsOnFly(boolean is) {
        this.isOnFly = is;
        return this;
    }

    public long getNetworkCost() {
        return nc;
    }

    public Cost setNetworkCost(long nc) {
        this.nc = nc;
        return this;
    }

    public long getScanCount() {
        return this.scanCount;
    }

    public Cost setScanCount(long count) {
        this.scanCount = count;
        return this;
    }

    public int compareTo(Object arg) {
        throw new NotSupportException();
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, TddlToStringStyle.DEFAULT_STYLE);
    }
}
