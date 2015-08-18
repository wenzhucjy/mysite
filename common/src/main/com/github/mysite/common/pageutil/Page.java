package com.github.mysite.common.pageutil;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/18 - 11:34
 */
public class Page<E> extends ArrayList<E> {


    private int pageSize = 15;      //每页显示的记录数，默认是15
    private int totalPage;          //总页码
    private int totalCount;         //总记录条数
    private int start;              //起始的记录数
    private int pageNum;            //页码，从1开始
    private List<E> result;         //对应的当前页记录

    public int getStart() {
        start = (getPageNum() - 1) * getPageSize();
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

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageNum() {
        if (pageNum <= 0)
            pageNum = 1;
        if (pageNum > getTotalPage())
            pageNum = getTotalPage();
        return pageNum;
    }

    public int getTotalPage() {
        return (int) Math.ceil(totalCount * 1.0 / pageSize);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("pageSize", pageSize)
                .add("totalPage", totalPage)
                .add("totalCount", totalCount)
                .add("start", start)
                .add("pageNum", pageNum)
                .add("result", result)
                .toString();
    }
}
