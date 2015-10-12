package com.taobao.tddl.executor.spi;

import com.taobao.tddl.common.exception.TddlException;
import com.taobao.tddl.common.model.ExtraCmd;
import com.taobao.tddl.common.model.Group;
import com.taobao.tddl.common.utils.GeneralUtil;
import com.taobao.tddl.executor.common.ExecutionContext;
import com.taobao.tddl.executor.common.ExecutorContext;
import com.taobao.tddl.executor.cursor.*;
import com.taobao.tddl.executor.cursor.impl.*;
import com.taobao.tddl.optimizer.core.expression.*;
import com.taobao.tddl.optimizer.core.expression.IFilter.OPERATION;
import com.taobao.tddl.optimizer.core.plan.IDataNodeExecutor;
import com.taobao.tddl.optimizer.core.plan.query.IJoin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 默认的cursor工厂
 *
 * @author mengshi.sunmengshi 2013-11-27 下午3:56:20
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
public class CursorFactoryDefaultImpl implements ICursorFactory {

    public CursorFactoryDefaultImpl() {
        super();
    }

    protected void closeParentCursor(Cursor parentCursor) {
        if (parentCursor != null) {
            List<TddlException> exs = new ArrayList();
            exs = parentCursor.close(exs);
            if (!exs.isEmpty()) throw new RuntimeException(GeneralUtil.mergeException(exs));

        }
    }

    @Override
    public IAggregateCursor aggregateCursor(ExecutionContext executionContext, ISchematicCursor cursor,
                                            List<IFunction> aggregates, List<IOrderBy> groupBycols,
                                            List<ISelectable> allSelectable, boolean isMerge) throws TddlException {
        try {
            return new AggregateCursor(cursor, aggregates, groupBycols, allSelectable, isMerge);
        } catch (Exception e) {
            closeParentCursor(cursor);
            throw new TddlException(e);
        }
    }

    @Override
    public IColumnAliasCursor columnAliasCursor(ExecutionContext executionContext, ISchematicCursor cursor,
                                                List<ISelectable> retColumns, String name) throws TddlException {
        try {
            return new ColumnAliasCursor(cursor, retColumns, name);
        } catch (Exception e) {
            closeParentCursor(cursor);
            throw new TddlException(e);
        }
    }

    @Override
    public ValueFilterCursor valueFilterCursor(ExecutionContext executionContext, ISchematicCursor cursor,
                                               IFilter filter) throws TddlException {
        try {
            return new ValueFilterCursor(cursor, filter, null);
        } catch (Exception e) {
            closeParentCursor(cursor);
            throw new TddlException(e);
        }
    }

    @Override
    public SortCursor mergeSortedCursor(ExecutionContext executionContext, List<ISchematicCursor> cursors,
                                        boolean duplicated) throws TddlException {
        try {
            return new MergeSortedCursors(cursors, duplicated);
        } catch (Exception e) {
            for (ISchematicCursor cursor : cursors) {
                closeParentCursor(cursor);
            }

            throw new TddlException(e);
        }
    }

    @Override
    public ITempTableSortCursor tempTableSortCursor(ExecutionContext executionContext, ISchematicCursor cursor,
                                                    List<IOrderBy> orderBys, boolean sortedDuplicates, long requestID)
            throws TddlException {
        try {
            if ("True".equalsIgnoreCase(GeneralUtil.getExtraCmdString(executionContext.getExtraCmds(),
                    ExtraCmd.ALLOW_TEMPORARY_TABLE))) {

                IRepository bdbRepo = ExecutorContext.getContext()
                        .getRepositoryHolder()
                        .getOrCreateRepository(Group.GroupType.BDB_JE.name(), Collections.EMPTY_MAP);
                return new TempTableSortCursor(this,
                        bdbRepo,
                        cursor,
                        orderBys,
                        sortedDuplicates,
                        requestID,
                        executionContext);
            }

            throw new IllegalStateException("not allow to use temporary table . allow first ");

        } catch (Exception e) {
            closeParentCursor(cursor);
            throw new TddlException(e);
        }
    }

    @Override
    public IReverseOrderCursor reverseOrderCursor(ExecutionContext executionContext, ISchematicCursor cursor)
            throws TddlException {
        try {
            return new ReverseOrderCursor(cursor);
        } catch (Exception e) {
            closeParentCursor(cursor);
            throw new TddlException(e);
        }
    }

    @Override
    public IMergeCursor mergeCursor(ExecutionContext executionContext, List<ISchematicCursor> cursors,
                                    IDataNodeExecutor currentExecotor) throws TddlException {
        try {
            return new MergeCursor(cursors, currentExecotor, executionContext);
        } catch (Exception e) {
            if (cursors != null) {
                for (ISchematicCursor iSchematicCursor : cursors) {
                    closeParentCursor(iSchematicCursor);
                }
            }
            throw new TddlException(e);
        }
    }

