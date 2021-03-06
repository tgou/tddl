package com.taobao.tddl.group.jdbc;

import com.taobao.tddl.atom.jdbc.SqlMetaDataFactory;
import com.taobao.tddl.atom.jdbc.TStatement;
import com.taobao.tddl.common.jdbc.SqlTypeParser;
import com.taobao.tddl.common.model.SqlMetaData;
import com.taobao.tddl.common.model.SqlType;
import com.taobao.tddl.common.utils.logger.Logger;
import com.taobao.tddl.common.utils.logger.LoggerFactory;
import com.taobao.tddl.group.config.GroupIndex;
import com.taobao.tddl.group.dbselector.DBSelector.AbstractDataSourceTryer;
import com.taobao.tddl.group.dbselector.DBSelector.DataSourceTryer;
import com.taobao.tddl.group.utils.GroupHintParser;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author linxuan
 * @author yangzhu
 */
public class TGroupStatement implements TStatement {

    private static final Logger log = LoggerFactory.getLogger(TGroupStatement.class);

    protected TGroupConnection tGroupConnection;
    protected TGroupDataSource tGroupDataSource;
    protected int retryingTimes;
    /**
     * query time out . 超时时间，如果超时时间不为0。那么超时应该被set到真正的query中。
     */
    protected int queryTimeout = 0;
    protected int fetchSize;
    protected int maxRows;
    /**
     * 经过计算后的结果集，允许使用 getResult函数调用. 一个statement只允许有一个结果集
     */
    protected ResultSet currentResultSet;
    /**
     * 更新计数，如果执行了多次，那么这个值只会返回最后一次执行的结果。 如果是一个query，那么返回的数据应该是-1
     */
    protected int updateCount;
    protected int resultSetType = ResultSet.TYPE_FORWARD_ONLY;
    protected int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
    // jdbc规范中未指明resultSetHoldability的默认值，要设成ResultSet.CLOSE_CURSORS_AT_COMMIT吗?
    // TODO 统一设成-1吗?
    protected int resultSetHoldability = -1;
    /**
     * sql元信息持有
     */
    protected SqlMetaData sqlMetaData = null;
    ;
    /*
     * ========================================================================
     * executeBatch
     * ======================================================================
     */
    protected List<String> batchedArgs;
    /*
     * ========================================================================
     * 关闭逻辑
     * ======================================================================
     */
    protected boolean closed; // 当前statment 是否是关闭的
    protected DataSourceTryer<ResultSet> executeQueryTryer = new AbstractDataSourceTryer<ResultSet>() {

        public ResultSet tryOnDataSource(DataSourceWrapper dsw,
                                         Object... args)
                throws SQLException {
            String sql = (String) args[0];
            Connection conn = TGroupStatement.this.tGroupConnection.createNewConnection(dsw,
                    true);
            return executeQueryOnConnection(conn, sql);
        }
    };
    /**
     * 貌似是只有存储过程中会出现多结果集 因此不支持
     */
    protected boolean moreResults;
    /*
     * ========================================================================
     * 下层(有可能不是真正的)Statement的持有，getter/setter包权限
     * ======================================================================
     */
    private Statement baseStatement;
    private DataSourceTryer<Integer> executeUpdateTryer = new AbstractDataSourceTryer<Integer>() {

        public Integer tryOnDataSource(DataSourceWrapper dsw,
                                       Object... args)
                throws SQLException {
            Connection conn = TGroupStatement.this.tGroupConnection.createNewConnection(dsw,
                    false);
            return executeUpdateOnConnection(conn,
                    (String) args[0],
                    (Integer) args[1],
                    (int[]) args[2],
                    (String[]) args[3]);
        }
    };
    private DataSourceTryer<int[]> executeBatchTryer = new AbstractDataSourceTryer<int[]>() {

        public int[] tryOnDataSource(DataSourceWrapper dsw,
                                     Object... args)
                throws SQLException {
            Connection conn = TGroupStatement.this.tGroupConnection.createNewConnection(dsw,
                    false);
            return executeBatchOnConnection(conn,
                    TGroupStatement.this.batchedArgs);
        }
    };

    public TGroupStatement(TGroupDataSource tGroupDataSource, TGroupConnection tGroupConnection) {
        this.tGroupDataSource = tGroupDataSource;
        this.tGroupConnection = tGroupConnection;

        this.retryingTimes = tGroupDataSource.getRetryingTimes();
    }

