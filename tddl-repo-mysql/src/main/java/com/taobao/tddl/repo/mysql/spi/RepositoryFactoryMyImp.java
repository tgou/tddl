package com.taobao.tddl.repo.mysql.spi;

import com.taobao.tddl.common.exception.TddlException;
import com.taobao.tddl.common.exception.TddlRuntimeException;
import com.taobao.tddl.common.utils.extension.Activate;
import com.taobao.tddl.executor.spi.IRepository;
import com.taobao.tddl.executor.spi.IRepositoryFactory;

import java.util.Map;

@Activate(name = "MYSQL_JDBC")
public class RepositoryFactoryMyImp implements IRepositoryFactory {

    @Override
    public IRepository buildRepository(Map<String, String> properties) {
        My_Repository myRepo = new My_Repository();
        try {
            myRepo.init();
        } catch (TddlException e) {
            throw new TddlRuntimeException(e);
        }
        return myRepo;
    }

}
