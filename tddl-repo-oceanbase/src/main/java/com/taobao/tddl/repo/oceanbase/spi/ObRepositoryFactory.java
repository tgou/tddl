package com.taobao.tddl.repo.oceanbase.spi;

import com.taobao.tddl.common.exception.TddlException;
import com.taobao.tddl.common.exception.TddlRuntimeException;
import com.taobao.tddl.common.utils.extension.Activate;
import com.taobao.tddl.executor.spi.IRepository;
import com.taobao.tddl.executor.spi.IRepositoryFactory;

import java.util.Map;

@Activate(name = "OCEANBASE_JDBC")
public class ObRepositoryFactory implements IRepositoryFactory {

    @Override
    public IRepository buildRepository(Map<String, String> properties) {
        Ob_Repository myRepo = new Ob_Repository();
        try {
            myRepo.init();
        } catch (TddlException e) {
            throw new TddlRuntimeException(e);
        }
        return myRepo;
    }

}
