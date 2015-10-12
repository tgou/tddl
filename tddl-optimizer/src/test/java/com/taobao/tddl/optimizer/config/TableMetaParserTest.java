package com.taobao.tddl.optimizer.config;

import com.taobao.tddl.optimizer.config.table.TableMeta;
import com.taobao.tddl.optimizer.config.table.parse.TableMetaParser;
import org.junit.Test;

import java.util.List;

public class TableMetaParserTest {

    @Test
    public void testSimple() {
        List<TableMeta> tables = TableMetaParser.parse(Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("config/test_table.xml"));
        System.out.println(tables);
    }
}
