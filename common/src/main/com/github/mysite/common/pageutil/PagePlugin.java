package com.github.mysite.common.pageutil;

import com.google.common.base.Strings;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * description:通过拦截<code>StatementHandler</code>的<code>prepare</code>方法，重写sql语句实现物理分页
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/18 - 13:57
 */

@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class})})
public class PagePlugin implements Interceptor {

    public static final Logger LOGGER = LoggerFactory.getLogger(PagePlugin.class);

    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();

    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static String defaultDialect = "mysql"; // 数据库类型(默认为mysql)
    private static String defaultPageSqlId = ".*Page$"; // 需要拦截的ID(正则匹配)

    private static String dialect = "";    //数据库方言
    private static String pageSqlId = ""; //mapper.xml中需要拦截的ID(正则匹配)


    public Object intercept(Invocation invocation) throws Throwable {

        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY,
                DEFAULT_OBJECT_WRAPPER_FACTORY);
        // 分离代理对象链(由于目标类可能被多个拦截器拦截，从而形成多次代理，通过下面的两次循环可以分离出最原始的的目标类)
        while (metaStatementHandler.hasGetter("h")) {
            Object object = metaStatementHandler.getValue("h");
            metaStatementHandler = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
        }
        // 分离最后一个代理对象的目标类
        while (metaStatementHandler.hasGetter("target")) {
            Object object = metaStatementHandler.getValue("target");
            metaStatementHandler = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
        }
        Configuration configuration = (Configuration) metaStatementHandler.getValue("delegate.configuration");
        dialect = configuration.getVariables().getProperty("dialect");
        if (Strings.isNullOrEmpty(dialect)) {
            LOGGER.debug("Property dialect is not setted,use default 'mysql'");
            dialect = defaultDialect;
        }
        pageSqlId = configuration.getVariables().getProperty("pageSqlId");
        if (Strings.isNullOrEmpty(pageSqlId)) {
            LOGGER.debug("Property pageSqlId is not setted,use default '.*Page$' ");
            pageSqlId = defaultPageSqlId;
        }
        MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");
        // 只重写需要分页的sql语句。通过MappedStatement的ID匹配，默认重写以Page结尾的MappedStatement的sql
        if (mappedStatement.getId().matches(pageSqlId)) {
            BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
            Object parameterObject = boundSql.getParameterObject();
            if (parameterObject == null) {
                throw new NullPointerException("parameterObject is null!");
            } else {
                Page page = (Page) metaStatementHandler
                        .getValue("delegate.boundSql.parameterObject.page");
                String sql = boundSql.getSql();
                // 重写sql
                String pageSql = buildPageSql(dialect, sql, page);
                metaStatementHandler.setValue("delegate.boundSql.sql", pageSql);
                // 采用物理分页后，就不需要mybatis的内存分页了，所以重置下面的两个参数
                metaStatementHandler.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
                metaStatementHandler.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);
                Connection connection = (Connection) invocation.getArgs()[0];
                // 重设分页参数里的总页数等
                setPageParameter(sql, connection, mappedStatement, boundSql, page);
            }
        }
        // 将执行权交给下一个拦截器
        return invocation.proceed();
    }

    /**
     * 根据数据库类型，生成特定的分页sql
     *
     * @param databaseType 数据库类型,默认MYSQL
     * @param sql          SQL
     * @param page         Page分页对象
     * @return 拼装出分页语句
     */
    private String buildPageSql(String databaseType, String sql, Page page) {
        Dialect dialect = null;
        switch (Dialect.Type.valueOf(databaseType.toUpperCase())) {
            case ORACLE:
                dialect = new OracleDialect();
                break;
            case MYSQL:
                dialect = new MySQLDialect();
                break;
        }
        return dialect.getLimitString(sql, page.getStart(), page.getPageSize());
    }


    /**
     * 从数据库里查询总的记录数并计算总页数，回写进分页参数<code>PageParameter</code>,这样调用者就可用通过 分页参数
     * <code>PageParameter</code>获得相关信息。
     *
     * @param sql             SQL
     * @param connection      Connection
     * @param mappedStatement MappedStatement
     * @param boundSql        BoundSql
     * @param page            Page
     */
    private void setPageParameter(String sql, Connection connection, MappedStatement mappedStatement,
                                  BoundSql boundSql, Page page) {
        // 记录总记录数
        String countSql = " SELECT COUNT(0) FROM (" + sql + ") AS total ";
        PreparedStatement countStmt = null;
        ResultSet rs = null;
        try {
            countStmt = connection.prepareStatement(countSql);
            BoundSql countBS = new BoundSql(mappedStatement.getConfiguration(), countSql,
                    boundSql.getParameterMappings(), boundSql.getParameterObject());
            setParameters(countStmt, mappedStatement, countBS, boundSql.getParameterObject());
            rs = countStmt.executeQuery();
            int totalCount = 0;
            if (rs.next()) {
                totalCount = rs.getInt(1);
            }
            page.setTotalCount(totalCount);
            int totalPage = totalCount / page.getPageSize() + ((totalCount % page.getPageSize() == 0) ? 0 : 1);
            page.setTotalPage(totalPage);

        } catch (SQLException e) {
            LOGGER.error("Ignore this exception", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.error("Ignore this exception", e);
            }
            try {
                countStmt.close();
            } catch (SQLException e) {
                LOGGER.error("Ignore this exception", e);
            }
        }

    }

    /**
     * 对SQL参数(?)设值
     *
     * @param ps                    PreparedStatement
     * @param mappedStatement       MappedStatement
     * @param boundSql              BoundSql
     * @param parameterObject       Object
     * @throws SQLException
     */
    private void setParameters(PreparedStatement ps, MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) throws
            SQLException {
        ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
        parameterHandler.setParameters(ps);
    }


    @Override
    public Object plugin(Object arg0) {
        return Plugin.wrap(arg0, this);
    }

    @Override
    public void setProperties(Properties p) {
        dialect = p.getProperty("dialect");
//		if (dialect!=null && dialect.trim().length()>0) {
//			System.out.println("dialect property is not found!");
//		}
        pageSqlId = p.getProperty("pageSqlId");
//		if (pageSqlId!=null && pageSqlId.trim().length()>0) {
//				System.out.println("pageSqlId property is not found!");
//		}
    }

