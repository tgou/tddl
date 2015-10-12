package com.taobao.tddl.repo.mysql.spi;

import com.taobao.tddl.executor.cursor.ICursorMeta;
import com.taobao.tddl.executor.rowset.IRowSet;
import com.taobao.tddl.executor.rowset.ResultSetRowSet;

import java.sql.ResultSet;

public class My_Convertor {

    public static IRowSet convert(ResultSet rs, ICursorMeta meta) {
        IRowSet rowSet = new ResultSetRowSet(meta, rs);
        return rowSet;
    }
}
