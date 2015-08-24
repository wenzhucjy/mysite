package com.github.mysite.common.pageutil;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * description:通过拦截<code>StatementHandler</code>的<code>prepare</code>方法，重写sql语句实现物理分页
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/18 - 13:57
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }) })
public class PagePlugin implements Interceptor {

	public static final Logger	LOGGER				= LoggerFactory.getLogger(PagePlugin.class);

	private static String		defaultDialect		= "mysql";										// 数据库类型(默认为mysql)
	private static String		defaultPageSqlId	= ".*Page$";									// 需要拦截的ID(正则匹配)

	private static String		dialectType			= "";											// 数据库方言
	private static String		pageSqlId			= "";											// mapper.xml中需要拦截的ID(正则匹配)

    @Override
	public Object intercept(Invocation ivk) throws Throwable {

		if (ivk.getTarget() instanceof RoutingStatementHandler) {
			RoutingStatementHandler statementHandler = (RoutingStatementHandler) ivk.getTarget();
			BaseStatementHandler delegate = (BaseStatementHandler) ReflectHelper.getValueByFieldName(statementHandler,
					"delegate");
			MappedStatement mappedStatement = (MappedStatement) ReflectHelper.getValueByFieldName(delegate,
					"mappedStatement");
			// 只重写需要分页的sql语句。通过 MappedStatement
			// 的ID匹配，默认重写以Page结尾的MappedStatement的sql
			if (mappedStatement.getId().matches(pageSqlId)) {
				BoundSql boundSql = delegate.getBoundSql();
				Object parameterObject = boundSql.getParameterObject();
				if (parameterObject == null) {
                    LOGGER.error("parameterObject error");
				} else {
					Connection connection = (Connection) ivk.getArgs()[0];
					// 重设分页参数里的总页数等
					setPageParameter(mappedStatement, boundSql, parameterObject, connection);
				}
			}
		}
		// 将执行权交给下一个拦截器
		return ivk.proceed();
	}

	private void setPageParameter(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject,Connection connection) {
		String sql = boundSql.getSql();
		// 记录总记录数
		String countSql = "select count(0) from (" + sql + ") myCount";
		LOGGER.info("总数sql 语句:{}", countSql);
		PreparedStatement countStmt = null;
		ResultSet rs = null;
        try {
            countStmt = connection.prepareStatement(countSql);
            BoundSql countBS = new BoundSql(mappedStatement.getConfiguration(), countSql,
                    boundSql.getParameterMappings(), parameterObject);
            setParameters(countStmt, mappedStatement, countBS, parameterObject);
            rs = countStmt.executeQuery();

            int totalCount = 0;
            if (rs.next()) {
                totalCount = rs.getInt(1);
            }
            PageInfo pageInfo = null;
            if (parameterObject instanceof PageInfo) {
                pageInfo = (PageInfo) parameterObject;
            } else if (parameterObject instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) parameterObject;
                pageInfo = (PageInfo) map.get("pageInfo");
                if (pageInfo == null) {
                    pageInfo = new PageInfo();
                }
            } else {
                Field pageField = ReflectHelper.getFieldByFieldName(parameterObject, "pageInfo");
                if (pageField != null) {
                    pageInfo = (PageInfo) ReflectHelper.getValueByFieldName(parameterObject, "pageInfo");
                    if (pageInfo == null) {
                        pageInfo = new PageInfo();
                    }
                    ReflectHelper.setValueByFieldName(parameterObject, "pageInfo", pageInfo);
                } else {
                    throw new NoSuchFieldException(parameterObject.getClass().getName());
                }
            }
            //总记录条数
            pageInfo.setTotalCount(totalCount);
            //根据数据库类型，生成特定的分页sql
            String pageSql = generatePageSql(sql, pageInfo);
            LOGGER.info("pageInfo sql:{}", pageSql);
            ReflectHelper.setValueByFieldName(boundSql, "sql", pageSql);
        } catch (Exception ex) {
            LOGGER.error("Ignore this exception", ex);
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

	private void setParameters(PreparedStatement ps, MappedStatement mappedStatement, BoundSql boundSql,
			Object parameterObject) throws SQLException {
		ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		if (parameterMappings != null) {
			Configuration configuration = mappedStatement.getConfiguration();
			TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
			MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
			for (int i = 0; i < parameterMappings.size(); i++) {
				ParameterMapping parameterMapping = parameterMappings.get(i);
				if (parameterMapping.getMode() != ParameterMode.OUT) {
					Object value;
					String propertyName = parameterMapping.getProperty();
					PropertyTokenizer prop = new PropertyTokenizer(propertyName);
					if (parameterObject == null) {
						value = null;
					} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
						value = parameterObject;
					} else if (boundSql.hasAdditionalParameter(propertyName)) {
						value = boundSql.getAdditionalParameter(propertyName);
					} else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX)
							&& boundSql.hasAdditionalParameter(prop.getName())) {
						value = boundSql.getAdditionalParameter(prop.getName());
						if (value != null) {
							value = configuration.newMetaObject(value).getValue(
									propertyName.substring(prop.getName().length()));
						}
					} else {
						value = metaObject == null ? null : metaObject.getValue(propertyName);
					}
					TypeHandler typeHandler = parameterMapping.getTypeHandler();
					if (typeHandler == null) {
						throw new ExecutorException("There was no TypeHandler found for parameter " + propertyName
								+ " of statement " + mappedStatement.getId());
					}
					typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());
				}
			}
		}
	}

	/**
	 * 根据数据库类型，生成特定的分页sql
	 *
	 * @param sql
	 *            SQL
	 * @param pageInfo
	 *            Page分页对象
	 * @return 拼装出分页语句
	 */
	private String generatePageSql(String sql, PageInfo pageInfo) {
		Dialect dialect = null;
		if (pageInfo != null) {
			switch (Dialect.Type.valueOf(dialectType.toUpperCase())) {
				case ORACLE:
					dialect = new OracleDialect();
					break;
				case MYSQL:
					dialect = new MySQLDialect();
					break;
			}
			return dialect.getLimitString(sql, pageInfo.getStart(), pageInfo.getPageSize());
		}
		return sql;

	}

	@Override
	public Object plugin(Object target) {
		// 当目标类是StatementHandler类型时，才包装目标类，否者直接返回目标本身,减少目标被代理的次数
		if (target instanceof StatementHandler) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}

	@Override
	public void setProperties(Properties p) {
		dialectType = p.getProperty("dialect");
		if (StringUtils.isBlank(dialectType)) {
			LOGGER.debug("Property dialect is not setted,use default 'mysql'");
			dialectType = defaultDialect;
		}
		pageSqlId = p.getProperty("pageSqlId");
		if (StringUtils.isBlank(pageSqlId)) {
			LOGGER.debug("Property pageSqlId is not setted,use default '.*PageInfo$' ");
			pageSqlId = defaultPageSqlId;
		}
	}
}
