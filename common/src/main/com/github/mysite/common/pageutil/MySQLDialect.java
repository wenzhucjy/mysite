package com.github.mysite.common.pageutil;

/**
 * description:MYSQL 数据库实现
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/18 - 14:45
 */
public class MySQLDialect extends Dialect {
    @Override
    public String getLimitString(String sql, int skipResults, int maxResults) {
        sql = sql.trim();
        StringBuffer pagingSelect = new StringBuffer(sql.length() + 50);
        pagingSelect.append(sql)
                    .append(" limit ")
                    .append(skipResults)
                    .append(" , ")
                    .append(maxResults);
        return pagingSelect.toString();
    }
}
