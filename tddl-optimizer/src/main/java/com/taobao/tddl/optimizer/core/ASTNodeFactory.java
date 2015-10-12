package com.taobao.tddl.optimizer.core;

import com.taobao.tddl.common.utils.extension.ExtensionLoader;
import com.taobao.tddl.optimizer.core.expression.*;
import com.taobao.tddl.optimizer.core.expression.bean.*;
import com.taobao.tddl.optimizer.core.plan.bean.*;
import com.taobao.tddl.optimizer.core.plan.dml.IDelete;
import com.taobao.tddl.optimizer.core.plan.dml.IInsert;
import com.taobao.tddl.optimizer.core.plan.dml.IReplace;
import com.taobao.tddl.optimizer.core.plan.dml.IUpdate;
import com.taobao.tddl.optimizer.core.plan.query.IJoin;
import com.taobao.tddl.optimizer.core.plan.query.IMerge;
import com.taobao.tddl.optimizer.core.plan.query.IQuery;

/**
 * Ast node构建工厂，简单的工具类没必要上接口了
 */
public class ASTNodeFactory {

    private static volatile ASTNodeFactory instance = null;

    public static ASTNodeFactory getInstance() {
        if (instance == null) {
            synchronized (ASTNodeFactory.class) {
                if (instance == null) { // double-check
                    try {
                        // 预留扩展，比如生成PB协议的node对象，未来比较长远
                        instance = ExtensionLoader.load(ASTNodeFactory.class);
                    } catch (Throwable e) {
                        instance = new ASTNodeFactory();
                    }
                }
            }
        }

        return instance;
    }

    public IQuery createQuery() {
        return new Query();
    }

    public IReplace createReplace() {
        return new Replace();
    }

    public IInsert createInsert() {
        return new Insert();
    }

    public IDelete createDelete() {
        return new Delete();
    }

    public IUpdate createUpdate() {
        return new Update();
    }

    public IColumn createColumn() {
        return new Column();
    }

    public IOrderBy createOrderBy() {
        return new OrderBy();
    }

    public IJoin createJoin() {
        return new Join();
    }

    public IMerge createMerge() {
        return new Merge();
    }

    public IBindVal createBindValue(int bind) {
        return new BindVal(bind);
    }

    public IBooleanFilter createBooleanFilter() {
        return new BooleanFilter();
    }

    public ILogicalFilter createLogicalFilter() {
        return new LogicalFilter();
    }

    public IFunction createFunction() {
        return new Function();
    }

    public Comparable createNullValue() {
        return NullValue.getNullValue();
    }
}
