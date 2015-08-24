package com.github.mysite.common.service;

import com.github.mysite.common.dao.BaseDAO;
import com.github.mysite.common.pageutil.PageInfo;
import com.github.mysite.common.vo.BaseModel;

import java.util.List;

/**
 * description:Service层基类
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/20 - 10:50
 */
public class BaseService<M, QM extends BaseModel> implements IBaseService<M, QM> {
    
    private BaseDAO dao = null;

    public void setDAO(BaseDAO dao) {
        this.dao = dao;
    }

    @Override
    public void create(M m) {
        dao.create(m);
    }

    @Override
    public void update(M m) {
        dao.update(m);
    }

    @Override
    public void delete(String uuid) {
        dao.delete(uuid);
    }

    @Override
    public M getByUuid(String uuid) {
        return (M) dao.getByUuid(uuid);
    }

    @Override
    public PageInfo<M> getByConditionPage(QM qm) {
        List<M> list = dao.getByConditionPage(qm);
        qm.getPageInfo().setResult(list);

        return qm.getPageInfo();
    }
}
