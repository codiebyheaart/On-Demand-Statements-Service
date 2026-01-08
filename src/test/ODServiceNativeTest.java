package com.td.odapi.ext.impl;

import com.ibm.edms.od.ODFolder;
import com.td.odapi.ext.ODFailureException;
import com.td.odapi.ext.ODServerConfig;
import com.td.odapi.util.CryptoUtils;
import com.td.odapi.util.PropertyLoader;
import com.td.odapi.util.Util;
import com.td.os.pool.PoolConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**

- Comprehensive unit test for ODServiceNative with proper Mockito usage
- 
- @ExtendWith(MockitoExtension.class) - Enables Mockito annotations and injection
  */
@ExtendWith(MockitoExtension.class)
class ODServiceNativeTest {

  // @Mock - Creates mock objects for dependencies
  @Mock
  private PropertyLoader mockPropertyLoader;

  @Mock
  private PoolConfig mockPoolConfig;

  @Mock
  private ODServerConfig mockServerConfig;

  @Mock
  private AbstractODServer mockAbstractODServer;

  @Mock
  private ODFolder mockFolder;

  @Mock
  private ODServerPool mockPool;

  // @InjectMocks - Automatically injects mocks into the class under test
  @InjectMocks
  private ODServiceNative odServiceNative;

  private Map < String, ODServerConfig > serverConfigMap;

  // MockedStatic for mocking static methods
  private MockedStatic < CMODConnectionManager > mockedConnectionManager;
  private MockedStatic < CryptoUtils > mockedCryptoUtils;
  private MockedStatic < Util > mockedUtil;

  @Mock
  private CMODConnectionManager mockConnectionManager;

  /**
  - @TempDir - JUnit 5 provides a temporary directory for file operations
    */
  @TempDir
  Path tempDir;

  /**
  - @BeforeEach - Setup before each test
  - This creates the proper mocking infrastructure for static methods
    */
  @BeforeEach
  void setUp() {
    // Initialize server config map
    serverConfigMap = new HashMap < > ();
    serverConfigMap.put("testPortal", mockServerConfig);

    // Mock static CMODConnectionManager
    mockedConnectionManager = mockStatic(CMODConnectionManager.class);
    mockedConnectionManager.when(CMODConnectionManager::getInstance)
      .thenReturn(mockConnectionManager);

    // Mock static CryptoUtils
    mockedCryptoUtils = mockStatic(CryptoUtils.class);

    // Mock static Util
    mockedUtil = mockStatic(Util.class);

    // Setup default property loader behavior
    Map < String, String > defaultProps = new HashMap < > ();
    defaultProps.put("od.maxHits", "100");
    defaultProps.put("logwk.traceLevel", "2");
    defaultProps.put("odNative_LogonAndRetryOnException", "true");
    defaultProps.put("odNative_closeFolderOnReturnToPool", "false");
    when(mockPropertyLoader.getAppProperties()).thenReturn(defaultProps);

    // Setup connection manager pool map
    Map < String, ODServerPool > poolMap = new HashMap < > ();
    poolMap.put("testPortal", mockPool);
    when(mockConnectionManager.getPoolmap()).thenReturn(poolMap);
  }

  /**
  - @AfterEach - Cleanup after each test
  - Critical for closing static mocks to prevent memory leaks
    */
  @AfterEach
  void tearDown() {
    if (mockedConnectionManager != null) {
      mockedConnectionManager.close();
    }
    if (mockedCryptoUtils != null) {
      mockedCryptoUtils.close();
    }
    if (mockedUtil != null) {
      mockedUtil.close();
    }
  }

  // ==================== INIT METHOD TESTS ====================

  /**
  - Test init() creates OD init file when odInitDir is null
    */
  @Test
  void testInit_WhenOdInitDirIsNull_CreatesTemporaryFile() throws Exception {
    // Arrange
    odServiceNative.setOdInitDir(null);
    String tempDirPath = tempDir.toString();

    // Mock System.getProperty to return our temp directory
    try (MockedStatic < System > mockedSystem = mockStatic(System.class, CALLS_REAL_METHODS)) {
      mockedSystem.when(() -> System.getProperty("java.io.tmpdir"))
        .thenReturn(tempDirPath);

      ``
      `
     // Act
     odServiceNative.init();
     
     // Assert
     assertNotNull(odServiceNative.getOdInitDir());
     assertEquals(tempDirPath, odServiceNative.getOdInitDir());
    `
      ``

    }
  }

  /**
  - Test init() uses existing odInitDir when set
    */
  @Test
  void testInit_WhenOdInitDirIsSet_UsesExistingDirectory() throws Exception {
    // Arrange
    String existingDir = tempDir.toString();
    odServiceNative.setOdInitDir(existingDir);

    // Act
    odServiceNative.init();

    // Assert
    assertEquals(existingDir, odServiceNative.getOdInitDir());
  }

