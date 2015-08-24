package com.github.mysite.common.vo;

import com.github.mysite.common.pageutil.PageInfo;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * description:基础类
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/19 - 15:17
 */
public class BaseModel implements Serializable{
    
    private int uuid;
    
    private PageInfo pageInfo = new PageInfo();

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public void setUuid(int uuid) {
        this.uuid = uuid;
    }

    public int getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("uuid", uuid)
                .add("pageInfo", pageInfo)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseModel baseModel = (BaseModel) o;
        return Objects.equal(uuid, baseModel.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