    @Override
    public IIndexNestLoopCursor indexNestLoopCursor(ExecutionContext executionContext, ISchematicCursor leftCursor,
                                                    ISchematicCursor rightCursor, List leftColumns, List rightColumns,
                                                    List columns, boolean prefix, IJoin executor) throws TddlException {
        try {
            return new IndexNestedLoopMgetImpCursor(leftCursor,
                    rightCursor,
                    leftColumns,
                    rightColumns,
                    columns,
                    prefix,
                    executor.getLeftNode().getColumns(),
                    executor.getRightNode().getColumns(),
                    executor);
        } catch (Exception e) {
            closeParentCursor(leftCursor);
            closeParentCursor(rightCursor);
            throw new TddlException(e);
        }
    }

    @Override
    public ILimitFromToCursor limitFromToCursor(ExecutionContext executionContext, ISchematicCursor cursor,
                                                Long limitFrom, Long limitTo) throws TddlException {
        try {
            return new LimitFromToCursor(cursor, limitFrom, limitTo);
        } catch (Exception e) {
            closeParentCursor(cursor);
            throw new TddlException(e);
        }
    }

    @Override
    public IMergeSortJoinCursor sortMergeJoinCursor(ExecutionContext executionContext, ISchematicCursor left_cursor,
                                                    ISchematicCursor right_cursor, List left_columns,
                                                    List right_columns, IJoin join) throws TddlException {
        try {

            return new SortMergeJoinCursor(left_cursor, right_cursor, left_columns, right_columns, join);
        } catch (Exception e) {
            closeParentCursor(left_cursor);
            closeParentCursor(right_cursor);
            throw new TddlException(e);
        }
    }

    @Override
    public AffectRowCursor affectRowCursor(ExecutionContext executionContext, int affectRow) {
        return new AffectRowCursor(affectRow);
    }

    @Override
    public ISchematicCursor schematicCursor(ExecutionContext executionContext, Cursor cursor, ICursorMeta meta,
                                            List<IOrderBy> orderBys) throws TddlException {
        try {
            return new SchematicCursor(cursor, meta, orderBys);
        } catch (Exception e) {
            closeParentCursor(cursor);
            throw new TddlException(e);
        }
    }

    @Override
    public IInCursor inCursor(ExecutionContext executionContext, Cursor cursor, List<IOrderBy> orderBys, IColumn c,
                              List<Object> v, OPERATION op) {
        return new InCursor(cursor, orderBys, c, v, op);
    }

    @Override
    public IMergeCursor mergeCursor(ExecutionContext executionContext, List<ISchematicCursor> cursors,
                                    ICursorMeta indexMeta, IDataNodeExecutor currentExecotor, List<IOrderBy> orderBys)
            throws TddlException {
        try {
            return new MergeCursor(cursors, indexMeta, currentExecotor, executionContext, orderBys);
        } catch (Exception e) {
            if (cursors != null) {
                for (ISchematicCursor iSchematicCursor : cursors) {
                    closeParentCursor(iSchematicCursor);
                }
            }
            throw new TddlException(e);
        }
    }

    @Override
    public IBlockNestedLoopCursor blockNestedLoopJoinCursor(ExecutionContext executionContext,
                                                            ISchematicCursor left_cursor,
                                                            ISchematicCursor right_cursor, List left_columns,
                                                            List right_columns, List columns, IJoin join)
            throws TddlException {

        try {
            return new BlockNestedtLoopCursor(left_cursor,
                    right_cursor,
                    left_columns,
                    right_columns,
                    columns,
                    this,
                    join,
                    executionContext,
                    join.getLeftNode().getColumns(),
                    join.getRightNode().getColumns());
        } catch (Exception e) {
            closeParentCursor(left_cursor);
            closeParentCursor(right_cursor);
            throw new TddlException(e);
        }
    }

    @Override
    public IRangeCursor rangeCursor(ExecutionContext executionContext, ISchematicCursor cursor, IFilter lf)
            throws TddlException {
        try {
            return new RangeCursor(cursor, lf);
        } catch (Exception e) {
            closeParentCursor(cursor);
            throw new TddlException(e);
        }
    }

    @Override
    public ISetOrderCursor setOrderCursor(ExecutionContext executionContext, ISchematicCursor cursor,
                                          List<IOrderBy> ordersInRequest) throws TddlException {
        try {
            return new SetOrderByCursor(cursor, null, ordersInRequest);
        } catch (Exception e) {
            closeParentCursor(cursor);
            throw new TddlException(e);
        }
    }

    @Override
    public ResultCursor resultCursor(ExecutionContext context, ISchematicCursor cursor, List<Object> retColumns)
            throws TddlException {
        return new ResultCursor(cursor, context, retColumns);
    }

}