  /**
  - Test init() throws exception when file creation fails
    */
  @Test
  void testInit_WhenFileCreationFails_ThrowsException() {
    // Arrange
    odServiceNative.setOdInitDir("/invalid/path / that / does / not / exist");

    // Act & Assert
    assertThrows(ODFailureException.class, () -> odServiceNative.init());
  }

  // ==================== SETUP CONNECTIONS TESTS ====================

  /**
  - Test setupConnections successfully creates pool for each server config
    */
  @Test
  void testSetupConnections_WithValidConfig_CreatesPoolsSuccessfully() {
    // Arrange
    ODServerPool newPool = mock(ODServerPool.class);
    Map < String, ODServerPool > poolMap = new HashMap < > ();

    when(mockConnectionManager.getPoolmap()).thenReturn(poolMap);
    when(mockServerConfig.toString()).thenReturn("ServerConfig[host = localhost]");

    odServiceNative.setOdInitDir(tempDir.toString());
    odServiceNative.setServerConfigmap(serverConfigMap);

    // Act
    odServiceNative.setupConnections(serverConfigMap);

    // Assert
    verify(mockConnectionManager, atLeastOnce()).getPoolmap();
  }

  /**
  - Test setupConnections handles exception during pool initialization
    */
  @Test
  void testSetupConnections_WhenPoolInitFails_LogsErrorAndContinues() {
    // Arrange
    when(mockConnectionManager.getPoolmap()).thenThrow(new RuntimeException("Pool init failed"));
    odServiceNative.setServerConfigmap(serverConfigMap);

    // Act & Assert
    assertDoesNotThrow(() -> odServiceNative.setupConnections(serverConfigMap));
    verify(mockConnectionManager, atLeastOnce()).getPoolmap();
  }

  // ==================== GET ALL FOLDERS TESTS ====================

  /**
  - Test getAllFolders returns list of folder names
    */
  @Test
  void testGetAllFolders_WithValidPortal_ReturnsFolderList() throws Exception {
    // Arrange
    String portal = "testPortal";

    // Mock pool behavior
    when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);
    when(mockAbstractODServer.getNumFolders()).thenReturn(2);

    // Mock folder enumeration
    Enumeration < String > mockEnumeration = mock(Enumeration.class);
    when(mockEnumeration.hasMoreElements())
      .thenReturn(true, true, false);
    when(mockEnumeration.nextElement())
      .thenReturn("Folder1", "Folder2");
    when(mockAbstractODServer.getFolderNames()).thenReturn(mockEnumeration);

    mockedUtil.when(() -> Util.sanitize(anyString())).thenAnswer(inv -> inv.getArgument(0));

    // Act
    String[] folders = odServiceNative.getAllFolders(portal);

    // Assert
    assertNotNull(folders);
    assertEquals(2, folders.length);
    assertEquals("Folder1", folders[0]);
    assertEquals("Folder2", folders[1]);

