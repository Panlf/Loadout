package com.loadout.jdbc;

import com.loadout.usual.MapBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC 操作工具类
 * <p>
 * 能力：连接构建、DDL/DML 执行、count 统计、普通查询（Map / Bean 两种形态）、
 * 元信息查询（数据库、表、表注释、字段注释）、资源静默关闭。
 * <p>
 * 约定：
 * 1. 全部 SQL 参数走 PreparedStatement 占位符绑定，不拼接字符串，防注入；
 * 2. 执行失败统一抛 {@link JdbcException}，不再吞异常返回默认值，
 *    调用方需要区分业务失败还是执行失败时可以直接捕获；
 * 3. 查询返回的 Map 为 {@link LinkedHashMap}，保持结果集列顺序。
 * @author panlf
 * @date 2026/10/31
 */
@Slf4j
public final class JdbcUtils {

    private JdbcUtils() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    /**
     * 获取数据库连接
     *
     * @param jdbcType 数据库类型（决定驱动类）
     * @param url      JDBC 连接串
     * @param username 用户名
     * @param password 密码
     * @return Connection
     * @throws JdbcException 驱动加载失败或连接建立失败
     */
    public static Connection getConnection(JdbcType jdbcType, String url, String username, String password) {
        if (jdbcType == null || StringUtils.isAnyBlank(url, username, password)) {
            throw new JdbcException("获取连接的参数不能为空: jdbcType/url/username/password");
        }
        try {
            // 驱动符合 SPI 规范时 Class.forName 可省略，这里显式加载兼容老驱动
            Class.forName(jdbcType.getType());
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new JdbcException("驱动类不存在: " + jdbcType.getType() + "，请确认依赖已引入", e);
        } catch (SQLException e) {
            throw new JdbcException("获取连接失败: " + url, e);
        }
    }

