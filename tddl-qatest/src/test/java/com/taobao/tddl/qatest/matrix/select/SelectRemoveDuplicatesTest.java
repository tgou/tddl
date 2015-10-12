package com.taobao.tddl.qatest.matrix.select;

import com.taobao.tddl.qatest.BaseMatrixTestCase;
import com.taobao.tddl.qatest.BaseTestCase;
import com.taobao.tddl.qatest.ExecuteTableName;
import com.taobao.tddl.qatest.util.EclipseParameterized;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * or去重的测试
 * <p/>
 * Author By: zhuoxue.yll Created Date: 2012-10-19 上午10:54:19
 */
@RunWith(EclipseParameterized.class)
public class SelectRemoveDuplicatesTest extends BaseMatrixTestCase {

    public SelectRemoveDuplicatesTest(String normaltblTableName) {
        BaseTestCase.normaltblTableName = normaltblTableName;
    }

    @Parameters(name = "{index}:table1={0}")
    public static List<String[]> prepare() {
        return Arrays.asList(ExecuteTableName.normaltblTable(dbType));
    }

    @Before
    public void prepareData() throws Exception {
        normaltblPrepare(0, MAX_DATA_SIZE);
    }

    @Test
    public void conditionWithLessAndGreatTest() throws Exception {
        long start = 5;
        int end = 300;

        String sql = "select * from " + normaltblTableName + " where pk >? or id> ?";
        List<Object> param = new ArrayList<Object>();
        param.add(start);
        param.add(end);
        String[] columnParam = {"PK", "NAME", "ID", "GMT_CREATE", "GMT_DATETIME", "FLOATCOL"};
        selectContentSameAssert(sql, columnParam, param);
    }

}
