package com.taobao.tddl.rule.groovy;

/**
 * @author <a href="junyu@taobao.com">junyu</a>
 * @version 1.0
 * @description
 * @date 2011-8-17 07:29:21
 * @since 1.6
 */
public class AppUtils {

    public static Long idFormat(Object value) {
        Long lva = Long.valueOf(String.valueOf(value));
        return lva * 10;
    }
}
