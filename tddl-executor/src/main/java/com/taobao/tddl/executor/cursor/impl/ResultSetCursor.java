package com.taobao.tddl.executor.cursor.impl;

import com.taobao.tddl.executor.cursor.ISchematicCursor;
import com.taobao.tddl.executor.cursor.ResultCursor;

import java.sql.ResultSet;

public class ResultSetCursor extends ResultCursor {

    private ResultSet rs;

    public ResultSetCursor(ResultSet rs) {
        super((ISchematicCursor) null, null);
        this.rs = rs;
    }

    public ResultSet getResultSet() {
        return this.rs;
    }

}
