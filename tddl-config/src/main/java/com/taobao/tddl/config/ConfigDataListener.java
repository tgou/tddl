package com.taobao.tddl.config;

/**
 * 接收信息的回调接口
 *
 * @author shenxun
 * @author <a href="zylicfc@gmail.com">junyu</a>
 * @version 1.0
 * @date 2011-1-11上午11:22:29
 * @since 1.6
 */
public interface ConfigDataListener {

    /**
     * 配置中心客户端收到数据时调用注册的监听器方法， 并把收到的数据传递到此方法中
     *
     * @param dataId 数据在配置中心注册的id
     * @param data   字符串数据
     */
    void onDataReceived(String dataId, String data);
}
