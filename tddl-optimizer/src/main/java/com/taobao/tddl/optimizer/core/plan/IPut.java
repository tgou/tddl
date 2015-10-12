package com.taobao.tddl.optimizer.core.plan;

import com.taobao.tddl.optimizer.core.expression.ISelectable;

import java.util.List;

public interface IPut<RT extends IPut> extends IDataNodeExecutor<RT> {

    /**
     * depend query command
     *
     * @return
     */
    IQueryTree getQueryTree();

    /**
     * @param queryCommon
     */
    RT setQueryTree(IQueryTree queryTree);

    List<ISelectable> getUpdateColumns();

    /**
     * set a = 1 ,b = 2 , c = 3 那么这个应该是 [‘a‘,‘b‘,‘c‘]
     *
     * @param columns
     */
    RT setUpdateColumns(List<ISelectable> columns);

    String getTableName();

    /**
     * IdxName
     *
     * @param indexName
     */
    RT setTableName(String indexName);

    String getIndexName();

    RT setIndexName(String indexName);

    List<Object> getUpdateValues();

    /**
     * set a = 1 ,b = 2 , c = 3 那么这个应该是 [1，2，3]
     *
     * @param columns
     */
    RT setUpdateValues(List<Object> values);

    PUT_TYPE getPutType();

    boolean isIgnore();

    RT setIgnore(boolean ignore);

    /**
     * 用于多值insert
     *
     * @return
     */
    public List<List<Object>> getMultiValues();

    public RT setMultiValues(List<List<Object>> multiValues);

    public boolean isMutiValues();

    public RT setMutiValues(boolean isMutiValues);

    public int getMuiltValuesSize();

    public List<Object> getValues(int index);

    public enum PUT_TYPE {
        REPLACE, INSERT, DELETE, UPDATE;
    }
}
