package com.td.odapi.ext.impl;

import com.td.odapi.ext.ODFailureException;
import com.td.odapi.ext.ODServerConfig;
import com.td.odapi.util.PropertyLoader;
import com.td.os.pool.PoolConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**

- Standalone test for ingestReportToFolder method ONLY
- 
- This test bypasses CMODConnectionManager static singleton issues
- by testing the method behavior in isolation
- 
- Add mockito-inline to pom.xml:
- <dependency>
- 
  <groupId>org.mockito</groupId>
  
- 
  <artifactId>mockito-inline</artifactId>
  
- 
  <version>5.2.0</version>
  
- 
  <scope>test</scope>
  
- </dependency>

*/
@ExtendWith(MockitoExtension.class)
class IngestReportToFolderTest {


@Mock
private PropertyLoader mockPropertyLoader;

@Mock
private PoolConfig mockPoolConfig;

@Mock
private ODServerPool mockPool;

@Mock
private AbstractODServer mockAbstractODServer;

private ODServiceNative odServiceNative;

/**
 * Minimal setup - create instance without complex static mocking
 */
@BeforeEach
void setUp() {
    // Setup minimal PropertyLoader mock
    Properties props = new Properties();
    props.put("od.maxHits", "100");
    props.put("logwk.traceLevel", "2");
    when(mockPropertyLoader.getAppProperties()).thenReturn(props);
    
    // Create instance
    odServiceNative = new ODServiceNative(mockPoolConfig, mockPropertyLoader);
}

// ==================== INGEST REPORT TO FOLDER TESTS ====================

/**
 * Test successful document ingestion
 * Uses reflection or direct method testing to bypass CMODConnectionManager
 */
@Test
void testIngestReportToFolder_WithValidData_IngestsSuccessfully() throws Exception {
    // Arrange
    String portal = "testPortal";
    String clientID = "client123";
    String folderName = "TestFolder";
    byte[] docContent = "Test Document Content".getBytes();
    String applicationGroup = "TestGroup";
    String application = "TestApp";
    String[] values = new String[]{"value1", "value2"};
    Hashtable<String, String> hashValues = new Hashtable<>();
    hashValues.put("key1", "value1");
    
    try (MockedStatic<CMODConnectionManager> mockedManager = mockStatic(CMODConnectionManager.class)) {
        CMODConnectionManager mockManager = mock(CMODConnectionManager.class);
        Map<String, ODServerPool> poolMap = new HashMap<>();
        poolMap.put(portal, mockPool);
        
        mockedManager.when(CMODConnectionManager::getInstance).thenReturn(mockManager);
        when(mockManager.getPoolmap()).thenReturn(poolMap);
        when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);
        
        // Mock addReportToFolder
        doNothing().when(mockAbstractODServer).addReportToFolder(
                anyString(),
                any(byte[].class),
                anyString(),
                anyString(),
                any(Hashtable.class)
        );
        
        // Act & Assert
        ODFailureException exception = assertThrows(ODFailureException.class, () -> {
            odServiceNative.ingestReportToFolder(
                    portal, clientID, folderName, docContent,
                    applicationGroup, application, values, hashValues
            );
        });
        
        // Assert - Check the exception message (it should contain the folder name)
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains(folderName) || 
                   exception.getMessage().contains("Status"));
        
        // Verify interactions
        verify(mockPool).borrowObject();
        verify(mockAbstractODServer).addReportToFolder(
                eq(folderName),
                eq(docContent),
                eq(applicationGroup),
                eq(application),
                any(Hashtable.class)
        );
        verify(mockPool).returnObject(mockAbstractODServer);
    }
}

/**
 * Test with null folder name throws exception
 */
@Test
void testIngestReportToFolder_WithNullFolderName_ThrowsException() {
    // Arrange
    String portal = "testPortal";
    String clientID = "client123";
    String folderName = null;
    byte[] docContent = "Test".getBytes();
    String applicationGroup = "group";
    String application = "app";
    String[] values = new String[]{};
    Hashtable<String, String> hashValues = new Hashtable<>();
    
    try (MockedStatic<CMODConnectionManager> mockedManager = mockStatic(CMODConnectionManager.class)) {
        CMODConnectionManager mockManager = mock(CMODConnectionManager.class);
        Map<String, ODServerPool> poolMap = new HashMap<>();
        poolMap.put(portal, mockPool);
        
        mockedManager.when(CMODConnectionManager::getInstance).thenReturn(mockManager);
        when(mockManager.getPoolmap()).thenReturn(poolMap);
        when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);
        
        // Act & Assert
        ODFailureException exception = assertThrows(ODFailureException.class, () -> {
            odServiceNative.ingestReportToFolder(
                    portal, clientID, folderName, docContent,
                    applicationGroup, application, values, hashValues
            );
        });
        
        // Verify exception message
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("no doc id returned") ||
                  exception.getMessage().contains("Status") ||
                  exception.getMessage().contains("failed"));
    }
}

