package com.taobao.tddl.rule.impl;

import com.taobao.tddl.rule.virtualnode.VirtualNodeMap;

import java.util.Map;

/**
 * @author <a href="junyu@taobao.com">junyu</a>
 * @version 1.0
 * @description
 * @date 2011-8-8 07:44:24
 * @since 1.6
 */
public class TableVirtualNodeRule extends VirtualNodeGroovyRule {

    public TableVirtualNodeRule(String expression, VirtualNodeMap vNodeMap) {
        super(expression, vNodeMap);
    }

    public TableVirtualNodeRule(String expression, VirtualNodeMap vNodeMap, String extraPackagesStr) {
        super(expression, vNodeMap, extraPackagesStr);
    }

    @Override
    public String eval(Map<String, Object> columnValues, Object outerContext) {
        String value = super.eval(columnValues, outerContext);
        return super.map(value);
    }
}
