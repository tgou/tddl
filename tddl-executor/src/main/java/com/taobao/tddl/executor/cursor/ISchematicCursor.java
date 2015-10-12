package com.taobao.tddl.executor.cursor;

import com.taobao.tddl.optimizer.core.expression.IOrderBy;

import java.util.List;

public interface ISchematicCursor extends Cursor {

    public List<IOrderBy> getOrderBy();

    /**
     * join查询可能存在多个可能的排序，比如sort merge join，会是左表或者右表的join列
     */
    public List<List<IOrderBy>> getJoinOrderBys();
}