//    public Object intercept_bak(Invocation ivk) throws Throwable {
//        if (ivk.getTarget() instanceof RoutingStatementHandler) {
//            RoutingStatementHandler statementHandler = (RoutingStatementHandler) ivk.getTarget();
//            BaseStatementHandler delegate = (BaseStatementHandler) ReflectHelper.getValueByFieldName(statementHandler, "delegate");
//            MappedStatement mappedStatement = (MappedStatement) ReflectHelper.getValueByFieldName(delegate, "mappedStatement");
//
//            // 只重写需要分页的sql语句。通过MappedStatement的ID匹配，默认重写以Page结尾的
//            if (mappedStatement.getId().matches(pageSqlId)) { //拦截需要分页的SQL
//                BoundSql boundSql = delegate.getBoundSql();
//                Object parameterObject = boundSql.getParameterObject();//分页SQL<select>中parameterType属性对应的实体参数，即Mapper接口中执行分页方法的参数,该参数不得为空
//                if (parameterObject == null) {
//                    throw new NullPointerException("parameterObject尚未实例化！");
//                } else {
//                    Connection connection = (Connection) ivk.getArgs()[0];
//                    String sql = boundSql.getSql();
//                    String countSql = "select count(0) from (" + sql + ") as tmp_count"; //记录统计
//                    PreparedStatement countStmt = connection.prepareStatement(countSql);
//                    BoundSql countBS = new BoundSql(mappedStatement.getConfiguration(), countSql, boundSql.getParameterMappings(),
//                            parameterObject);
//                    setParameters(countStmt, mappedStatement, countBS, parameterObject);
//                    ResultSet rs = countStmt.executeQuery();
//                    int count = 0;
//                    if (rs.next()) {
//                        count = rs.getInt(1);
//                    }
//                    rs.close();
//                    countStmt.close();
//
//                    Page page = null;
//                    if (parameterObject instanceof Page) { // 参数就是Page实体
//                        page = (Page) parameterObject;
//                        page.setTotalCount(count);
//                    } else { // 参数为某个实体，该实体拥有Page属性
//                        Field pageField = ReflectHelper.getFieldByFieldName(parameterObject, "page");
//                        if (pageField != null) {
//                            page = (Page) ReflectHelper.getValueByFieldName(parameterObject, "page");
//                            if (page == null)
//                                page = new Page();
//                            page.setTotalCount(count);
//                            ReflectHelper.setValueByFieldName(parameterObject, "page", page); // 通过反射，对实体对象设置分页对象
//                        } else {
//                            throw new NoSuchFieldException(parameterObject.getClass().getName() + "不存在 page 属性！");
//                        }
//                    }
//                    String pageSql = generatePageSql(sql, page);
//                    ReflectHelper.setValueByFieldName(boundSql, "sql", pageSql); // 将分页sql语句反射回BoundSql.
//                }
//            }
//        }
//        // 将执行权交给下一个拦截器
//        return ivk.proceed();
//    }
//
//    /**
//     * 对SQL参数(?)设值,参考org.apache.ibatis.executor.parameter.DefaultParameterHandler
//     *
//     * @param ps
//     * @param mappedStatement
//     * @param boundSql
//     * @param parameterObject
//     * @throws SQLException
//     */
//    private void setParameters(PreparedStatement ps, MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) throws
//            SQLException {
//        ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
//        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
//        if (parameterMappings != null) {
//            Configuration configuration = mappedStatement.getConfiguration();
//            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
//            MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
//            for (int i = 0; i < parameterMappings.size(); i++) {
//                ParameterMapping parameterMapping = parameterMappings.get(i);
//                if (parameterMapping.getMode() != ParameterMode.OUT) {
//                    Object value;
//                    String propertyName = parameterMapping.getProperty();
//                    PropertyTokenizer prop = new PropertyTokenizer(propertyName);
//                    if (parameterObject == null) {
//                        value = null;
//                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
//                        value = parameterObject;
//                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
//                        value = boundSql.getAdditionalParameter(propertyName);
//                    } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql.hasAdditionalParameter(prop.getName())) {
//                        value = boundSql.getAdditionalParameter(prop.getName());
//                        if (value != null) {
//                            value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
//                        }
//                    } else {
//                        value = metaObject == null ? null : metaObject.getValue(propertyName);
//                    }
//                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
//                    if (typeHandler == null) {
//                        throw new ExecutorException("There was no TypeHandler found for parameter " + propertyName + " of statement " +
//                                mappedStatement.getId());
//                    }
//                    typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());
//                }
//            }
//        }
//    }
//
//    /**
//     * 根据数据库方言，生成特定的分页sql
//     *
//     * @param sql
//     * @param page
//     * @return
//     */
//    private String generatePageSql(String sql, Page page) {
//        if (page != null && dialect != null && dialect.trim().length() > 0) {
//            StringBuffer pageSql = new StringBuffer();
//            if ("mysql".equals(dialect)) {
//                pageSql.append(sql);
//                pageSql.append(" limit " + page.getStart() + "," + page.getPageSize());
//            } else if ("oracle".equals(dialect)) {
//                pageSql.append("select * from (select tmp_tb.*,ROWNUM row_id from (");
//                pageSql.append(sql);
//                pageSql.append(") as tmp_tb where ROWNUM<=");
//                pageSql.append(page.getStart() + page.getPageSize());
//                pageSql.append(") where row_id>");
//                pageSql.append(page.getStart());
//            }
//            return pageSql.toString();
//        } else {
//            return sql;
//        }
//    }

}