package com.taobao.tddl.rule.groovy;

/**
 * @author <a href="junyu@taobao.com">junyu</a>
 * @version 1.0
 * @description
 * @date 2011-8-17 07:23:43
 * @since 1.6
 */
public class ProfileFactory {

    private static SimpleProfile profile = new SimpleProfile();

    public static SimpleProfile getProfile() {
        return profile;
    }
}
