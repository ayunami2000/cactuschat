// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.Closeable;

public final class Closer
{
    private Closer() {
    }
    
    public static void closeSilently(final Closeable closeable) {
        try {
            close(closeable);
        }
        catch (Exception ex) {}
    }
    
    public static void close(final Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }
    
    public static void closeSilently(final ServerSocket serverSocket) {
        try {
            close(serverSocket);
        }
        catch (Exception ex) {}
    }
    
    public static void close(final ServerSocket serverSocket) throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }
    
    public static void closeSilently(final DatagramSocket datagramSocket) {
        try {
            close(datagramSocket);
        }
        catch (Exception ex) {}
    }
    
    public static void close(final DatagramSocket datagramSocket) throws IOException {
        if (datagramSocket != null) {
            datagramSocket.close();
        }
    }
    
    public static void closeSilently(final Statement statement) {
        try {
            close(statement);
        }
        catch (Exception ex) {}
    }
    
    public static void close(final Statement statement) throws SQLException {
        if (statement != null) {
            statement.close();
        }
    }
    
    public static void closeSilently(final Connection connection) {
        try {
            close(connection);
        }
        catch (Exception ex) {}
    }
    
    public static void close(final Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
