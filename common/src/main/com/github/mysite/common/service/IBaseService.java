package com.github.mysite.common.service;

import com.github.mysite.common.pageutil.PageInfo;
import com.github.mysite.common.vo.BaseModel;

/**
 * description:Service基础接口
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/20 - 10:49
 */
public interface IBaseService<M, QM extends BaseModel> {
    public void create(M m);

    public void update(M m);

    public void delete(String uuid);

    public M getByUuid(String uuid);

    public PageInfo<M> getByConditionPage(QM qm);
}