    /**
     * 设置在底层执行的具体的Statement 如果前面的baseStatement未关，则先关闭
     *
     * @param baseStatement
     */
    void setBaseStatement(Statement baseStatement) {
        if (this.baseStatement != null) {
            try {
                this.baseStatement.close();
            } catch (SQLException e) {
                log.error("close baseStatement failed.", e);
            }
        }
        this.baseStatement = baseStatement;
    }

    public boolean execute(String sql) throws SQLException {
        return executeInternal(sql, -1, null, null);
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return executeInternal(sql, autoGeneratedKeys, null, null);
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return executeInternal(sql, -1, columnIndexes, null);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return executeInternal(sql, -1, null, columnNames);
    }

    // jdbc规范: 返回true表示executeQuery，false表示executeUpdate
    private boolean executeInternal(String sql, int autoGeneratedKeys, int[] columnIndexes, String[] columnNames)
            throws SQLException {
        if (SqlTypeParser.isQuerySql(sql)) {
            executeQuery(sql);
            return true;
        } else {
            if (autoGeneratedKeys == -1 && columnIndexes == null && columnNames == null) {
                executeUpdate(sql);
            } else if (autoGeneratedKeys != -1) {
                executeUpdate(sql, autoGeneratedKeys);
            } else if (columnIndexes != null) {
                executeUpdate(sql, columnIndexes);
            } else if (columnNames != null) {
                executeUpdate(sql, columnNames);
            } else {
                executeUpdate(sql);
            }

            return false;
        }
    }

    /*
     * ========================================================================
     * executeUpdate逻辑
     * ======================================================================
     */
    public int executeUpdate(String sql) throws SQLException {
        return executeUpdateInternal(sql, -1, null, null);
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return executeUpdateInternal(sql, autoGeneratedKeys, null, null);
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return executeUpdateInternal(sql, -1, columnIndexes, null);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return executeUpdateInternal(sql, -1, null, columnNames);
    }

    private int executeUpdateInternal(String sql, int autoGeneratedKeys, int[] columnIndexes, String[] columnNames)
            throws SQLException {
        checkClosed();
        ensureResultSetIsEmpty();

        Connection conn = tGroupConnection.getBaseConnection(sql, false);

        if (conn != null) {
            sql = GroupHintParser.removeTddlGroupHint(sql);
            this.updateCount = executeUpdateOnConnection(conn, sql, autoGeneratedKeys, columnIndexes, columnNames);
            return this.updateCount;
        } else {
            GroupIndex dataSourceIndex = GroupHintParser.convertHint2Index(sql);
            sql = GroupHintParser.removeTddlGroupHint(sql);
            if (dataSourceIndex == null) {
                dataSourceIndex = ThreadLocalDataSourceIndex.getIndex();
            }
            this.updateCount = this.tGroupDataSource.getDBSelector(false).tryExecute(null,
                    executeUpdateTryer,
                    retryingTimes,
                    sql,
                    autoGeneratedKeys,
                    columnIndexes,
                    columnNames,
                    dataSourceIndex);
            return this.updateCount;
        }
    }

    private int executeUpdateOnConnection(Connection conn, String sql, int autoGeneratedKeys, int[] columnIndexes,
                                          String[] columnNames) throws SQLException {
        Statement stmt = createStatementInternal(conn, sql, false);

        if (autoGeneratedKeys == -1 && columnIndexes == null && columnNames == null) {
            return stmt.executeUpdate(sql);
        } else if (autoGeneratedKeys != -1) {
            return stmt.executeUpdate(sql, autoGeneratedKeys);
        } else if (columnIndexes != null) {
            return stmt.executeUpdate(sql, columnIndexes);
        } else if (columnNames != null) {
            return stmt.executeUpdate(sql, columnNames);
        } else {
            return stmt.executeUpdate(sql);
        }
    }

    /**
     * 会调用setBaseStatement以关闭已有的Statement
     */
    private Statement createStatementInternal(Connection conn, String sql, boolean isBatch) throws SQLException {
        Statement stmt;
        if (isBatch) {
            stmt = conn.createStatement();
        } else {
            int resultSetHoldability = this.resultSetHoldability;
            if (resultSetHoldability == -1) {// 未调用过setResultSetHoldability
                resultSetHoldability = conn.getHoldability();
            }
            stmt = conn.createStatement(this.resultSetType, this.resultSetConcurrency, resultSetHoldability);
        }

        setBaseStatement(stmt); // 会关闭已有的Statement
        stmt.setQueryTimeout(queryTimeout); // 这句也有可能抛出异常，放在最后
        stmt.setFetchSize(fetchSize);
        stmt.setMaxRows(maxRows);
        // 填充sql元信息
        fillSqlMetaData(stmt, sql);
        return stmt;
    }