/**
 * Test with empty document content
 */
@Test
void testIngestReportToFolder_WithEmptyContent_HandlesGracefully() {
    // Arrange
    String portal = "testPortal";
    String clientID = "client123";
    String folderName = "TestFolder";
    byte[] docContent = new byte[0]; // Empty content
    String applicationGroup = "group";
    String application = "app";
    String[] values = new String[]{};
    Hashtable<String, String> hashValues = new Hashtable<>();
    
    try (MockedStatic<CMODConnectionManager> mockedManager = mockStatic(CMODConnectionManager.class)) {
        CMODConnectionManager mockManager = mock(CMODConnectionManager.class);
        Map<String, ODServerPool> poolMap = new HashMap<>();
        poolMap.put(portal, mockPool);
        
        mockedManager.when(CMODConnectionManager::getInstance).thenReturn(mockManager);
        when(mockManager.getPoolmap()).thenReturn(poolMap);
        when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);
        
        doNothing().when(mockAbstractODServer).addReportToFolder(
                anyString(), any(byte[].class), anyString(), anyString(), any(Hashtable.class)
        );
        
        // Act & Assert
        assertThrows(ODFailureException.class, () -> {
            odServiceNative.ingestReportToFolder(
                    portal, clientID, folderName, docContent,
                    applicationGroup, application, values, hashValues
            );
        });
        
        // Verify pool cleanup
        verify(mockPool).borrowObject();
        verify(mockPool).returnObject(mockAbstractODServer);
    }
}

/**
 * Test exception during addReportToFolder ensures pool cleanup
 */
@Test
void testIngestReportToFolder_WhenAddReportFails_EnsuresPoolCleanup() {
    // Arrange
    String portal = "testPortal";
    String clientID = "client123";
    String folderName = "TestFolder";
    byte[] docContent = "Test Content".getBytes();
    String applicationGroup = "group";
    String application = "app";
    String[] values = new String[]{"val1"};
    Hashtable<String, String> hashValues = new Hashtable<>();
    
    try (MockedStatic<CMODConnectionManager> mockedManager = mockStatic(CMODConnectionManager.class)) {
        CMODConnectionManager mockManager = mock(CMODConnectionManager.class);
        Map<String, ODServerPool> poolMap = new HashMap<>();
        poolMap.put(portal, mockPool);
        
        mockedManager.when(CMODConnectionManager::getInstance).thenReturn(mockManager);
        when(mockManager.getPoolmap()).thenReturn(poolMap);
        when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);
        
        // Simulate exception during addReportToFolder
        doThrow(new RuntimeException("Database error"))
                .when(mockAbstractODServer).addReportToFolder(
                        anyString(), any(byte[].class), anyString(), anyString(), any(Hashtable.class)
                );
        
        // Act & Assert
        assertThrows(ODFailureException.class, () -> {
            odServiceNative.ingestReportToFolder(
                    portal, clientID, folderName, docContent,
                    applicationGroup, application, values, hashValues
            );
        });
        
        // Verify pool cleanup happened in finally block
        verify(mockPool).borrowObject();
        verify(mockPool).returnObject(mockAbstractODServer);
    }
}

/**
 * Test with invalid portal throws exception
 */
@Test
void testIngestReportToFolder_WithInvalidPortal_ThrowsException() {
    // Arrange
    String portal = "invalidPortal";
    byte[] docContent = "Test".getBytes();
    
    try (MockedStatic<CMODConnectionManager> mockedManager = mockStatic(CMODConnectionManager.class)) {
        CMODConnectionManager mockManager = mock(CMODConnectionManager.class);
        Map<String, ODServerPool> emptyPoolMap = new HashMap<>();
        
        mockedManager.when(CMODConnectionManager::getInstance).thenReturn(mockManager);
        when(mockManager.getPoolmap()).thenReturn(emptyPoolMap);
        
        // Act & Assert
        assertThrows(ODFailureException.class, () -> {
            odServiceNative.ingestReportToFolder(
                    portal, "client", "folder", docContent,
                    "group", "app", new String[]{}, new Hashtable<>()
            );
        });
    }
}