    // Verify pool interactions
    verify(mockPool).borrowObject();
    verify(mockAbstractODServer).getNumFolders();
    verify(mockAbstractODServer).getFolderNames();
    verify(mockPool).returnObject(mockAbstractODServer);
  }

  /**
  - Test getAllFolders throws exception when portal not found
    */
  @Test
  void testGetAllFolders_WithInvalidPortal_ThrowsException() {
    // Arrange
    Map < String, ODServerPool > emptyPoolMap = new HashMap < > ();
    when(mockConnectionManager.getPoolmap()).thenReturn(emptyPoolMap);

    // Act & Assert
    ODFailureException exception = assertThrows(ODFailureException.class,
      () -> odServiceNative.getAllFolders("invalidPortal"));

    assertTrue(exception.getMessage().contains("failed with exception"));
  }

  /**
  - Test getAllFolders handles exception and returns object to pool
    */
  @Test
  void testGetAllFolders_WhenExceptionOccurs_ReturnsObjectToPool() throws Exception {
    // Arrange
    String portal = "testPortal";

    when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);
    when(mockAbstractODServer.getNumFolders()).thenThrow(new RuntimeException("Test exception"));

    // Act & Assert
    assertThrows(ODFailureException.class, () -> odServiceNative.getAllFolders(portal));

    // Verify pool cleanup in finally block
    verify(mockPool).borrowObject();
    verify(mockPool).returnObject(mockAbstractODServer);
  }

  // ==================== INGEST REPORT TO FOLDER TESTS ====================

  /**
  - Test ingestReportToFolder successfully ingests document
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
    Map < String, String > values = new HashMap < > ();
    values.put("field1", "value1");
    Hashtable < String, String > hashValues = new Hashtable < > ();

    // Mock pool and server behavior
    when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);
    doNothing().when(mockAbstractODServer).addReportToFolder(
      eq(folderName),
      eq(docContent),
      eq(applicationGroup),
      eq(application),
      any()
    );

    mockedUtil.when(() -> Util.sanitize(anyString())).thenAnswer(inv -> inv.getArgument(0));

    // Act
    String result = odServiceNative.ingestReportToFolder(
      portal, clientID, folderName, docContent,
      applicationGroup, application, values, hashValues
    );

    // Assert
    assertEquals("", result); // Success returns empty string

    // Verify interactions
    verify(mockPool).borrowObject();
    verify(mockAbstractODServer).addReportToFolder(
      eq(folderName),
      eq(docContent),
      eq(applicationGroup),
      eq(application),
      any()
    );
    verify(mockPool).returnObject(mockAbstractODServer);
  }

  /**
  - Test ingestReportToFolder with null folder name
    */
  @Test
  void testIngestReportToFolder_WithNullFolderName_ThrowsException() {
    // Arrange
    String portal = "testPortal";
    byte[] docContent = "Test".getBytes();

    when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);

    // Act & Assert
    ODFailureException exception = assertThrows(ODFailureException.class,
      () -> odServiceNative.ingestReportToFolder(
        portal, "client", null, docContent, "group", "app", new HashMap < > (), new Hashtable < > ()
      )
    );

    assertTrue(exception.getMessage().contains("status"));
  }

  /**
  - Test ingestReportToFolder handles exception during ingestion
    */
  @Test
  void testIngestReportToFolder_WhenIngestionFails_ThrowsExceptionAndReturnsObject() throws Exception {
    // Arrange
    String portal = "testPortal";
    String folderName = "TestFolder";
    byte[] docContent = "Test".getBytes();

    when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);
    doThrow(new RuntimeException("Ingestion failed"))
      .when(mockAbstractODServer).addReportToFolder(
        anyString(), any(byte[].class), anyString(), anyString(), any()
      );

    mockedUtil.when(() -> Util.sanitize(anyString())).thenAnswer(inv -> inv.getArgument(0));

    // Act & Assert
    assertThrows(ODFailureException.class,
      () -> odServiceNative.ingestReportToFolder(
        portal, "client", folderName, docContent, "group", "app", new HashMap < > (), new Hashtable < > ()
      )
    );

    // Verify pool cleanup
    verify(mockPool).borrowObject();
    verify(mockPool).returnObject(mockAbstractODServer);
  }

  // ==================== RELEASE RESOURCES TESTS ====================

  /**
  - Test releaseResources closes folder when configured
    */
  @Test
  void testReleaseResources_WithCloseFolderEnabled_ClosesFolder() throws Exception {
    // Arrange
    odServiceNative.setCloseFolderOnReturnToPool(true);
    ODFolder folder = mock(ODFolder.class);

    when(mockPool.closeFolder()).thenReturn(true);
    doNothing().when(folder).close();

    // Act
    odServiceNative.releaseResources(mockPool, mockPool, folder, mockAbstractODServer);

    // Assert
    verify(folder).close();
  }

  /**
  - Test releaseResources returns objects to pool
    */
  @Test
  void testReleaseResources_Always_ReturnsObjectsToPool() {
    // Arrange
    odServiceNative.setCloseFolderOnReturnToPool(false);

    // Act
    odServiceNative.releaseResources(mockPool, mockPool, null, mockAbstractODServer);

    // Assert
    verify(mockPool).returnObject(mockAbstractODServer);
  }

  /**
  - Test releaseResources handles exception during close
    */
  @Test
  void testReleaseResources_WhenCloseThrowsException_HandlesGracefully() throws Exception {
    // Arrange
    odServiceNative.setCloseFolderOnReturnToPool(true);
    ODFolder folder = mock(ODFolder.class);

    when(mockPool.closeFolder()).thenReturn(true);
    doThrow(new RuntimeException("Close failed")).when(folder).close();

    // Act & Assert - should not throw
    assertDoesNotThrow(() ->
      odServiceNative.releaseResources(mockPool, mockPool, folder, mockAbstractODServer)
    );
  }

  // ==================== CLOSE METHOD TESTS ====================

  /**
  - Test close method closes pool connection
    */
  @Test
  void testClose_WithValidPortal_ClosesPoolConnection() {
    // Arrange
    String portal = "testPortal";
    Map < String, ODServerPool > poolMap = new HashMap < > ();
    poolMap.put(portal, mockPool);

    when(mockConnectionManager.getPoolmap()).thenReturn(poolMap);
    doNothing().when(mockPool).close();

    // Act
    odServiceNative.close(portal);

    // Assert
    verify(mockConnectionManager).getPoolmap();
    verify(mockPool).close();
  }

  /**
  - Test close handles exception gracefully
    */
  @Test
  void testClose_WhenExceptionOccurs_HandlesGracefully() {
    // Arrange
    String portal = "testPortal";

    when(mockConnectionManager.getPoolmap()).thenThrow(new RuntimeException("Close failed"));

    // Act & Assert - should not throw
    assertDoesNotThrow(() -> odServiceNative.close(portal));
  }

  // ==================== GETTER/SETTER TESTS ====================

  @Test
  void testGettersAndSetters_AllProperties() {
    // Test OdInitDir
    String testDir = "/test/dir";
    odServiceNative.setOdInitDir(testDir);
    assertEquals(testDir, odServiceNative.getOdInitDir());

    ``
    `
   // Test CloseFolderOnReturnToPool
   odServiceNative.setCloseFolderOnReturnToPool(true);
   assertTrue(odServiceNative.isCloseFolderOnReturnToPool());
   
   odServiceNative.setCloseFolderOnReturnToPool(false);
   assertFalse(odServiceNative.isCloseFolderOnReturnToPool());
   
   // Test LogonAndRetryOnException
   odServiceNative.setLogonAndRetryOnException(true);
   assertTrue(odServiceNative.isLogonAndRetryOnException());
   
   // Test MaxHits
   odServiceNative.setMaxHits(500);
   assertEquals(500, odServiceNative.getMaxHits());
   
   // Test TraceLevel
   odServiceNative.setTraceLevel(3);
   assertEquals(3, odServiceNative.getTraceLevel());
   
   // Test PoolConfig
   odServiceNative.setPoolConfig(mockPoolConfig);
   assertEquals(mockPoolConfig, odServiceNative.getPoolConfig());
   
   // Test ServerConfigmap
   odServiceNative.setServerConfigmap(serverConfigMap);
   assertEquals(serverConfigMap, odServiceNative.getServerConfigmap());
   
   // Test PropLoader
   assertEquals(mockPropertyLoader, odServiceNative.getPropLoader());
  `
    ``

  }

  // ==================== EDGE CASE TESTS ====================

  /**
  - Test with empty folder enumeration
    */
  @Test
  void testGetAllFolders_WithNoFolders_ReturnsEmptyArray() throws Exception {
    // Arrange
    String portal = "testPortal";

    when(mockPool.borrowObject()).thenReturn(mockAbstractODServer);
    when(mockAbstractODServer.getNumFolders()).thenReturn(0);

    Enumeration < String > emptyEnum = Collections.emptyEnumeration();
    when(mockAbstractODServer.getFolderNames()).thenReturn(emptyEnum);

    // Act
    String[] folders = odServiceNative.getAllFolders(portal);

    // Assert
    assertNotNull(folders);
    assertEquals(0, folders.length);
    verify(mockPool).returnObject(mockAbstractODServer);
  }

  /**
  - Test maxHits boundary values
    */
  @Test
  void testMaxHits_BoundaryValues() {
    odServiceNative.setMaxHits(0);
    assertEquals(0, odServiceNative.getMaxHits());

    odServiceNative.setMaxHits(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, odServiceNative.getMaxHits());

    odServiceNative.setMaxHits(-1);
    assertEquals(-1, odServiceNative.getMaxHits());
  }

  /**
  - Test concurrent access scenario (simulated)
    */
  @Test
  void testConcurrentAccess_MultiplePortals() throws Exception {
    // Arrange
    Map < String, ODServerPool > multiPoolMap = new HashMap < > ();
    ODServerPool pool1 = mock(ODServerPool.class);
    ODServerPool pool2 = mock(ODServerPool.class);
    AbstractODServer server1 = mock(AbstractODServer.class);
    AbstractODServer server2 = mock(AbstractODServer.class);

    multiPoolMap.put("portal1", pool1);
    multiPoolMap.put("portal2", pool2);

    when(mockConnectionManager.getPoolmap()).thenReturn(multiPoolMap);
    when(pool1.borrowObject()).thenReturn(server1);
    when(pool2.borrowObject()).thenReturn(server2);
    when(server1.getNumFolders()).thenReturn(1);
    when(server2.getNumFolders()).thenReturn(1);

    Enumeration < String > enum1 = Collections.enumeration(Arrays.asList("Folder1"));
    Enumeration < String > enum2 = Collections.enumeration(Arrays.asList("Folder2"));
    when(server1.getFolderNames()).thenReturn(enum1);
    when(server2.getFolderNames()).thenReturn(enum2);

    mockedUtil.when(() -> Util.sanitize(anyString())).thenAnswer(inv -> inv.getArgument(0));

    // Act
    String[] folders1 = odServiceNative.getAllFolders("portal1");
    String[] folders2 = odServiceNative.getAllFolders("portal2");

    // Assert
    assertNotNull(folders1);
    assertNotNull(folders2);
    verify(pool1).borrowObject();
    verify(pool2).borrowObject();
    verify(pool1).returnObject(server1);
    verify(pool2).returnObject(server2);
  }
}
