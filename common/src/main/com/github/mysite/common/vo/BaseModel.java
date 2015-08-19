package com.github.mysite.common.vo;

import com.github.mysite.common.pageutil.Page;
import com.google.common.base.Objects;

/**
 * description:
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/19 - 15:17
 */
public class BaseModel {
    
    private Integer uuid;
    private Page page = new Page();

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Integer getUuid() {
        return uuid;
    }

    public void setUuid(Integer uuid) {
        this.uuid = uuid;
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
