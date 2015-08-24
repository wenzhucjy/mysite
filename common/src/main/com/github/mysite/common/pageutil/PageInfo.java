package com.github.mysite.common.pageutil;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.List;

/**
 * description: PAGE 分页对象
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/18 - 11:34
 */
public class PageInfo<E> implements Serializable{

    private int pageSize = 15;      //每页显示的记录数，默认是15
    private int totalPage;          //总页码
    private int totalCount;         //总记录条数
    private int start;              //起始的记录数
    private int currentPage;        //当前页码，从1开始
    private List<E> result;         //对应的当前页记录

    public int getStart() {
        start = (getCurrentPage() - 1) * getPageSize();
        if (start < 0) {
            start = 0;
        }
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<E> getResult() {
        return result;
    }

    public void setResult(List<E> result) {
        this.result = result;
    }

    public void setTotalPage(int totalPage) {
        
        this.totalPage = totalPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getCurrentPage() {
        return currentPage > getTotalPage() ? getTotalPage() : currentPage <= 0 ? 1 : currentPage;
    }

    public int getTotalPage() {
        return (int) Math.ceil(getTotalCount() * 1.0 / getPageSize());
    }

    /**
     * 无参数构造方法
     */
    public PageInfo() {
        
    }

    /**
     * 带参数构造方法
     * @param currentPage   当前页码
     * @param pageSize      每页显示条数
     */
    public PageInfo(int currentPage,int pageSize) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("pageSize", pageSize)
                .add("totalPage", totalPage)
                .add("totalCount", totalCount)
                .add("start", start)
                .add("currentPage", currentPage)
                .add("result", result)
                .toString();
    }
}
