package com.github.mysite.common.pageutil;

/**
 * description:
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/18 - 14:41
 */
public class OracleDialect extends Dialect {

    @Override
    public String getLimitString(String sql, int offset, int limit) {
        sql = sql.trim();
        StringBuffer pagingSelect = new StringBuffer(sql.length() + 100);

        pagingSelect.append("SELECT * FROM ( SELECT row_.*, ROWNUM rownum_ FROM ( ")
                    .append(sql)
                    .append(" ) AS row_ ) WHERE rownum_ > ")
                    .append(offset).append(" AND rownum_ <= ")
                    .append(offset + limit);
        return pagingSelect.toString();
    }
}
