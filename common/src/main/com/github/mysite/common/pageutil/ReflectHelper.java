package com.github.mysite.common.pageutil;

import java.lang.reflect.Field;

/**
 * description:反射工具类
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/18 - 13:54
 */
public class ReflectHelper {


    /**
     * 获取obj对象fieldName的Field
     *
     * @param obj   对象
     * @param fieldName 字段名
     * @return  Field
     */
    public static Field getFieldByFieldName(Object obj, String fieldName) {
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                return superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            }
        }
        return null;
    }

    /**
     * 获取obj对象fieldName的属性值
     *
     * @param obj   对象
     * @param fieldName 字段名
     * @return Object
     * @throws Exception
     */
    public static Object getValueByFieldName(Object obj, String fieldName)
            throws Exception {
        Field field = getFieldByFieldName(obj, fieldName);
        Object value = null;
        if (field != null) {
            if (field.isAccessible()) {
                value = field.get(obj);
            } else {
                field.setAccessible(true);
                value = field.get(obj);
                field.setAccessible(false);
            }
        }
        return value;
    }

    /**
     * 设置obj对象fieldName的属性值
     *
     * @param obj
     * @param fieldName
     * @param value
     * @throws Exception
     */
    public static void setValueByFieldName(Object obj, String fieldName,
                                           Object value) throws Exception {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            if (field.isAccessible()) {
                field.set(obj, value);
            } else {
                field.setAccessible(true);
                field.set(obj, value);
                field.setAccessible(false);
            }
        } catch (Exception err) {
            //
        }
    }
}
