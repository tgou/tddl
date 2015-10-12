package com.taobao.tddl.atom;

import com.taobao.diamond.mockserver.MockServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

@Ignore
public class BaseAtomTest {

    @BeforeClass
    public static void beforeClass() {
        MockServer.setUpMockServer();
    }

    @AfterClass
    public static void after() {
        MockServer.tearDownMockServer();
    }
}
