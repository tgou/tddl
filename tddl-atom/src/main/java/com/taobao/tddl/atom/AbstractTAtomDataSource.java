package com.taobao.tddl.atom;

import com.taobao.tddl.common.utils.mbean.TddlMBeanServer;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 抽象的TAtomDataSource 定义，实现了DataSource接口 并且定义了自己的接口抽象方法
 *
 * @author qihao
 */
public abstract class AbstractTAtomDataSource implements TAtomDsStandard {

    public static void main(String[] args) throws SQLException {
        StaticTAtomDataSource s = new StaticTAtomDataSource();
        System.out.println(s.isWrapperFor(StaticTAtomDataSource.class));
    }

    protected abstract DataSource getDataSource() throws SQLException;

    public abstract void init() throws Exception;

    public abstract void flushDataSource();

    public abstract void destroyDataSource() throws Exception;

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return getDataSource().getConnection(username, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return getDataSource().getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        getDataSource().setLogWriter(out);

    }

    public int getLoginTimeout() throws SQLException {
        return getDataSource().getLoginTimeout();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        getDataSource().setLoginTimeout(seconds);
    }

    public void setShutDownMBean(boolean shutDownMBean) {
        TddlMBeanServer.shutDownMBean = shutDownMBean;
    }

    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (isWrapperFor(iface)) {
            return (T) this;
        } else {
            throw new SQLException("not a wrapper for " + iface);
        }
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return AbstractTAtomDataSource.class.isAssignableFrom(iface);
    }
}
