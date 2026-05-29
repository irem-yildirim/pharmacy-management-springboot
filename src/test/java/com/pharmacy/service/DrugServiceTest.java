package com.pharmacy.service;

import com.pharmacy.dto.request.DrugCreateRequest;
import com.pharmacy.model.Drug;
import com.pharmacy.advice.DrugNotFoundException;
import com.pharmacy.advice.DuplicateEntryException;
import com.pharmacy.repository.DrugRepository;
import com.pharmacy.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DrugServiceTest {

    @Mock
    private DrugRepository drugRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private DrugService drugService;

    private Drug sampleDrug;

    @BeforeEach
    void setUp() {
        sampleDrug = Drug.builder()
                .barcode("8690000000001")
                .name("Parol 500 mg")
                .currentSellingPrice(new BigDecimal("45.50"))
                .minStockAlert(10)
                .isActive(true)
                .build();
    }

    @Test
    void testFindAllActive() {
        when(drugRepository.findByIsActiveTrue()).thenReturn(List.of(sampleDrug));

        List<Drug> result = drugService.findAllActive();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Parol 500 mg", result.get(0).getName());
        verify(drugRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testFindByBarcode() {
        when(drugRepository.findById("8690000000001")).thenReturn(Optional.of(sampleDrug));

        Drug result = drugService.findByBarcode("8690000000001");

        assertNotNull(result);
        assertEquals("Parol 500 mg", result.getName());
        verify(drugRepository, times(1)).findById("8690000000001");
    }

    @Test
    void testFindByBarcodeNotFound() {
        when(drugRepository.findById("9999999999999")).thenReturn(Optional.empty());

        assertThrows(DrugNotFoundException.class, () -> drugService.findByBarcode("9999999999999"));
        verify(drugRepository, times(1)).findById("9999999999999");
    }

    @Test
    void testCreate() {
        DrugCreateRequest request = DrugCreateRequest.builder()
                .barcode("8690000000001")
                .name("Parol 500 mg")
                .categoryId(1L)
                .brandId(1L)
                .currentSellingPrice(new BigDecimal("45.50"))
                .minStockAlert(10)
                .build();

        when(drugRepository.existsById("8690000000001")).thenReturn(false);
        when(drugRepository.save(any(Drug.class))).thenReturn(sampleDrug);

        Drug result = drugService.create(request);

        assertNotNull(result);
        assertEquals("Parol 500 mg", result.getName());
        verify(drugRepository, times(1)).existsById("8690000000001");
        verify(drugRepository, times(1)).save(any(Drug.class));
    }

    @Test
    void testCreateDuplicateBarcode() {
        DrugCreateRequest request = DrugCreateRequest.builder()
                .barcode("8690000000001")
                .name("Parol 500 mg")
                .categoryId(1L)
                .brandId(1L)
                .currentSellingPrice(new BigDecimal("45.50"))
                .minStockAlert(10)
                .build();

        when(drugRepository.existsById("8690000000001")).thenReturn(true);

        assertThrows(DuplicateEntryException.class, () -> drugService.create(request));
        verify(drugRepository, times(1)).existsById("8690000000001");
        verify(drugRepository, never()).save(any(Drug.class));
    }

    @Test
    void testSoftDelete() {
        when(drugRepository.findById("8690000000001")).thenReturn(Optional.of(sampleDrug));
        when(drugRepository.save(any(Drug.class))).thenReturn(sampleDrug);

        drugService.softDelete("8690000000001");

        assertFalse(sampleDrug.getIsActive());
        verify(drugRepository, times(1)).findById("8690000000001");
        verify(drugRepository, times(1)).save(sampleDrug);
    }
}
