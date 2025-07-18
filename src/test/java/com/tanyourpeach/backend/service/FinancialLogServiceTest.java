package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.repository.FinancialLogRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class FinancialLogServiceTest {

    @Mock
    private FinancialLogRepository financialLogRepository;

    @InjectMocks
    private FinancialLogService financialLogService;

    private FinancialLog log;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        log = new FinancialLog();
        log.setLogId(1L);
        log.setType(FinancialLog.Type.revenue);
        log.setSource("appointment");
        log.setReferenceId(100L);
        log.setDescription("Log test");
        log.setAmount(BigDecimal.valueOf(50.0));
    }

    @Test
    void getAllLogs_shouldReturnList() {
        when(financialLogRepository.findAll()).thenReturn(List.of(log));
        List<FinancialLog> logs = financialLogService.getAllLogs();
        assertEquals(1, logs.size());
    }

    @Test
    void getLogById_shouldReturnLog_whenExists() {
        when(financialLogRepository.findById(1L)).thenReturn(Optional.of(log));
        Optional<FinancialLog> found = financialLogService.getLogById(1L);
        assertTrue(found.isPresent());
        assertEquals(log.getLogId(), found.get().getLogId());
    }

    @Test
    void getAllLogs_shouldReturnEmptyList_whenNoLogsExist() {
        when(financialLogRepository.findAll()).thenReturn(List.of());

        List<FinancialLog> logs = financialLogService.getAllLogs();

        assertNotNull(logs);
        assertTrue(logs.isEmpty());
    }

    @Test
    void getLogById_shouldReturnEmpty_whenNotExists() {
        when(financialLogRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<FinancialLog> found = financialLogService.getLogById(2L);
        assertTrue(found.isEmpty());
    }

    @Test
    void createLog_shouldSaveSuccessfully() {
        when(financialLogRepository.save(log)).thenReturn(log);
        FinancialLog created = financialLogService.createLog(log);
        assertEquals(log.getLogId(), created.getLogId());
    }

    @Test
    void createLog_shouldHandleLogWithoutId() {
        FinancialLog newLog = new FinancialLog();
        newLog.setType(FinancialLog.Type.expense);
        newLog.setSource("manual");
        newLog.setReferenceId(300L);
        newLog.setDescription("No ID yet");
        newLog.setAmount(BigDecimal.valueOf(25.0));

        FinancialLog saved = new FinancialLog();
        saved.setLogId(10L);
        saved.setType(newLog.getType());
        saved.setSource(newLog.getSource());
        saved.setReferenceId(newLog.getReferenceId());
        saved.setDescription(newLog.getDescription());
        saved.setAmount(newLog.getAmount());

        when(financialLogRepository.save(newLog)).thenReturn(saved);

        FinancialLog result = financialLogService.createLog(newLog);

        assertNotNull(result);
        assertEquals(10L, result.getLogId());
    }

    @Test
    void createLog_shouldAllowNullSource() {
        FinancialLog log = new FinancialLog();
        log.setType(FinancialLog.Type.revenue);
        log.setSource(null); // Allowed
        log.setAmount(BigDecimal.valueOf(30.0));

        when(financialLogRepository.save(any())).thenAnswer(i -> {
            FinancialLog saved = i.getArgument(0);
            saved.setLogId(55L);
            return saved;
        });

        FinancialLog result = financialLogService.createLog(log);
        assertNotNull(result);
        assertEquals(55L, result.getLogId());
    }

    @Test
    void createLog_shouldReturnNull_whenAmountIsNegative() {
        FinancialLog invalidLog = new FinancialLog();
        invalidLog.setAmount(BigDecimal.valueOf(-10));
        invalidLog.setType(FinancialLog.Type.expense);
        invalidLog.setSource("test");
        invalidLog.setDescription("Invalid");

        FinancialLog result = financialLogService.createLog(invalidLog);
        assertNull(result);
    }

    @Test
    void createLog_shouldReturnNull_whenRequiredFieldsMissing() {
        FinancialLog incompleteLog = new FinancialLog(); // no amount, type, etc.
        FinancialLog result = financialLogService.createLog(incompleteLog);
        assertNull(result);
    }

    @Test
    void updateLog_shouldModifyAndSave() {
        FinancialLog updated = new FinancialLog();
        updated.setType(FinancialLog.Type.expense);
        updated.setSource("inventory");
        updated.setReferenceId(200L);
        updated.setDescription("Updated log");
        updated.setAmount(BigDecimal.valueOf(80.0));

        when(financialLogRepository.findById(1L)).thenReturn(Optional.of(log));
        when(financialLogRepository.save(any())).thenReturn(updated);

        Optional<FinancialLog> result = financialLogService.updateLog(1L, updated);
        assertTrue(result.isPresent());
        assertEquals(FinancialLog.Type.expense, result.get().getType());
    }

    @Test
    void updateLog_shouldPreserveLogId_whenUpdating() {
        FinancialLog updated = new FinancialLog();
        updated.setType(FinancialLog.Type.expense);
        updated.setSource("inventory");
        updated.setReferenceId(200L);
        updated.setDescription("Updated log");
        updated.setAmount(BigDecimal.valueOf(80.0));

        when(financialLogRepository.findById(1L)).thenReturn(Optional.of(log));
        when(financialLogRepository.save(any())).thenAnswer(i -> i.getArgument(0)); // simulate in-place update

        Optional<FinancialLog> result = financialLogService.updateLog(1L, updated);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getLogId()); // original ID
        assertEquals("inventory", result.get().getSource());
    }

    @Test
    void updateLog_shouldAllowNullSource() {
        FinancialLog updated = new FinancialLog();
        updated.setType(FinancialLog.Type.expense);
        updated.setSource(null); // now allowed
        updated.setReferenceId(42L);
        updated.setDescription("Null source update");
        updated.setAmount(BigDecimal.valueOf(75.00));

        when(financialLogRepository.findById(1L)).thenReturn(Optional.of(log));
        when(financialLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<FinancialLog> result = financialLogService.updateLog(1L, updated);

        assertTrue(result.isPresent());
        assertNull(result.get().getSource());
        assertEquals(BigDecimal.valueOf(75.00), result.get().getAmount());
    }

    @Test
    void updateLog_shouldReturnEmpty_whenValidationFails() {
        FinancialLog updated = new FinancialLog();
        updated.setAmount(BigDecimal.valueOf(-5)); // invalid
        updated.setType(FinancialLog.Type.revenue);
        updated.setSource("test");
        updated.setDescription("bad");

        Optional<FinancialLog> result = financialLogService.updateLog(1L, updated);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateLog_shouldReturnEmpty_whenNotFound() {
        when(financialLogRepository.findById(3L)).thenReturn(Optional.empty());
        Optional<FinancialLog> result = financialLogService.updateLog(3L, new FinancialLog());
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteLog_shouldDelete_whenExists() {
        when(financialLogRepository.existsById(1L)).thenReturn(true);
        boolean deleted = financialLogService.deleteLog(1L);
        assertTrue(deleted);
        verify(financialLogRepository).deleteById(1L);
    }

    @Test
    void deleteLog_shouldReturnFalse_whenNotFound() {
        when(financialLogRepository.existsById(99L)).thenReturn(false);
        boolean deleted = financialLogService.deleteLog(99L);
        assertFalse(deleted);
    }
}