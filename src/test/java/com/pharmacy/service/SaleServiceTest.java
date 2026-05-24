package com.pharmacy.service;

import com.pharmacy.entity.*;
import com.pharmacy.exception.DrugNotFoundException;
import com.pharmacy.exception.InsufficientStockException;
import com.pharmacy.exception.PrescriptionRequiredException;
import com.pharmacy.repository.*;
import com.pharmacy.dto.request.SaleItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;
    @Mock
    private DrugRepository drugRepository;
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private SaleService saleService;

    private Drug sampleDrug;
    private Purchase batchOld;
    private Purchase batchNew;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleDrug = Drug.builder()
                .barcode("8690000000001")
                .name("Parol 500 mg")
                .currentSellingPrice(new BigDecimal("45.50"))
                .isActive(true)
                .build();

        batchOld = Purchase.builder()
                .id(1L)
                .drug(sampleDrug)
                .originalQuantity(10)
                .remainingQuantity(4)
                .purchasePrice(new BigDecimal("30.00"))
                .expirationDate(LocalDate.now().plusDays(15))
                .purchaseDate(LocalDate.now().minusDays(30))
                .build();

        batchNew = Purchase.builder()
                .id(2L)
                .drug(sampleDrug)
                .originalQuantity(20)
                .remainingQuantity(20)
                .purchasePrice(new BigDecimal("35.00"))
                .expirationDate(LocalDate.now().plusDays(60))
                .purchaseDate(LocalDate.now().minusDays(5))
                .build();

        sampleUser = User.builder()
                .id(1L)
                .name("Admin User")
                .username("admin")
                .password("hashed")
                .role(Role.ADMIN)
                .isActive(true)
                .build();
    }

    @Test
    void testCreateSaleFIFO() {
        SaleItemRequest request = new SaleItemRequest("8690000000001", 6);
        when(drugRepository.findById("8690000000001")).thenReturn(Optional.of(sampleDrug));
        when(purchaseRepository.findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc(
                "8690000000001", 0)).thenReturn(List.of(batchOld, batchNew));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        Sale sale = saleService.createSale(List.of(request), false, null, sampleUser);

        assertEquals(new BigDecimal("273.00"), sale.getTotalAmount());
        assertEquals(2, sale.getItems().size());
        assertEquals(4, sale.getItems().get(0).getQuantity());
        assertEquals(2, sale.getItems().get(1).getQuantity());
        assertEquals(0, batchOld.getRemainingQuantity().intValue());
        assertEquals(18, batchNew.getRemainingQuantity().intValue());
        verify(purchaseRepository, times(2)).save(any(Purchase.class));
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @Test
    void testCreateSaleSingleBatch() {
        SaleItemRequest request = new SaleItemRequest("8690000000001", 3);
        when(drugRepository.findById("8690000000001")).thenReturn(Optional.of(sampleDrug));
        when(purchaseRepository.findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc(
                "8690000000001", 0)).thenReturn(List.of(batchOld, batchNew));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        Sale sale = saleService.createSale(List.of(request), false, null, sampleUser);

        assertEquals(new BigDecimal("136.50"), sale.getTotalAmount());
        assertEquals(1, sale.getItems().size());
        assertEquals(3, sale.getItems().get(0).getQuantity());
        assertEquals(1, batchOld.getRemainingQuantity().intValue());
    }

    @Test
    void testInsufficientStock() {
        SaleItemRequest request = new SaleItemRequest("8690000000001", 100);
        when(drugRepository.findById("8690000000001")).thenReturn(Optional.of(sampleDrug));
        when(purchaseRepository.findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc(
                "8690000000001", 0)).thenReturn(List.of(batchOld, batchNew));

        assertThrows(InsufficientStockException.class,
                () -> saleService.createSale(List.of(request), false, null, sampleUser));
    }

    @Test
    void testDrugNotFound() {
        SaleItemRequest request = new SaleItemRequest("9999999999999", 1);
        when(drugRepository.findById("9999999999999")).thenReturn(Optional.empty());

        assertThrows(DrugNotFoundException.class,
                () -> saleService.createSale(List.of(request), false, null, sampleUser));
    }

    @Test
    void testPrescriptionRequired() {
        PresType presType = PresType.builder().id(4L).name("Red").riskLevel(2).build();
        Drug rxDrug = Drug.builder()
                .barcode("8690000000002")
                .name("Controlled Drug")
                .presType(presType)
                .currentSellingPrice(new BigDecimal("100.00"))
                .isActive(true)
                .build();
        Purchase rxBatch = Purchase.builder()
                .id(3L)
                .drug(rxDrug)
                .originalQuantity(10)
                .remainingQuantity(10)
                .purchasePrice(new BigDecimal("80.00"))
                .expirationDate(LocalDate.now().plusDays(90))
                .purchaseDate(LocalDate.now())
                .build();

        SaleItemRequest request = new SaleItemRequest("8690000000002", 2);
        when(drugRepository.findById("8690000000002")).thenReturn(Optional.of(rxDrug));

        assertThrows(PrescriptionRequiredException.class,
                () -> saleService.createSale(List.of(request), false, null, sampleUser));
    }

    @Test
    void testPrescriptionLoggedAllowsRxDrug() {
        PresType presType = PresType.builder().id(4L).name("Red").riskLevel(2).build();
        Drug rxDrug = Drug.builder()
                .barcode("8690000000002")
                .name("Controlled Drug")
                .presType(presType)
                .currentSellingPrice(new BigDecimal("100.00"))
                .isActive(true)
                .build();
        Purchase rxBatch = Purchase.builder()
                .id(3L)
                .drug(rxDrug)
                .originalQuantity(10)
                .remainingQuantity(10)
                .purchasePrice(new BigDecimal("80.00"))
                .expirationDate(LocalDate.now().plusDays(90))
                .purchaseDate(LocalDate.now())
                .build();

        SaleItemRequest request = new SaleItemRequest("8690000000002", 2);
        when(drugRepository.findById("8690000000002")).thenReturn(Optional.of(rxDrug));
        when(purchaseRepository.findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc(
                "8690000000002", 0)).thenReturn(List.of(rxBatch));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        Sale sale = saleService.createSale(List.of(request), true, null, sampleUser);

        assertNotNull(sale);
        assertTrue(sale.getIsPrescriptionLogged());
    }

    @Test
    void testCreateSaleWithCustomer() {
        Customer customer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .balance(BigDecimal.ZERO)
                .isActive(true)
                .build();

        SaleItemRequest request = new SaleItemRequest("8690000000001", 1);
        when(drugRepository.findById("8690000000001")).thenReturn(Optional.of(sampleDrug));
        when(purchaseRepository.findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc(
                "8690000000001", 0)).thenReturn(List.of(batchOld));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Sale sale = saleService.createSale(List.of(request), false, 1L, sampleUser);

        assertNotNull(sale.getCustomer());
        assertEquals(new BigDecimal("45.50"), customer.getBalance());
        verify(customerRepository, times(1)).save(customer);
    }
}
