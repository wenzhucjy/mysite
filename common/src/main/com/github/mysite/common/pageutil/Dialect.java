package com.github.mysite.common.pageutil;

/**
 * description: 数据库类型
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/18 - 14:40
 */
public abstract class Dialect {
    public static enum Type {
        MYSQL,
        ORACLE;
    }

    /**
     * 拼装成最后的分页语句
     * @param sql   SQL
     * @param skipResults   检索开始条数
     * @param maxResults    检索结束条数
     * @return  拼装的最后分页语句
     */
    public abstract String getLimitString(String sql, int skipResults, int maxResults);

}
