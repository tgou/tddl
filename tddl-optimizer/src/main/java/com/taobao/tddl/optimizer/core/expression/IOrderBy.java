package com.taobao.tddl.optimizer.core.expression;

import com.taobao.tddl.common.jdbc.ParameterContext;
import com.taobao.tddl.optimizer.core.CanVisit;
import com.taobao.tddl.optimizer.core.datatype.DataType;

import java.util.Map;

/**
 * @since 5.0.0
 */
public interface IOrderBy extends CanVisit {

    public ISelectable getColumn();

    public IOrderBy setColumn(ISelectable columnName);

    public Boolean getDirection();

    public IOrderBy setDirection(Boolean direction);

    public IOrderBy assignment(Map<Integer, ParameterContext> parameterSettings);

    public String getAlias();

    public String getTableName();

    public IOrderBy setTableName(String alias);

    public String getColumnName();

    public IOrderBy setColumnName(String alias);

    public DataType getDataType();

    public String toStringWithInden(int inden);

    public IOrderBy copy();

}
