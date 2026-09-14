package com.umaso.mantenimientos.config;

import com.umaso.mantenimientos.modules.health.service.DatabaseKeepAlive;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class DatabaseKeepAliveTest {
    @Test
    void boundsQueryAndClosesResources() throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet result = mock(ResultSet.class);
        when(source.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT 1")).thenReturn(result);
        when(result.next()).thenReturn(true, false);
        var metadata = mock(java.sql.ResultSetMetaData.class);
        when(result.getMetaData()).thenReturn(metadata);
        when(metadata.getColumnCount()).thenReturn(1);
        when(result.getInt(1)).thenReturn(1);

        new DatabaseKeepAlive(source).ping();

        verify(statement).setQueryTimeout(5);
        verify(statement).executeQuery("SELECT 1");
        verify(result).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void failureDoesNotLeakDriverDetailsOrPreventNextAttempt(CapturedOutput output) throws Exception {
        DataSource source = mock(DataSource.class);
        when(source.getConnection()).thenThrow(new SQLException("secret-password@example-host"));
        DatabaseKeepAlive task = new DatabaseKeepAlive(source);

        task.ping();
        task.ping();

        verify(source, times(2)).getConnection();
        assertThat(output.getAll()).contains("Database keep-alive failed")
                .doesNotContain("secret-password", "example-host");
    }
}
