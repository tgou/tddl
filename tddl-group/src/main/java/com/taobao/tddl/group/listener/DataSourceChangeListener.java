package com.taobao.tddl.group.listener;

import javax.sql.DataSource;
import java.util.Map;

public interface DataSourceChangeListener {

    public void onDataSourceChanged(Map<String, DataSource> dataSources);
}