    public void addBatch(String sql) throws SQLException {
        checkClosed();
        if (batchedArgs == null) {
            batchedArgs = new LinkedList<String>();
        }
        if (sql != null) {
            batchedArgs.add(sql);
        }
    }

    public void clearBatch() throws SQLException {
        checkClosed();
        if (batchedArgs != null) {
            batchedArgs.clear();
        }
    }

    public int[] executeBatch() throws SQLException {
        try {
            checkClosed();
            ensureResultSetIsEmpty();

            if (batchedArgs == null || batchedArgs.isEmpty()) {
                return new int[0];
            }

            Connection conn = tGroupConnection.getBaseConnection(null, false);

            if (conn != null) {
                // 如果当前已经有连接,则不做任何重试。对于更新来说，不管有没有事务，
                // 用户总期望getConnection获得连接之后，后续的一系列操作都在这同一个库，同一个连接上执行
                return executeBatchOnConnection(conn, this.batchedArgs);
            } else {
                return tGroupDataSource.getDBSelector(false).tryExecute(null, executeBatchTryer, retryingTimes);
            }
        } finally {
            if (batchedArgs != null) {
                batchedArgs.clear();
            }
        }
    }

    private int[] executeBatchOnConnection(Connection conn, List<String> batchedSqls) throws SQLException {
        Statement stmt = createStatementInternal(conn, batchedSqls.get(0), true);
        for (String sql : batchedSqls) {
            stmt.addBatch(sql);
        }
        return stmt.executeBatch();
    }

    public void close() throws SQLException {
        close(true);
    }

    void close(boolean removeThis) throws SQLException {
        if (closed) {
            return;
        }
        closed = true;

        try {
            if (currentResultSet != null) {
                currentResultSet.close();
            }
        } catch (SQLException e) {
            log.warn("Close currentResultSet failed.", e);
        } finally {
            currentResultSet = null;
        }

        try {
            if (this.baseStatement != null) {
                this.baseStatement.close();
            }
        } finally {
            this.baseStatement = null;
            if (removeThis) {
                tGroupConnection.removeOpenedStatements(this);
            }
        }
    }

    protected void checkClosed() throws SQLException {
        if (closed) {
            throw new SQLException("No operations allowed after statement closed.");
        }
    }

    /**
     * 如果新建了查询，那么上一次查询的结果集应该被显示的关闭掉。这才是符合jdbc规范的
     *
     * @throws SQLException
     */
    protected void ensureResultSetIsEmpty() throws SQLException {

        if (currentResultSet != null) {
            // log.debug("result set is not null,close current result set");
            try {
                currentResultSet.close();
            } catch (SQLException e) {
                log.error("exception on close last result set . can do nothing..", e);
            } finally {
                // 最终要显示的关闭它
                currentResultSet = null;
            }
        }

    }

    /*
     * ========================================================================
     * executeQuery 查询逻辑
     * ======================================================================
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        checkClosed();
        ensureResultSetIsEmpty();

        boolean gotoRead = SqlType.SELECT.equals(SqlTypeParser.getSqlType(sql)) && tGroupConnection.getAutoCommit();
        Connection conn = tGroupConnection.getBaseConnection(sql, gotoRead);

        if (conn != null) {
            sql = GroupHintParser.removeTddlGroupHint(sql);
            return executeQueryOnConnection(conn, sql);
        } else {
            // hint优先
            GroupIndex dataSourceIndex = GroupHintParser.convertHint2Index(sql);
            sql = GroupHintParser.removeTddlGroupHint(sql);
            if (dataSourceIndex == null) {
                dataSourceIndex = ThreadLocalDataSourceIndex.getIndex();
            }
            return this.tGroupDataSource.getDBSelector(gotoRead).tryExecute(executeQueryTryer,
                    retryingTimes,
                    sql,
                    dataSourceIndex);
        }
    }

    protected ResultSet executeQueryOnConnection(Connection conn, String sql) throws SQLException {
        Statement stmt = createStatementInternal(conn, sql, false);
        this.currentResultSet = stmt.executeQuery(sql);
        return this.currentResultSet;
    }

    public SQLWarning getWarnings() throws SQLException {
        checkClosed();
        if (baseStatement != null) return baseStatement.getWarnings();
        return null;
    }

    /*
     * ========================================================================
     * 以下为简单支持的方法
     * ======================================================================
     */

