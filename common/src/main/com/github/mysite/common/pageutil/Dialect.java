package com.github.mysite.common.pageutil;

/**
 * description:
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

    public abstract String getLimitString(String sql, int skipResults, int maxResults);

}
