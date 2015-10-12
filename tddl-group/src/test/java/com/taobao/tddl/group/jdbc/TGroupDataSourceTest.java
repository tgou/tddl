package com.taobao.tddl.group.jdbc;

import org.junit.Test;

import java.io.PrintWriter;
import java.sql.Connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author yangzhu
 */
public class TGroupDataSourceTest {

    @Test
    public void javax_sql_DataSource_api_support() throws Exception {
        TGroupDataSource ds = new TGroupDataSource();
        assertEquals(ds.getLoginTimeout(), 0);
        assertEquals(ds.getLogWriter(), null);
        PrintWriter writer = new PrintWriter(System.out);
        ds.setLoginTimeout(100);
        ds.setLogWriter(writer);
        assertEquals(ds.getLoginTimeout(), 100);
        assertEquals(ds.getLogWriter(), writer);

        Connection conn = ds.getConnection();
        assertTrue((conn instanceof TGroupConnection));

        conn = ds.getConnection("username", "password");
        assertTrue((conn instanceof TGroupConnection));
    }
}
