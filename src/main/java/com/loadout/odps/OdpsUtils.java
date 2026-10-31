package com.loadout.odps;

import com.alibaba.fastjson2.JSON;
import com.aliyun.odps.Column;
import com.aliyun.odps.Instance;
import com.aliyun.odps.Odps;
import com.aliyun.odps.OdpsException;
import com.aliyun.odps.Partition;
import com.aliyun.odps.PartitionSpec;
import com.aliyun.odps.Table;
import com.aliyun.odps.TableSchema;
import com.aliyun.odps.Tables;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.data.Record;
import com.aliyun.odps.data.RecordReader;
import com.aliyun.odps.task.SQLTask;
import com.aliyun.odps.tunnel.InstanceTunnel;
import com.aliyun.odps.tunnel.TunnelException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadout.odps.dto.ColumnBaseInfo;
import com.loadout.odps.dto.TableGrantInfo;
import com.loadout.odps.dto.TableInfo;
import com.loadout.odps.dto.TaskResult;
import com.loadout.usual.MapBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ODPS(MaxCompute) 工具类
 * <p>
 * 能力：连接构建、SQL执行（同步/异步/任务结果）、数据查询（普通 / Tunnel大结果集，均支持 Map / Bean 两种形态）、
 * 表元信息、字段信息、文件量统计、对外授权信息、表/视图DDL生成
 * @author panlf
 * @date 2026/10/31
 */
@Slf4j
public final class OdpsUtils {

    /**
     * Jackson线程安全，全局复用，避免每次调用重复创建
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private OdpsUtils() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    /**
     * 返回Odps连接
     *
     * @param accessId  AccessKey ID
     * @param accessKey AccessKey Secret
     * @param endPoint  ODPS Endpoint
     * @param project   默认Project
     * @return Odps连接
     */
    public static Odps getOdps(String accessId, String accessKey, String endPoint, String project) {
        if (StringUtils.isAnyBlank(accessId, accessKey, endPoint, project)) {
            throw new IllegalArgumentException("构建Odps连接的参数不能为空");
        }
        Account account = new AliyunAccount(accessId, accessKey);
        Odps odps = new Odps(account);
        odps.setEndpoint(endPoint);
        odps.setDefaultProject(project);
        return odps;
    }

    /**
     * 执行SQL并等待完成，返回各Task的原始结果
     * <p>
     * 注意：结果集走 instance 结果通道，查询结果超过1万条会被截断，
     * 大结果集请使用 {@link #selectTunnel(Odps, String)}
     *
     * @param odps ODPS连接
     * @param sql  SQL语句
     * @return taskName -> 原始结果
     * @throws OdpsException SQL提交或执行失败
     */
    public static Map<String, String> runSQL(Odps odps, String sql) throws OdpsException {
        checkSqlArgs(odps, sql);
        Instance i = SQLTask.run(odps, sql);
        i.waitForSuccess();
        return i.getTaskResults();
    }

    /**
     * 异步执行SQL语句，立即返回instanceId，不等待执行完成
     *
     * @param odps ODPS连接
     * @param sql  SQL语句
     * @return instanceId，配合 {@link #getTaskResult(Odps, String)} 轮询结果
     * @throws OdpsException SQL提交失败
     */
    public static String runSQLAsync(Odps odps, String sql) throws OdpsException {
        checkSqlArgs(odps, sql);
        Instance i = SQLTask.run(odps, sql);
        return i.getId();
    }

    /**
     * 根据jobId获取任务结果（无论成功失败都返回TaskResult，不抛异常）
     *
     * @param odps  ODPS连接
     * @param jobId instanceId
     * @return 任务结果，失败时result为异常描述
     */
    public static TaskResult getTaskResult(Odps odps, String jobId) {
        TaskResult taskResult = new TaskResult();
        try {
            Instance i = odps.instances().get(jobId);
            taskResult.setResult(JSON.toJSONString(i.getTaskResults()));
            taskResult.setEndTime(i.getEndTime() == null ? new Date() : i.getEndTime());
        } catch (OdpsException e) {
            log.error("获取任务结果失败, jobId={}", jobId, e);
            taskResult.setResult("异常结果码：" + e.getErrorCode() + "异常结果信息：" + e.getMessage());
            taskResult.setEndTime(new Date());
        }
        return taskResult;
    }