    public void clearWarnings() throws SQLException {
        checkClosed();
        if (baseStatement != null) baseStatement.clearWarnings();
    }

    public boolean getMoreResults() throws SQLException {
        return moreResults;
    }

    public int getQueryTimeout() throws SQLException {
        return queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) throws SQLException {
        this.queryTimeout = queryTimeout;
    }

    public ResultSet getResultSet() throws SQLException {
        return currentResultSet;
    }

    public int getUpdateCount() throws SQLException {
        return updateCount;
    }

    public int getResultSetConcurrency() throws SQLException {
        return resultSetConcurrency;
    }

    public void setResultSetConcurrency(int resultSetConcurrency) {
        this.resultSetConcurrency = resultSetConcurrency;
    }

    public int getResultSetHoldability() throws SQLException {
        return resultSetHoldability;
    }

    public void setResultSetHoldability(int resultSetHoldability) {
        this.resultSetHoldability = resultSetHoldability;
    }

    public int getResultSetType() throws SQLException {
        return resultSetType;
    }

    public void setResultSetType(int resultSetType) {
        this.resultSetType = resultSetType;
    }

    public Connection getConnection() throws SQLException {
        return tGroupConnection;
    }

    public void cancel() throws SQLException {
        // 调用底层进行关闭
        // see com.mysql.jdbc.StatementImpl
        this.baseStatement.cancel();
    }

    /*
     * ========================================================================
     * 以下为不支持的方法
     * ======================================================================
     */
    public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException("getFetchDirection");
    }

    public void setFetchDirection(int fetchDirection) throws SQLException {
        throw new UnsupportedOperationException("setFetchDirection");
    }

    public int getFetchSize() throws SQLException {
        return this.fetchSize;
    }

    public void setFetchSize(int fetchSize) throws SQLException {
        this.fetchSize = fetchSize;
    }

    public int getMaxFieldSize() throws SQLException {
        throw new UnsupportedOperationException("getMaxFieldSize");
    }

    public void setMaxFieldSize(int maxFieldSize) throws SQLException {
        throw new UnsupportedOperationException("setMaxFieldSize");
    }

    public int getMaxRows() throws SQLException {
        return this.maxRows;
    }

    public void setMaxRows(int maxRows) throws SQLException {
        this.maxRows = maxRows;
    }

    public void setCursorName(String cursorName) throws SQLException {
        throw new UnsupportedOperationException("setCursorName");
    }

    public void setEscapeProcessing(boolean escapeProcessing) throws SQLException {
        throw new UnsupportedOperationException("setEscapeProcessing");
    }

    public boolean getMoreResults(int current) throws SQLException {
        throw new UnsupportedOperationException("getMoreResults");
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        if (this.baseStatement != null) {
            return this.baseStatement.getGeneratedKeys();
        } else {
            throw new SQLException("在调用getGeneratedKeys前未执行过任何更新操作");
        }
        // throw new UnsupportedOperationException("getGeneratedKeys");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return this.getClass().isAssignableFrom(iface);
    }

    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            return (T) this;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public boolean isClosed() throws SQLException {
        throw new SQLException("not support exception");
    }

    public boolean isPoolable() throws SQLException {
        throw new SQLException("not support exception");
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        // TODO
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return true; // TODO
    }

    public void setPoolable(boolean poolable) throws SQLException {
        throw new SQLException("not support exception");
    }

    protected void fillSqlMetaData(Statement statement, String sql) {
        if (statement instanceof TStatement) fillSqlMetaData((TStatement) statement, sql);
    }

    protected void fillSqlMetaData(TStatement statement, String sql) {
        if (this.sqlMetaData == null) {
            this.sqlMetaData = SqlMetaDataFactory.getSqlMetaData(sql);
        }
        statement.fillMetaData(this.sqlMetaData);
    }

    @Override
    public void fillMetaData(SqlMetaData sqlMetaData) {
        this.sqlMetaData = sqlMetaData;
    }

    @Override
    public SqlMetaData getSqlMetaData() {
        return this.sqlMetaData;
    }
}
