// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.db.jdbc;

import org.apache.logging.log4j.core.layout.PatternLayout;
import java.util.ArrayList;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import java.sql.Statement;
import java.util.Iterator;
import java.io.Closeable;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Timestamp;
import org.apache.logging.log4j.core.LogEvent;
import java.sql.SQLException;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import java.sql.DatabaseMetaData;
import org.apache.logging.log4j.core.util.Closer;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.util.List;
import org.apache.logging.log4j.core.appender.db.AbstractDatabaseManager;

public final class JdbcDatabaseManager extends AbstractDatabaseManager
{
    private static final JdbcDatabaseManagerFactory INSTANCE;
    private final List<Column> columns;
    private final ConnectionSource connectionSource;
    private final String sqlStatement;
    private Connection connection;
    private PreparedStatement statement;
    private boolean isBatchSupported;
    
    private JdbcDatabaseManager(final String name, final int bufferSize, final ConnectionSource connectionSource, final String sqlStatement, final List<Column> columns) {
        super(name, bufferSize);
        this.connectionSource = connectionSource;
        this.sqlStatement = sqlStatement;
        this.columns = columns;
    }
    
    @Override
    protected void startupInternal() throws Exception {
        this.connection = this.connectionSource.getConnection();
        final DatabaseMetaData metaData = this.connection.getMetaData();
        this.isBatchSupported = metaData.supportsBatchUpdates();
        Closer.closeSilently(this.connection);
    }
    
    @Override
    protected void shutdownInternal() {
        if (this.connection != null || this.statement != null) {
            this.commitAndClose();
        }
    }
    
    @Override
    protected void connectAndStart() {
        try {
            (this.connection = this.connectionSource.getConnection()).setAutoCommit(false);
            this.statement = this.connection.prepareStatement(this.sqlStatement);
        }
        catch (SQLException e) {
            throw new AppenderLoggingException("Cannot write logging event or flush buffer; JDBC manager cannot connect to the database.", e);
        }
    }
    
    @Override
    protected void writeInternal(final LogEvent event) {
        StringReader reader = null;
        try {
            if (!this.isRunning() || this.connection == null || this.connection.isClosed() || this.statement == null || this.statement.isClosed()) {
                throw new AppenderLoggingException("Cannot write logging event; JDBC manager not connected to the database.");
            }
            int i = 1;
            for (final Column column : this.columns) {
                if (column.isEventTimestamp) {
                    this.statement.setTimestamp(i++, new Timestamp(event.getTimeMillis()));
                }
                else if (column.isClob) {
                    reader = new StringReader(column.layout.toSerializable(event));
                    if (column.isUnicode) {
                        this.statement.setNClob(i++, reader);
                    }
                    else {
                        this.statement.setClob(i++, reader);
                    }
                }
                else if (column.isUnicode) {
                    this.statement.setNString(i++, column.layout.toSerializable(event));
                }
                else {
                    this.statement.setString(i++, column.layout.toSerializable(event));
                }
            }
            if (this.isBatchSupported) {
                this.statement.addBatch();
            }
            else if (this.statement.executeUpdate() == 0) {
                throw new AppenderLoggingException("No records inserted in database table for log event in JDBC manager.");
            }
        }
        catch (SQLException e) {
            throw new AppenderLoggingException("Failed to insert record for log event in JDBC manager: " + e.getMessage(), e);
        }
        finally {
            Closer.closeSilently(reader);
        }
    }
    
    @Override
    protected void commitAndClose() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                if (this.isBatchSupported) {
                    this.statement.executeBatch();
                }
                this.connection.commit();
            }
        }
        catch (SQLException e) {
            throw new AppenderLoggingException("Failed to commit transaction logging event or flushing buffer.", e);
        }
        finally {
            try {
                Closer.close(this.statement);
            }
            catch (Exception e2) {
                this.logWarn("failed to close SQL statement logging event or flushing buffer", e2);
                this.statement = null;
            }
            finally {
                this.statement = null;
            }
            try {
                Closer.close(this.connection);
            }
            catch (Exception e2) {
                this.logWarn("failed to close database connection logging event or flushing buffer", e2);
                this.connection = null;
            }
            finally {
                this.connection = null;
            }
        }
    }
    
    public static JdbcDatabaseManager getJDBCDatabaseManager(final String name, final int bufferSize, final ConnectionSource connectionSource, final String tableName, final ColumnConfig[] columnConfigs) {
        return AbstractDatabaseManager.getManager(name, new FactoryData(bufferSize, connectionSource, tableName, columnConfigs), (ManagerFactory<JdbcDatabaseManager, FactoryData>)getFactory());
    }
    
    private static JdbcDatabaseManagerFactory getFactory() {
        return JdbcDatabaseManager.INSTANCE;
    }
    
    static {
        INSTANCE = new JdbcDatabaseManagerFactory();
    }
    
    private static final class FactoryData extends AbstractFactoryData
    {
        private final ColumnConfig[] columnConfigs;
        private final ConnectionSource connectionSource;
        private final String tableName;
        
        protected FactoryData(final int bufferSize, final ConnectionSource connectionSource, final String tableName, final ColumnConfig[] columnConfigs) {
            super(bufferSize);
            this.connectionSource = connectionSource;
            this.tableName = tableName;
            this.columnConfigs = columnConfigs;
        }
    }
    
    private static final class JdbcDatabaseManagerFactory implements ManagerFactory<JdbcDatabaseManager, FactoryData>
    {
        @Override
        public JdbcDatabaseManager createManager(final String name, final FactoryData data) {
            final StringBuilder columnPart = new StringBuilder();
            final StringBuilder valuePart = new StringBuilder();
            final List<Column> columns = new ArrayList<Column>();
            int i = 0;
            for (final ColumnConfig config : data.columnConfigs) {
                if (i++ > 0) {
                    columnPart.append(',');
                    valuePart.append(',');
                }
                columnPart.append(config.getColumnName());
                if (config.getLiteralValue() != null) {
                    valuePart.append(config.getLiteralValue());
                }
                else {
                    columns.add(new Column(config.getLayout(), config.isEventTimestamp(), config.isUnicode(), config.isClob()));
                    valuePart.append('?');
                }
            }
            final String sqlStatement = "INSERT INTO " + data.tableName + " (" + (Object)columnPart + ") VALUES (" + (Object)valuePart + ')';
            return new JdbcDatabaseManager(name, data.getBufferSize(), data.connectionSource, sqlStatement, columns, null);
        }
    }
    
    private static final class Column
    {
        private final PatternLayout layout;
        private final boolean isEventTimestamp;
        private final boolean isUnicode;
        private final boolean isClob;
        
        private Column(final PatternLayout layout, final boolean isEventDate, final boolean isUnicode, final boolean isClob) {
            this.layout = layout;
            this.isEventTimestamp = isEventDate;
            this.isUnicode = isUnicode;
            this.isClob = isClob;
        }
    }
}