    /**
     * 执行count类SQL并返回第一行第一列的长整型值
     * <p>
     * 注意：执行失败或结果为空时返回0（与历史行为保持一致），无法区分"0条"和"失败"，
     * 需要区分场景请改用 {@link #select(Odps, String)} 自行解析
     *
     * @param odps ODPS连接
     * @param sql  count类SQL
     * @return 数量；失败返回0
     */
    public static long countTable(Odps odps, String sql) {
        long count = 0;
        try {
            log.debug("countTable sql ===> {}", sql);
            Instance i = SQLTask.run(odps, sql);
            i.waitForSuccess();
            List<Record> records = SQLTask.getResult(i);
            if (CollectionUtils.isEmpty(records)) {
                log.warn("countTable结果为空, sql={}", sql);
                return count;
            }
            Object value = records.get(0).get(0);
            count = value == null ? 0 : Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            log.error("countTable执行失败, sql={}", sql, e);
            return count;
        }
        return count;
    }

    /**
     * 查询数据，每行为 列名->值（LinkedHashMap，保持列顺序）
     * <p>
     * 注意：底层走 SQLTask.getResult，受 ODPS 1万条上限约束，超过会被截断；
     * 大结果集请使用 {@link #selectTunnel(Odps, String)}
     *
     * @param odps ODPS连接
     * @param sql  查询SQL
     * @return 行列表
     * @throws OdpsException SQL提交或执行失败
     */
    public static List<Map<String, Object>> select(Odps odps, String sql) throws OdpsException {
        return selectRows(runSelectInstance(odps, sql));
    }

    /**
     * 查询数据并转 Bean
     * <p>
     * 列名支持下划线命名，自动映射到 Bean 的驼峰属性（user_name -> userName）
     *
     * @param odps  ODPS连接
     * @param sql   查询SQL
     * @param clazz 目标 Bean 类型
     * @param <T>   目标类型
     * @return Bean 列表
     * @throws OdpsException SQL提交或执行失败
     */
    public static <T> List<T> select(Odps odps, String sql, Class<T> clazz) throws OdpsException {
        return MapBeanUtils.mapListToBeanList(select(odps, sql), clazz);
    }

