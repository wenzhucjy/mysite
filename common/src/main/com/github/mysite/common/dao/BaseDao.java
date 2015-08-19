package com.github.mysite.common.dao;

import java.util.List;

/**
 * description:
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/19 - 15:15
 */
public interface BaseDao<M,QM> {
    
    public void create(M m);

    public void update(M m);

    public void delete(int uuid);

    public M getByUuid(int uuid);

    public List<M> getByConditionPage(QM qm);
}
