package com.taobao.tddl.repo.mysql.spi;

import com.taobao.tddl.common.exception.TddlException;
import com.taobao.tddl.executor.common.ExecutionContext;
import com.taobao.tddl.executor.cursor.Cursor;
import com.taobao.tddl.executor.cursor.ICursorMeta;
import com.taobao.tddl.executor.cursor.ISchematicCursor;
import com.taobao.tddl.executor.spi.CursorFactoryDefaultImpl;
import com.taobao.tddl.optimizer.core.expression.IOrderBy;
import com.taobao.tddl.repo.mysql.cursor.SchematicMyCursor;

import java.util.List;

public class CursorFactoryMyImpl extends CursorFactoryDefaultImpl {

    public CursorFactoryMyImpl() {
        super();
    }

    @Override
    public ISchematicCursor schematicCursor(ExecutionContext executionContext, Cursor cursor, ICursorMeta meta,
                                            List<IOrderBy> orderBys) throws TddlException {
        try {
            return new SchematicMyCursor(cursor, meta, orderBys);
        } catch (Exception e) {
            closeParentCursor(cursor);
            throw new TddlException(e);
        }
    }

}