    /**
     * 查询单条数据，无结果返回null
     *
     * @param odps ODPS连接
     * @param sql  查询SQL
     * @return 首行数据；无结果返回null
     * @throws OdpsException SQL提交或执行失败
     */
    public static Map<String, Object> selectOne(Odps odps, String sql) throws OdpsException {
        List<Map<String, Object>> list = select(odps, sql);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    /**
     * 查询单条数据并转 Bean，无结果返回null
     *
     * @param odps  ODPS连接
     * @param sql   查询SQL
     * @param clazz 目标 Bean 类型
     * @param <T>   目标类型
     * @return Bean 实例；无结果返回null
     * @throws OdpsException SQL提交或执行失败
     */
    public static <T> T selectOne(Odps odps, String sql, Class<T> clazz) throws OdpsException {
        return MapBeanUtils.mapToBean(selectOne(odps, sql), clazz);
    }

    /**
     * Tunnel方式查询数据，绕过ODPS SQL结果1万条的返回上限
     * <p>
     * 原理：SQL执行完成后，通过 InstanceTunnel 直接下载实例结果集，
     * 不经过 SQLTask.getResult 的1万条截断。
     * <p>
     * 注意：
     * 1. sql必须是SELECT类查询，DDL/DML类实例没有可下载的结果集；
     * 2. 结果全部加载进内存，数据量极大时请使用 {@link #selectTunnel(Odps, String, long)} 限制行数，防止OOM；
     * 3. 需要SDK版本支持 InstanceTunnel（odps-sdk-core 0.24+）
     *
     * @param odps ODPS连接
     * @param sql  查询SQL（SELECT）
     * @return 行列表，每行为 列名->值（LinkedHashMap，保持列顺序）
     * @throws OdpsException   SQL提交或执行失败
     * @throws TunnelException Tunnel下载会话创建失败（含实例无结果集的情况）
     * @throws IOException     数据读取失败
     */
    public static List<Map<String, Object>> selectTunnel(Odps odps, String sql) throws OdpsException, TunnelException, IOException {
        return selectTunnel(odps, sql, -1L);
    }

    /**
     * Tunnel方式查询数据并转 Bean
     *
     * @param odps  ODPS连接
     * @param sql   查询SQL（SELECT）
     * @param clazz 目标 Bean 类型
     * @param <T>   目标类型
     * @return Bean 列表
     * @throws OdpsException   SQL提交或执行失败
     * @throws TunnelException Tunnel下载会话创建失败
     * @throws IOException     数据读取失败
     */
    public static <T> List<T> selectTunnel(Odps odps, String sql, Class<T> clazz) throws OdpsException, TunnelException, IOException {
        return MapBeanUtils.mapListToBeanList(selectTunnel(odps, sql), clazz);
    }

    /**
     * Tunnel方式查询数据（带最大行数保护）
     *
     * @param odps    ODPS连接
     * @param sql     查询SQL（SELECT）
     * @param maxRows 最大返回行数，&lt;=0 表示不限制
     * @return 行列表，每行为 列名->值（LinkedHashMap，保持列顺序）
     * @throws OdpsException   SQL提交或执行失败
     * @throws TunnelException Tunnel下载会话创建失败
     * @throws IOException     数据读取失败
     */
    public static List<Map<String, Object>> selectTunnel(Odps odps, String sql, long maxRows) throws OdpsException, TunnelException, IOException {
        checkSqlArgs(odps, sql);
        log.debug("selectTunnel sql ==> {}", sql);

        Instance instance = SQLTask.run(odps, sql);
        instance.waitForSuccess();

        InstanceTunnel tunnel = new InstanceTunnel(odps);
        InstanceTunnel.DownloadSession session = tunnel.createDownloadSession(odps.getDefaultProject(), instance.getId());
        long recordCount = session.getRecordCount();
        log.debug("selectTunnel instanceId={}, recordCount={}", instance.getId(), recordCount);

        List<Map<String, Object>> rows = new ArrayList<>();
        try (RecordReader reader = session.openRecordReader(0, recordCount)) {
            Record record;
            while ((record = reader.read()) != null) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (Column col : record.getColumns()) {
                    row.put(col.getName(), record.get(col.getName()));
                }
                rows.add(row);
                if (maxRows > 0 && rows.size() >= maxRows) {
                    log.debug("selectTunnel 达到maxRows={}, 提前停止读取", maxRows);
                    break;
                }
            }
        }
        return rows;
    }

    /**
     * Tunnel方式查询数据并转 Bean（带最大行数保护）
     *
     * @param odps    ODPS连接
     * @param sql     查询SQL（SELECT）
     * @param maxRows 最大返回行数，&lt;=0 表示不限制
     * @param clazz   目标 Bean 类型
     * @param <T>     目标类型
     * @return Bean 列表
     * @throws OdpsException   SQL提交或执行失败
     * @throws TunnelException Tunnel下载会话创建失败
     * @throws IOException     数据读取失败
     */
    public static <T> List<T> selectTunnel(Odps odps, String sql, long maxRows, Class<T> clazz) throws OdpsException, TunnelException, IOException {
        return MapBeanUtils.mapListToBeanList(selectTunnel(odps, sql, maxRows), clazz);
    }

    /**
     * 获取表信息（遍历project下所有表，tableName非空时仅保留同名表）
     *
     * @param project   project名（仅作回填，可为null）
     * @param tableName 表名过滤条件，空则返回全部表
     * @param odps      ODPS连接
     * @return 表信息列表
     */
    public static List<TableInfo> listTableAllInfo(String project, String tableName, Odps odps) {
        List<TableInfo> list = new ArrayList<>();
        //获取该空间下所有的表
        Tables tables = odps.tables();

        for (Table table : tables) {
            if (StringUtils.isNotEmpty(tableName) && !table.getName().equals(tableName)) {
                continue;
            }
            TableInfo tableInfo = new TableInfo();
            //是否是视图
            if (table.isVirtualView()) {
                tableInfo.setType(1);
            } else {
                tableInfo.setType(0);
            }
            tableInfo.setProject(project);
            tableInfo.setTableName(table.getName());
            tableInfo.setTableComment(table.getComment());
            tableInfo.setColumnBaseInfoList(findAllColumnInfo(table));
            list.add(tableInfo);
        }
        return list;
    }