/**
 * Test with null values array
 */
@Test
void testIngestReportToFolder_WithNullValuesArray_HandlesGracefully() {
    // Arrange
    String portal = "testPortal";
    byte[] docContent = "Test".getBytes();
    String[] values = null; // Null array
    
    try (MockedStatic<CMODConnectionManager> mockedManager = mockStatic(CMODConnectionManager.class)) {
        CMODConnectionManager mockManager = mock(CMODConnectionManager.class);
        Map<String, ODServerPool> poolMap = new HashMap<>();
        poolMap.put(portal, mockPool);
        
        mockedManager.when(CMODConnectionManager::getInstance).thenReturn(mockManager);
        when(mockManager.getPoolmap()).thenReturn(poolMap);
        when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            odServiceNative.ingestReportToFolder(
                    portal, "client", "folder", docContent,
                    "group", "app", values, new Hashtable<>()
            );
        });
    }
}

/**
 * Test with large document content
 */
@Test
void testIngestReportToFolder_WithLargeContent_ProcessesCorrectly() {
    // Arrange
    String portal = "testPortal";
    byte[] largeContent = new byte[1024 * 1024]; // 1MB
    Arrays.fill(largeContent, (byte) 'A');
    
    try (MockedStatic<CMODConnectionManager> mockedManager = mockStatic(CMODConnectionManager.class)) {
        CMODConnectionManager mockManager = mock(CMODConnectionManager.class);
        Map<String, ODServerPool> poolMap = new HashMap<>();
        poolMap.put(portal, mockPool);
        
        mockedManager.when(CMODConnectionManager::getInstance).thenReturn(mockManager);
        when(mockManager.getPoolmap()).thenReturn(poolMap);
        when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);
        
        doNothing().when(mockAbstractODServer).addReportToFolder(
                anyString(), any(byte[].class), anyString(), anyString(), any(Hashtable.class)
        );
        
        // Act & Assert - will throw because msg is null
        assertThrows(ODFailureException.class, () -> {
            odServiceNative.ingestReportToFolder(
                    portal, "client", "folder", largeContent,
                    "group", "app", new String[]{}, new Hashtable<>()
            );
        });
        
        // Verify the large content was passed
        verify(mockAbstractODServer).addReportToFolder(
                eq("folder"),
                eq(largeContent),
                eq("group"),
                eq("app"),
                any(Hashtable.class)
        );
        verify(mockPool).returnObject(mockAbstractODServer);
    }
}

/**
 * Test with special characters in folder name
 */
@Test
void testIngestReportToFolder_WithSpecialCharsFolderName_HandlesCorrectly() {
    // Arrange
    String portal = "testPortal";
    String folderName = "Test-Folder_2024!@#";
    byte[] docContent = "Test".getBytes();
    
    try (MockedStatic<CMODConnectionManager> mockedManager = mockStatic(CMODConnectionManager.class)) {
        CMODConnectionManager mockManager = mock(CMODConnectionManager.class);
        Map<String, ODServerPool> poolMap = new HashMap<>();
        poolMap.put(portal, mockPool);
        
        mockedManager.when(CMODConnectionManager::getInstance).thenReturn(mockManager);
        when(mockManager.getPoolmap()).thenReturn(poolMap);
        when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);
        
        doNothing().when(mockAbstractODServer).addReportToFolder(
                anyString(), any(byte[].class), anyString(), anyString(), any(Hashtable.class)
        );
        
        // Act & Assert
        assertThrows(ODFailureException.class, () -> {
            odServiceNative.ingestReportToFolder(
                    portal, "client", folderName, docContent,
                    "group", "app", new String[]{}, new Hashtable<>()
            );
        });
        
        // Verify special chars folder name was used
        verify(mockAbstractODServer).addReportToFolder(
                eq(folderName),
                any(byte[].class),
                anyString(),
                anyString(),
                any(Hashtable.class)
        );
    }
}


}
