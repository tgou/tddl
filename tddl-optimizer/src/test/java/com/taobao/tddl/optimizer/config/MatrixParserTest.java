package com.taobao.tddl.optimizer.config;

import com.taobao.tddl.common.model.Matrix;
import com.taobao.tddl.optimizer.config.table.parse.MatrixParser;
import org.junit.Test;

public class MatrixParserTest {

    @Test
    public void testSimple() {
        Matrix matrix = MatrixParser.parse(Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("config/test_matrix.xml"));
        System.out.println(matrix);
    }
}