    private static List<ColumnBaseInfo> findAllColumnInfo(Table table) {
        List<ColumnBaseInfo> list = new ArrayList<>();

        TableSchema tableSchema = table.getSchema();
        List<Column> columnList = tableSchema.getColumns();

        for (Column column : columnList) {
            ColumnBaseInfo columnBaseInfo = new ColumnBaseInfo();
            columnBaseInfo.setColumnName(column.getName());
            columnBaseInfo.setComment(column.getComment());
            columnBaseInfo.setType(column.getTypeInfo().getTypeName());
            list.add(columnBaseInfo);
        }
        return list;
    }

    /**
     * 获取单表的字段信息
     *
     * @param odps      ODPS连接
     * @param tableName 表名
     * @return 字段信息列表；表不存在返回null
     */
    public static List<ColumnBaseInfo> getOneTable(Odps odps, String tableName) {
        List<TableInfo> list = listTableAllInfo(null, tableName, odps);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0).getColumnBaseInfoList();
        }
        return null;
    }

    /**
     * 获取表（或分区）的文件数与存储字节数
     *
     * @param odps          ODPS连接
     * @param tableName     表名
     * @param partitionSpec 分区规格，非分区表传null
     * @return [文件数, 字节数]
     * @throws OdpsException 元信息获取失败或分区不存在
     */
    public static long[] getTableFileAggInfo(Odps odps, String tableName, String partitionSpec) throws OdpsException {
        if (StringUtils.isBlank(tableName)) {
            throw new IllegalArgumentException("tableName不能为空");
        }
        Table table = odps.tables().get(tableName);
        if (!table.isPartitioned()) {
            return new long[]{table.getFileNum(), table.getSize()};
        }
        PartitionSpec spec = new PartitionSpec(partitionSpec);
        Partition partition = table.getPartition(spec);
        return new long[]{partition.getFileNum(), partition.getSize()};
    }

    /**
     * 获取表的对外授权信息
     * <p>
     * show grants on table 返回的 result JSON 结构：
     * {"12":{"rec":{
     *   "1":{map:["str","lst",count,{Implicit条目}]},  // Implicit 内置
     *   "2":{map:["str","lst",count,{ACL条目}]},         // ACL 对外授权
     *   "3":{map:["str","lst",count,{Policy条目}]},      // Policy 内置角色
     *   "4":{map:["str","lst",count,{Package条目}]}      // Package
     * }}}
     * <p>
     * ACL 条目格式：{"user/RAM$xxx:xxx_sjc": ["str", 1, "Select"]}
     * Policy 条目格式：{"role/role_project_admin": ["rec", 1, {"1":{"str":"Allow"},"3":{"lst":["str",1,"odps:*"]}}]}
     *
     * @param odps      ODPS连接
     * @param tableName 表名
     * @return 授权信息列表；无对外授权时返回一条 hasExternalGrants=false 的记录；执行失败返回空列表
     */
    public static List<TableGrantInfo> getTableGrants(Odps odps, String tableName) {
        List<TableGrantInfo> grants = new ArrayList<>();
        try {
            Map<String, String> result = runSQL(odps, "show grants on table " + tableName + ";");

            for (String taskResult : result.values()) {
                JsonNode taskNode = OBJECT_MAPPER.readTree(taskResult);
                if (!"0".equals(taskNode.path("errorCode").asText())) {
                    continue;
                }

                String resultStr = taskNode.path("result").asText();
                if (resultStr.isEmpty()) {
                    continue;
                }

                JsonNode resultNode = OBJECT_MAPPER.readTree(resultStr);
                JsonNode recNode = resultNode.path("12").path("rec");

                // section "2" = ACL
                JsonNode aclMap = recNode.path("2").path("map");
                if (!aclMap.isArray() || aclMap.size() < 4) {
                    continue;
                }

                int aclCount = aclMap.get(2).asInt();
                if (aclCount == 0) {
                    // 无对外授权
                    grants.add(new TableGrantInfo(tableName, false, null, null, null, null));
                    continue;
                }

                // index 3 是授权条目对象
                JsonNode aclEntries = aclMap.get(3);
                Iterator<String> fieldNames = aclEntries.fieldNames();
                while (fieldNames.hasNext()) {
                    String principal = fieldNames.next();

                    // 解析 principal: "user/RAM$xxx:xxx_sjc" → type=USER, name=xxx_sjc
                    String principalType = "UNKNOWN";
                    String principalName = principal;

                    int slashIdx = principal.indexOf('/');
                    if (slashIdx > 0) {
                        principalType = principal.substring(0, slashIdx).toUpperCase();
                        String afterSlash = principal.substring(slashIdx + 1);
                        int colonIdx = afterSlash.lastIndexOf(':');
                        if (colonIdx >= 0 && colonIdx < afterSlash.length() - 1) {
                            principalName = afterSlash.substring(colonIdx + 1);
                        } else {
                            principalName = afterSlash;
                        }
                    }

                    // 解析权限值
                    String privilege = parsePrivilege(aclEntries.get(principal));

                    grants.add(new TableGrantInfo(tableName, true, principal,
                            principalType, principalName, privilege));
                }
            }
        } catch (Exception e) {
            log.error("获取表[{}]授权信息失败", tableName, e);
        }
        return grants;
    }

    /**
     * 从授权条目中解析权限值
     * <p>
     * ACL 格式:   ["str", 1, "Select"]              → "Select"
     * Policy 格式: ["rec", 1, {"1":{"str":"Allow"},"3":{"lst":["str",1,"odps:Read"]}}]
     *              → "Allow(odps:Read)"
     */
    private static String parsePrivilege(JsonNode entryNode) {
        if (entryNode == null || !entryNode.isArray() || entryNode.size() < 3) {
            return "Unknown";
        }

        String type = entryNode.get(0).asText();

        if ("str".equals(type)) {
            return entryNode.get(2).asText();
        }

        if ("rec".equals(type)) {
            JsonNode recObj = entryNode.get(2);
            String effect = recObj.path("1").path("str").asText("Allow");
            JsonNode actionsNode = recObj.path("3").path("lst");
            StringBuilder sb = new StringBuilder(effect);
            if (actionsNode.isArray() && actionsNode.size() > 2) {
                sb.append("(");
                for (int i = 2; i < actionsNode.size(); i++) {
                    if (i > 2) {
                        sb.append(",");
                    }
                    sb.append(actionsNode.get(i).asText());
                }
                sb.append(")");
            }
            return sb.toString();
        }

        return "Unknown";
    }

    /**
     * 获取表或视图的DDL语句
     * <p>
     * 表：拼装 CREATE TABLE DDL（含字段、字段注释、分区、表注释、生命周期）
     * 视图：返回 CREATE OR REPLACE VIEW IF NOT EXISTS ... AS <视图定义SQL>（含字段注释、视图注释）
     *
     * @param odps      ODPS连接
     * @param tableName 表名或视图名
     * @return DDL语句；表不存在或获取失败时返回null
     */
    public static String getTableDDL(Odps odps, String tableName) {
        try {
            checkSqlArgs(odps, tableName);
            Table table = odps.tables().get(tableName);

            // 视图：直接取视图定义SQL，字段注释、视图注释一并带上
            if (table.isVirtualView()) {
                String viewText = StringUtils.trimToEmpty(table.getViewText());
                if (viewText.endsWith(";")) {
                    viewText = viewText.substring(0, viewText.length() - 1);
                }

                StringBuilder viewDdl = new StringBuilder();
                viewDdl.append("CREATE OR REPLACE VIEW IF NOT EXISTS ").append(tableName);

                // 字段清单（视图字段类型由查询推导，这里只带字段名和注释）
                List<Column> viewColumns = table.getSchema().getColumns();
                if (CollectionUtils.isNotEmpty(viewColumns)) {
                    List<String> viewColDefs = new ArrayList<>();
                    for (Column column : viewColumns) {
                        String colDef = "\t`" + column.getName() + "`";
                        if (StringUtils.isNotEmpty(column.getComment())) {
                            colDef += "   COMMENT '" + escapeSingleQuote(column.getComment()) + "'";
                        }
                        viewColDefs.add(colDef);
                    }
                    viewDdl.append(" (\n").append(String.join(",\n", viewColDefs)).append("\n)");
                }

                // 视图注释（与 as 同行）
                if (StringUtils.isNotEmpty(table.getComment())) {
                    viewDdl.append(" comment '").append(escapeSingleQuote(table.getComment())).append("'");
                }

                viewDdl.append(" as\n").append(viewText).append(";");
                return viewDdl.toString();
            }

            // 表：拼装 CREATE TABLE
            TableSchema schema = table.getSchema();
            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (\n");

            List<String> columnDefs = new ArrayList<>();
            for (Column column : schema.getColumns()) {
                columnDefs.add(buildColumnDef(column));
            }
            ddl.append(String.join(",\n", columnDefs));

            // 分区字段
            List<Column> partitionColumns = schema.getPartitionColumns();
            if (CollectionUtils.isNotEmpty(partitionColumns)) {
                ddl.append("\n)\nPARTITIONED BY (\n");
                List<String> partitionDefs = new ArrayList<>();
                for (Column column : partitionColumns) {
                    partitionDefs.add(buildColumnDef(column));
                }
                ddl.append(String.join(",\n", partitionDefs));
            }
            ddl.append("\n)");

            // 表注释
            if (StringUtils.isNotEmpty(table.getComment())) {
                ddl.append("\nCOMMENT '").append(escapeSingleQuote(table.getComment())).append("'");
            }

            // 生命周期（-1 表示永久，不输出）
            Long lifecycle = table.getLife();
            if (lifecycle > 0) {
                ddl.append("\nLIFECYCLE ").append(lifecycle);
            }

            ddl.append(";");
            return ddl.toString();
        } catch (Exception e) {
            log.error("获取表[{}]DDL失败", tableName, e);
            return null;
        }
    }

    /**
     * 提交SELECT类SQL并等待完成，返回Instance
     */
    private static Instance runSelectInstance(Odps odps, String sql) throws OdpsException {
        checkSqlArgs(odps, sql);
        log.debug("select sql ==> {}", sql);
        Instance i = SQLTask.run(odps, ensureSemicolon(sql));
        i.waitForSuccess();
        return i;
    }

    /**
     * 将Instance结果集转为 列名->值 的行列表
     */
    private static List<Map<String, Object>> selectRows(Instance instance) throws OdpsException {
        List<Record> records = SQLTask.getResult(instance);
        List<Map<String, Object>> rows = new ArrayList<>(records.size());
        for (Record r : records) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (Column col : r.getColumns()) {
                row.put(col.getName(), r.get(col.getName()));
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * 拼装单个字段定义（含注释）
     */
    private static String buildColumnDef(Column column) {
        StringBuilder def = new StringBuilder();
        def.append("  ").append(column.getName()).append(" ").append(column.getTypeInfo().getTypeName());
        if (StringUtils.isNotEmpty(column.getComment())) {
            def.append(" COMMENT '").append(escapeSingleQuote(column.getComment())).append("'");
        }
        return def.toString();
    }

    /**
     * 转义字符串中的单引号，用于SQL字面量
     */
    private static String escapeSingleQuote(String value) {
        return value.replace("'", "\\'");
    }

    /**
     * 参数校验：odps与SQL均不能为空
     */
    private static void checkSqlArgs(Odps odps, String sql) {
        if (odps == null || StringUtils.isBlank(sql)) {
            throw new IllegalArgumentException("odps和sql参数不能为空");
        }
    }

    /**
     * SQL末尾补分号（已带则原样返回）
     */
    private static String ensureSemicolon(String sql) {
        return sql.endsWith(";") ? sql : sql + ";";
    }
}