    /**
     * 获取所有的数据库名称
     *
     * @param connection 数据库连接
     * @return 数据库名列表
     */
    public static List<String> getAllDataBase(Connection connection) {
        checkConnection(connection);
        List<String> result = new ArrayList<>();
        ResultSet catalogs = null;
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            catalogs = metaData.getCatalogs();
            while (catalogs.next()) {
                result.add(catalogs.getString("TABLE_CAT"));
            }
        } catch (SQLException e) {
            throw new JdbcException("获取数据库列表失败", e);
        } finally {
            close(catalogs);
        }
        return result;
    }

    /**
     * 获取指定 schema 下所有的表名
     *
     * @param connection 数据库连接
     * @param schemaName schema 名
     * @return 表名列表
     */
    public static List<String> getAllTable(Connection connection, String schemaName) {
        checkConnection(connection);
        if (StringUtils.isBlank(schemaName)) {
            throw new JdbcException("schemaName不能为空");
        }
        String sql = "SELECT TABLE_NAME FROM information_schema.tables WHERE TABLE_SCHEMA = ?";
        List<Map<String, Object>> rows = selectData(connection, sql, schemaName);
        List<String> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Object name = row.get("TABLE_NAME");
            if (name != null) {
                result.add(String.valueOf(name));
            }
        }
        return result;
    }

    /**
     * 获取所有的表名和表注释，结果保持表的元数据返回顺序
     *
     * @param connection 数据库连接
     * @param schema     schema 名，空则查所有 schema
     * @return tableName -> tableComment
     */
    public static Map<String, String> getAllTableAndComment(Connection connection, String schema) {
        checkConnection(connection);
        Map<String, String> result = new LinkedHashMap<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.tables";
            if (StringUtils.isNotEmpty(schema)) {
                sql += " WHERE TABLE_SCHEMA = ?";
            }
            ps = connection.prepareStatement(sql);
            if (StringUtils.isNotEmpty(schema)) {
                ps.setString(1, schema);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("TABLE_NAME"), rs.getString("TABLE_COMMENT"));
            }
        } catch (SQLException e) {
            throw new JdbcException("获取表注释失败, schema=" + schema, e);
        } finally {
            close(rs, ps);
        }
        return result;
    }

    /**
     * 获取表的字段名和字段注释
     *
     * @param connection 数据库连接
     * @param table      表名
     * @return columnName -> columnComment
     */
    public static Map<String, String> getAllColumnAndComment(Connection connection, String table) {
        checkConnection(connection);
        if (StringUtils.isBlank(table)) {
            throw new JdbcException("table不能为空");
        }
        Map<String, String> result = new LinkedHashMap<>();
        String sql = "SELECT COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE table_name = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, table);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("COLUMN_NAME"), rs.getString("COLUMN_COMMENT"));
            }
        } catch (SQLException e) {
            throw new JdbcException("获取字段注释失败, table=" + table, e);
        } finally {
            close(rs, ps);
        }
        return result;
    }

    /**
     * 根据 SQL 获取一个统计值（结果集第一行第一列）
     *
     * @param connection 数据库连接
     * @param sql        count 类 SQL，可含 ? 占位符
     * @param params     占位符参数
     * @return 长整型值
     */
    public static long getOneLongResult(Connection connection, String sql, Object... params) {
        List<Map<String, Object>> rows = selectData(connection, sql, params);
        if (CollectionUtils.isEmpty(rows)) {
            throw new JdbcException("统计SQL无结果, sql=" + sql);
        }
        Object value = rows.get(0).values().iterator().next();
        if (value == null) {
            throw new JdbcException("统计SQL结果为null, sql=" + sql);
        }
        return Long.parseLong(String.valueOf(value));
    }

    /**
     * 执行 DDL/DML 语句
     *
     * @param connection 数据库连接
     * @param sql        待执行语句
     */
    public static void execute(Connection connection, String sql) {
        checkConnection(connection);
        if (StringUtils.isBlank(sql)) {
            throw new JdbcException("sql不能为空");
        }
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.execute();
        } catch (SQLException e) {
            log.error("执行失败, sql={}", sql, e);
            throw new JdbcException("执行失败: " + e.getMessage() + ", sql=" + sql, e);
        } finally {
            close(ps);
        }
    }

    /**
     * 查询数据
     *
     * @param connection 数据库连接
     * @param sql        查询 SQL，可含 ? 占位符
     * @param params     占位符参数
     * @return 行列表，每行为 列名->值（LinkedHashMap，保持列顺序）
     */
    public static List<Map<String, Object>> selectData(Connection connection, String sql, Object... params) {
        checkConnection(connection);
        if (StringUtils.isBlank(sql)) {
            throw new JdbcException("sql不能为空");
        }
        List<Map<String, Object>> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            bindParams(ps, params);
            rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                list.add(row);
            }
        } catch (SQLException e) {
            log.error("查询失败, sql={}", sql, e);
            throw new JdbcException("查询失败: " + e.getMessage() + ", sql=" + sql, e);
        } finally {
            close(rs, ps);
        }
        return list;
    }

    /**
     * 查询单行数据，无结果返回 null
     *
     * @param connection 数据库连接
     * @param sql        查询 SQL，可含 ? 占位符
     * @param params     占位符参数
     * @return 首行数据；无结果返回 null
     */
    public static Map<String, Object> selectOneData(Connection connection, String sql, Object... params) {
        List<Map<String, Object>> list = selectData(connection, sql, params);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询数据并转 Bean
     * <p>
     * 列名支持下划线命名，自动映射到 Bean 的驼峰属性（user_name -> userName）
     *
     * @param connection 数据库连接
     * @param sql        查询 SQL，可含 ? 占位符
     * @param clazz      目标 Bean 类型
     * @param params     占位符参数
     * @param <T>        目标类型
     * @return Bean 列表
     */
    public static <T> List<T> selectDataAsBean(Connection connection, String sql, Class<T> clazz, Object... params) {
        return MapBeanUtils.mapListToBeanList(selectData(connection, sql, params), clazz);
    }

    /**
     * 查询单行数据并转 Bean，无结果返回 null
     *
     * @param connection 数据库连接
     * @param sql        查询 SQL，可含 ? 占位符
     * @param clazz      目标 Bean 类型
     * @param params     占位符参数
     * @param <T>        目标类型
     * @return Bean 实例；无结果返回 null
     */
    public static <T> T selectOneDataAsBean(Connection connection, String sql, Class<T> clazz, Object... params) {
        return MapBeanUtils.mapToBean(selectOneData(connection, sql, params), clazz);
    }

    /**
     * Map 转 Bean（委托 {@link MapBeanUtils#mapToBean}）
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz) {
        return MapBeanUtils.mapToBean(map, clazz);
    }

    /**
     * List&lt;Map&gt; 转 List&lt;Bean&gt;（委托 {@link MapBeanUtils#mapListToBeanList}）
     */
    public static <T> List<T> mapListToBeanList(List<Map<String, Object>> mapList, Class<T> clazz) {
        return MapBeanUtils.mapListToBeanList(mapList, clazz);
    }

    /**
     * 绑定占位符参数
     */
    private static void bindParams(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null || params.length == 0) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private static void checkConnection(Connection connection) {
        if (connection == null) {
            throw new JdbcException("connection不能为空");
        }
    }

    /**
     * 静默关闭资源，关闭失败仅记录日志不影响主流程
     */
    public static void close(AutoCloseable... closeables) {
        if (closeables == null || closeables.length == 0) {
            return;
        }
        Arrays.stream(closeables).forEach(closeable -> {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    log.warn("资源关闭失败: {}", closeable.getClass().getSimpleName(), e);
                }
            }
        });
    }

    /**
     * JDBC 操作异常
     */
    public static class JdbcException extends RuntimeException {
        public JdbcException(String message) {
            super(message);
        }

        public JdbcException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
