package com.taobao.tddl.atom.jdbc;

import com.taobao.tddl.atom.utils.AtomDataSourceHelper;
import com.taobao.tddl.atom.utils.ConnRestrictSlot;
import com.taobao.tddl.common.utils.logger.Logger;
import com.taobao.tddl.common.utils.logger.LoggerFactory;

import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class TConnectionWrapper implements Connection {

    private static Logger log = LoggerFactory.getLogger(TConnectionWrapper.class);
    private final Connection targetConnection;
    private final TDataSourceWrapper dataSourceWrapper;
    private ConnRestrictSlot connRestrictSlot;
    private Set<TStatementWrapper> statements = new HashSet<TStatementWrapper>(1);

    public TConnectionWrapper(Connection targetConnection, ConnRestrictSlot connRestrictSlot,
                              TDataSourceWrapper dataSourceWrapper) {
        this.targetConnection = targetConnection;
        this.connRestrictSlot = connRestrictSlot;
        this.dataSourceWrapper = dataSourceWrapper;
    }

    public Connection getTargetConnection() {
        return targetConnection;
    }

    public void clearWarnings() throws SQLException {
        this.targetConnection.clearWarnings();
    }

    public void close() throws SQLException {
        try {
            this.targetConnection.close();
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("appname : ").append(dataSourceWrapper.connectionProperties.datasourceName).append(" ");
                sb.append("threadcount : ").append(dataSourceWrapper.threadCount);
            }

            if (connRestrictSlot != null) {
                connRestrictSlot.freeConnection();
                connRestrictSlot = null; // 防止重复关闭
            }
            dataSourceWrapper.threadCount.decrementAndGet();
            AtomDataSourceHelper.removeConnRestrictKey();
        }

        for (TStatementWrapper statementWrapper : this.statements) {
            try {
                statementWrapper.close(false);
            } catch (SQLException e) {
                log.error("", e);
            }
        }
    }

    void removeOpenedStatements(Statement statement) {
        if (!statements.remove(statement)) {
            log.warn("current statmenet ：" + statement + " doesn't exist!");
        }
    }

    public void commit() throws SQLException {
        this.targetConnection.commit();
    }

    public Statement createStatement() throws SQLException {
        Statement targetStatement = this.targetConnection.createStatement();
        TStatementWrapper statementWrapper = new TStatementWrapper(targetStatement, this, this.dataSourceWrapper);
        this.statements.add(statementWrapper);
        return statementWrapper;
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        Statement targetStatement = this.targetConnection.createStatement(resultSetType, resultSetConcurrency);
        TStatementWrapper statementWrapper = new TStatementWrapper(targetStatement, this, this.dataSourceWrapper);
        this.statements.add(statementWrapper);
        return statementWrapper;
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        Statement s = this.targetConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        TStatementWrapper statementWrapper = new TStatementWrapper(s, this, this.dataSourceWrapper);
        this.statements.add(statementWrapper);
        return statementWrapper;
    }

    public boolean getAutoCommit() throws SQLException {
        return this.targetConnection.getAutoCommit();
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.targetConnection.setAutoCommit(autoCommit);
    }

    public String getCatalog() throws SQLException {
        return this.targetConnection.getCatalog();
    }

    public void setCatalog(String catalog) throws SQLException {
        this.targetConnection.setCatalog(catalog);
    }

    public int getHoldability() throws SQLException {
        return this.targetConnection.getHoldability();
    }

    public void setHoldability(int holdability) throws SQLException {
        this.targetConnection.setHoldability(holdability);
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        DatabaseMetaData targetMetaData = this.targetConnection.getMetaData();
        return new DatabaseMetaDataWrapper(targetMetaData, this);
    }

    public int getTransactionIsolation() throws SQLException {
        return this.targetConnection.getTransactionIsolation();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        this.targetConnection.setTransactionIsolation(level);

    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return this.targetConnection.getTypeMap();
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        this.targetConnection.setTypeMap(map);
    }

    public SQLWarning getWarnings() throws SQLException {
        return this.targetConnection.getWarnings();
    }

    public boolean isClosed() throws SQLException {
        return this.targetConnection.isClosed();
    }

    public boolean isReadOnly() throws SQLException {
        return this.targetConnection.isReadOnly();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        this.targetConnection.setReadOnly(readOnly);
    }

    public String nativeSQL(String sql) throws SQLException {
        return this.targetConnection.nativeSQL(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        CallableStatement cs = targetConnection.prepareCall(sql);
        CallableStatementWrapper csw = new CallableStatementWrapper(cs, this, this.dataSourceWrapper, sql);
        statements.add(csw);
        return csw;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        CallableStatement cs = this.targetConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
        CallableStatementWrapper csw = new CallableStatementWrapper(cs, this, this.dataSourceWrapper, sql);
        statements.add(csw);
        return csw;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        CallableStatement cs = this.targetConnection.prepareCall(sql,
                resultSetType,
                resultSetConcurrency,
                resultSetHoldability);
        CallableStatementWrapper csw = new CallableStatementWrapper(cs, this, this.dataSourceWrapper, sql);
        statements.add(csw);
        return csw;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        PreparedStatement ps = this.targetConnection.prepareStatement(sql);
        TPreparedStatementWrapper psw = new TPreparedStatementWrapper(ps, this, this.dataSourceWrapper, sql);
        statements.add(psw);
        return psw;
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        PreparedStatement ps = this.targetConnection.prepareStatement(sql, autoGeneratedKeys);
        TPreparedStatementWrapper psw = new TPreparedStatementWrapper(ps, this, this.dataSourceWrapper, sql);
        statements.add(psw);
        return psw;
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        PreparedStatement ps = this.targetConnection.prepareStatement(sql, columnIndexes);
        TPreparedStatementWrapper psw = new TPreparedStatementWrapper(ps, this, this.dataSourceWrapper, sql);
        statements.add(psw);
        return psw;
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        PreparedStatement ps = this.targetConnection.prepareStatement(sql, columnNames);
        TPreparedStatementWrapper psw = new TPreparedStatementWrapper(ps, this, this.dataSourceWrapper, sql);
        statements.add(psw);
        return psw;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        PreparedStatement ps = this.targetConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        TPreparedStatementWrapper psw = new TPreparedStatementWrapper(ps, this, this.dataSourceWrapper, sql);
        statements.add(psw);
        return psw;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        PreparedStatement ps = this.targetConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        TPreparedStatementWrapper psw = new TPreparedStatementWrapper(ps, this, this.dataSourceWrapper, sql);
        statements.add(psw);
        return psw;
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        this.targetConnection.releaseSavepoint(savepoint);
    }

    public void rollback() throws SQLException {
        this.targetConnection.rollback();
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        this.targetConnection.rollback(savepoint);
    }

    public Savepoint setSavepoint() throws SQLException {
        return this.targetConnection.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return this.targetConnection.setSavepoint(name);
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
        return this.getClass().isAssignableFrom(iface);
    }

    public Clob createClob() throws SQLException {
        return this.targetConnection.createClob();
    }

    public Blob createBlob() throws SQLException {
        return this.targetConnection.createBlob();
    }

    public NClob createNClob() throws SQLException {
        return this.targetConnection.createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return this.targetConnection.createSQLXML();
    }

    public boolean isValid(int timeout) throws SQLException {
        return this.targetConnection.isValid(timeout);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        this.targetConnection.setClientInfo(name, value);
    }

    public String getClientInfo(String name) throws SQLException {
        return this.targetConnection.getClientInfo(name);
    }

    public Properties getClientInfo() throws SQLException {
        return this.targetConnection.getClientInfo();
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        this.targetConnection.setClientInfo(properties);
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return this.targetConnection.createArrayOf(typeName, elements);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return this.targetConnection.createStruct(typeName, attributes);
    }
}
