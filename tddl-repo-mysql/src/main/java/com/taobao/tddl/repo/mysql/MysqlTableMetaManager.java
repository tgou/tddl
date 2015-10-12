package com.taobao.tddl.repo.mysql;

import com.taobao.tddl.common.exception.TddlRuntimeException;
import com.taobao.tddl.common.utils.XmlHelper;
import com.taobao.tddl.common.utils.extension.Activate;
import com.taobao.tddl.common.utils.logger.Logger;
import com.taobao.tddl.common.utils.logger.LoggerFactory;
import com.taobao.tddl.executor.spi.IDataSourceGetter;
import com.taobao.tddl.optimizer.config.table.RepoSchemaManager;
import com.taobao.tddl.optimizer.config.table.TableMeta;
import com.taobao.tddl.optimizer.config.table.parse.TableMetaParser;
import com.taobao.tddl.repo.mysql.spi.DatasourceMySQLImplement;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.util.List;

/**
 * @author mengshi.sunmengshi 2013-12-5 下午6:18:14
 * @since 5.0.0
 */
@Activate(name = "MYSQL_JDBC", order = 2)
public class MysqlTableMetaManager extends RepoSchemaManager {

    private final static Logger logger = LoggerFactory.getLogger(MysqlTableMetaManager.class);
    public static String xmlHead = "<tables xmlns=\"https://github.com/tddl/tddl/schema/table\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://github.com/tddl/tddl/schema/table https://raw.github.com/tddl/tddl/master/tddl-common/src/main/resources/META-INF/table.xsd\">";
    private final IDataSourceGetter mysqlDsGetter = new DatasourceMySQLImplement();

    public MysqlTableMetaManager() {
    }

    public static TableMeta resultSetMetaToSchema(ResultSetMetaData rsmd, DatabaseMetaData dbmd,
                                                  String logicalTableName, String actualTableName) {

        String xml = resultSetMetaToSchemaXml(rsmd, dbmd, logicalTableName, actualTableName);
        if (xml == null) {
            return null;
        }
        xml = xml.replaceFirst("<tables>", xmlHead);
        List<TableMeta> ts = null;
        ts = TableMetaParser.parse(xml);
        if (ts != null && !ts.isEmpty()) {
            return ts.get(0);
        }

        return null;
    }

    public static String resultSetMetaToSchemaXml(ResultSetMetaData rsmd, DatabaseMetaData dbmd,
                                                  String logicalTableName, String actualTableName) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            // build
            builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element tables = doc.createElement("tables");
            doc.appendChild(tables); // 将根元素添加到文档上
            Element table = doc.createElement("table");
            table.setAttribute("name", logicalTableName);
            tables.appendChild(table);
            Element columns = doc.createElement("columns");
            table.appendChild(columns);

            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                Element column = doc.createElement("column");
                column.setAttribute("name", rsmd.getColumnName(i));
                columns.appendChild(column);
                boolean isUnsigned = StringUtils.containsIgnoreCase(rsmd.getColumnTypeName(i), "unsigned");
                String type = TableMetaParser.jdbcTypeToDataTypeString(rsmd.getColumnType(i), isUnsigned);
                column.setAttribute("type", type);
            }

            try {
                ResultSet pkrs = dbmd.getPrimaryKeys(null, null, actualTableName);
                if (pkrs.next()) {
                    Element primaryKey = doc.createElement("primaryKey");
                    primaryKey.appendChild(doc.createTextNode(pkrs.getString("COLUMN_NAME")));
                    table.appendChild(primaryKey);
                } else {
                    Element primaryKey = doc.createElement("primaryKey");
                    primaryKey.appendChild(doc.createTextNode(rsmd.getColumnName(1)));
                    table.appendChild(primaryKey);
                }
            } catch (Exception ex) {
                // logger.warn("fetch pk error, choose the first column as pk, tablename is: "
                // + logicalTableName, ex);
                logger.warn("fetch pk error, choose the first column as pk, tablename is: " + logicalTableName);
                Element primaryKey = doc.createElement("primaryKey");
                primaryKey.appendChild(doc.createTextNode(rsmd.getColumnName(1)));
                table.appendChild(primaryKey);
            }

            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OutputStreamWriter outwriter = new OutputStreamWriter(baos);

                XmlHelper.callWriteXmlFile(doc, outwriter, "utf-8");
                outwriter.close();
                String content = baos.toString();
                return content;
            } catch (Exception e) {
                logger.error("fetch schema error", e);
            }

            return null;
        } catch (Exception ex) {
            logger.error("fetch schema error", ex);
            return null;
        }

    }

    protected IDataSourceGetter getDatasourceGetter() {
        return this.mysqlDsGetter;
    }

    /**
     * 需要各Repo来实现
     *
     * @param tableName
     */
    @Override
    protected TableMeta getTable0(String logicalTableName, String actualTableName) {
        TableMeta ts = fetchSchema(logicalTableName, actualTableName);
        return ts;
    }

    private TableMeta fetchSchema(String logicalTableName, String actualTableName) {
        if (actualTableName == null) {
            throw new TddlRuntimeException("table " + logicalTableName + " cannot fetched without a actual tableName");
        }

        DataSource ds = getDatasourceGetter().getDataSource(this.getGroup().getName());
        if (ds == null) {
            logger.error("schema of " + logicalTableName + " cannot be fetched, datasource is null, group name is "
                    + this.getGroup().getName());
            return null;
        }

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from " + actualTableName + " limit 1");
            ResultSetMetaData rsmd = rs.getMetaData();
            DatabaseMetaData dbmd = conn.getMetaData();
            return resultSetMetaToSchema(rsmd, dbmd, logicalTableName, actualTableName);
        } catch (Exception e) {
            if (e instanceof SQLException) {
                if ("42000".equals(((SQLException) e).getSQLState())) {
                    try {
                        rs = stmt.executeQuery("select * from " + actualTableName + " where rownum<=2");
                        ResultSetMetaData rsmd = rs.getMetaData();
                        DatabaseMetaData dbmd = conn.getMetaData();
                        return resultSetMetaToSchema(rsmd, dbmd, logicalTableName, actualTableName);
                    } catch (SQLException e1) {
                        logger.warn(e);
                    }
                }
            }
            e.printStackTrace();
            logger.error("schema of " + logicalTableName + " cannot be fetched", e);
            return null;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.warn(e);
            }
        }

    }
}
