package com.pharmacy.config;

import com.pharmacy.model.*;
import com.pharmacy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ACİL DURUM VERİ SEEDER'I
// Sunum anında veya lokal testlerde veritabanı tamamen sıfırlanırsa,
// aşağıdaki '// @Component' satırının başındaki yorum satırını kaldırarak
// sistemi saniyeler içinde zengin sunum verileriyle doldurabilirsiniz.
// @Component
@Order(1)
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final PresTypeRepository presTypeRepository;
    private final DrugRepository drugRepository;
    private final PurchaseRepository purchaseRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        saleRepository.deleteAll();
        userRepository.deleteAll();

        seedUsers();
        seedPrescriptionTypes();
        seedBrands();
        seedCategories();
        seedDrugsAndBatches();
        seedCustomers();
        seedSalesHistory();
    }

    private void seedUsers() {
        userRepository.deleteAll();

        User admin = User.builder()
                .name("Admin Yönetici")
                .username("admin")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .isActive(true)
                .build();
        userRepository.save(admin);

        User pharmacist1 = User.builder()
                .name("Ecz. Ayşe Yılmaz")
                .username("eczaci_ayse")
                .password(passwordEncoder.encode("password123"))
                .role(Role.PHARMACIST)
                .isActive(true)
                .build();
        userRepository.save(pharmacist1);

        User pharmacist2 = User.builder()
                .name("Ecz. Mehmet Demir")
                .username("eczaci_mehmet")
                .password(passwordEncoder.encode("password123"))
                .role(Role.PHARMACIST)
                .isActive(true)
                .build();
        userRepository.save(pharmacist2);

        User cashier1 = User.builder()
                .name("Kasiyer Veli Kaya")
                .username("kasiyer_veli")
                .password(passwordEncoder.encode("password123"))
                .role(Role.CASHIER)
                .isActive(true)
                .build();
        userRepository.save(cashier1);

        User cashier2 = User.builder()
                .name("Kasiyer Zeynep Öztürk")
                .username("kasiyer_zeynep")
                .password(passwordEncoder.encode("password123"))
                .role(Role.CASHIER)
                .isActive(true)
                .build();
        userRepository.save(cashier2);
    }

    private void seedPrescriptionTypes() {
        if (presTypeRepository.count() > 0) return;

        presTypeRepository.save(PresType.builder().name("White").riskLevel(1).build());
        presTypeRepository.save(PresType.builder().name("Orange").riskLevel(2).build());
        presTypeRepository.save(PresType.builder().name("Purple").riskLevel(2).build());
        presTypeRepository.save(PresType.builder().name("Green").riskLevel(3).build());
        presTypeRepository.save(PresType.builder().name("Red").riskLevel(4).build());
    }

    private void seedBrands() {
        if (brandRepository.count() > 0) return;

        brandRepository.save(Brand.builder().name("Pfizer").isActive(true).build());
        brandRepository.save(Brand.builder().name("Novartis").isActive(true).build());
        brandRepository.save(Brand.builder().name("Bayer").isActive(true).build());
        brandRepository.save(Brand.builder().name("Sanofi").isActive(true).build());
        brandRepository.save(Brand.builder().name("Abdi İbrahim").isActive(true).build());
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) return;

        categoryRepository.save(Category.builder().name("Antibiyotikler").isActive(true).build());
        categoryRepository.save(Category.builder().name("Ağrı Kesiciler").isActive(true).build());
        categoryRepository.save(Category.builder().name("Antiviral İlaçlar").isActive(true).build());
        categoryRepository.save(Category.builder().name("Kardiyovasküler").isActive(true).build());
        categoryRepository.save(Category.builder().name("Gastrointestinal").isActive(true).build());
        categoryRepository.save(Category.builder().name("Psikiyatrik İlaçlar").isActive(true).build());
    }

    private void seedDrugsAndBatches() {
        if (drugRepository.count() > 0) return;

        List<Brand> brands = brandRepository.findAll();
        List<Category> categories = categoryRepository.findAll();
        List<PresType> presTypes = presTypeRepository.findAll();

        PresType white = findByName(presTypes, "White");
        PresType orange = findByName(presTypes, "Orange");
        PresType purple = findByName(presTypes, "Purple");
        PresType green = findByName(presTypes, "Green");
        PresType red = findByName(presTypes, "Red");

        Brand pfizer = findBrand(brands, "Pfizer");
        Brand novartis = findBrand(brands, "Novartis");
        Brand bayer = findBrand(brands, "Bayer");
        Brand sanofi = findBrand(brands, "Sanofi");
        Brand abdi = findBrand(brands, "Abdi İbrahim");

        Category antibiotics = findCat(categories, "Antibiyotikler");
        Category painkillers = findCat(categories, "Ağrı Kesiciler");
        Category antivirals = findCat(categories, "Antiviral İlaçlar");
        Category cardio = findCat(categories, "Kardiyovasküler");
        Category gastro = findCat(categories, "Gastrointestinal");
        Category psych = findCat(categories, "Psikiyatrik İlaçlar");

        Drug drug1 = createDrugAndBatches("8699514010686", "Parol 500 mg Tablet", painkillers, abdi, white, new BigDecimal("45.50"), 100);
        Drug drug2 = createDrugAndBatches("8699504090564", "Augmentin 1000 mg BID", antibiotics, bayer, white, new BigDecimal("125.00"), 30);
        Drug drug3 = createDrugAndBatches("8699532040034", "Xanax 0.5 mg Tablet", psych, pfizer, green, new BigDecimal("85.50"), 15);
        Drug drug4 = createDrugAndBatches("8699593090044", "Concerta 36 mg Tablet", psych, novartis, red, new BigDecimal("420.00"), 10);
        Drug drug5 = createDrugAndBatches("8699809090038", "Pharmaton Vitality 30 Kapsül", painkillers, sanofi, white, new BigDecimal("350.00"), 20);
        Drug drug6 = createDrugAndBatches("8699508090409", "Beloc Zok 50 mg Tablet", cardio, novartis, white, new BigDecimal("95.25"), 40);
        Drug drug7 = createDrugAndBatches("8699522010045", "Zinnat 500 mg Tablet", antibiotics, bayer, orange, new BigDecimal("210.00"), 20);
        Drug drug8 = createDrugAndBatches("8699544010072", "Lipitor 20 mg Tablet", cardio, pfizer, white, new BigDecimal("175.50"), 50);
        Drug drug9 = createDrugAndBatches("8699566010098", "Nexium 40 mg Tablet", gastro, sanofi, white, new BigDecimal("290.00"), 15);
        Drug drug10 = createDrugAndBatches("8699588010124", "Ritalin 10 mg Tablet", psych, novartis, red, new BigDecimal("380.00"), 12);
        Drug drug11 = createDrugAndBatches("8699610010155", "Tamiflu 75 mg Kapsül", antivirals, pfizer, green, new BigDecimal("520.00"), 8);
        Drug drug12 = createDrugAndBatches("8699632010186", "Voltaren 50 mg Tablet", painkillers, novartis, white, new BigDecimal("65.00"), 60);
        Drug drug13 = createDrugAndBatches("8699654010216", "Cipro 500 mg Tablet", antibiotics, bayer, white, new BigDecimal("185.00"), 25);
        Drug drug14 = createDrugAndBatches("8699676010247", "Risperdal 2 mg Tablet", psych, pfizer, purple, new BigDecimal("450.00"), 10);
        Drug drug15 = createDrugAndBatches("8699698010278", "Gaviscon Advance Likit", gastro, abdi, white, new BigDecimal("78.90"), 35);

        drugRepository.saveAll(List.of(drug1, drug2, drug3, drug4, drug5, drug6, drug7, drug8, drug9, drug10, drug11, drug12, drug13, drug14, drug15));
    }

    private Drug createDrugAndBatches(String barcode, String name, Category category, Brand brand, PresType presType,
                                       BigDecimal sellingPrice, int minStockAlert) {
        Drug drug = Drug.builder()
                .barcode(barcode)
                .name(name)
                .category(category)
                .brand(brand)
                .presType(presType)
                .currentSellingPrice(sellingPrice)
                .minStockAlert(minStockAlert)
                .isActive(true)
                .build();

        LocalDate expiredDate = LocalDate.of(2026, 5, 28);
        LocalDate criticalDate = LocalDate.of(2026, 6, 17);
        LocalDate safeDate = LocalDate.of(2027, 3, 15);

        BigDecimal cost1 = sellingPrice.multiply(new BigDecimal("0.55"));
        BigDecimal cost2 = sellingPrice.multiply(new BigDecimal("0.62"));
        BigDecimal cost3 = sellingPrice.multiply(new BigDecimal("0.58"));

        Purchase batch1 = Purchase.builder()
                .drug(drug)
                .originalQuantity(30)
                .remainingQuantity(30)
                .purchasePrice(cost1)
                .expirationDate(expiredDate)
                .purchaseDate(LocalDate.now().minusDays(60))
                .build();

        Purchase batch2 = Purchase.builder()
                .drug(drug)
                .originalQuantity(50)
                .remainingQuantity(50)
                .purchasePrice(cost2)
                .expirationDate(criticalDate)
                .purchaseDate(LocalDate.now().minusDays(30))
                .build();

        Purchase batch3 = Purchase.builder()
                .drug(drug)
                .originalQuantity(200)
                .remainingQuantity(200)
                .purchasePrice(cost3)
                .expirationDate(safeDate)
                .purchaseDate(LocalDate.now().minusDays(5))
                .build();

        purchaseRepository.saveAll(List.of(batch1, batch2, batch3));
        return drug;
    }

    private void seedCustomers() {
        if (customerRepository.count() > 0) return;

        customerRepository.save(Customer.builder().name("Ahmet Yılmaz").phone("5551234567").balance(new BigDecimal("1250.50")).isActive(true).build());
        customerRepository.save(Customer.builder().name("Ayşe Demir").phone("5557654321").balance(new BigDecimal("340.00")).isActive(true).build());
        customerRepository.save(Customer.builder().name("Mehmet Kaya").phone("5559876543").balance(new BigDecimal("2780.75")).isActive(true).build());
        customerRepository.save(Customer.builder().name("Fatma Şahin").phone("5553456789").balance(new BigDecimal("520.25")).isActive(true).build());
        customerRepository.save(Customer.builder().name("Ali Özdemir").phone("5551122334").balance(new BigDecimal("890.00")).isActive(true).build());
    }

    private void seedSalesHistory() {
        if (saleRepository.count() > 0) return;

        List<User> users = userRepository.findAll();
        List<Customer> customers = customerRepository.findAll();
        List<Purchase> purchases = purchaseRepository.findAll();

        User cashier1 = users.stream().filter(u -> "kasiyer_veli".equals(u.getUsername())).findFirst().orElse(users.get(0));
        User cashier2 = users.stream().filter(u -> "kasiyer_zeynep".equals(u.getUsername())).findFirst().orElse(users.get(0));
        User pharm = users.stream().filter(u -> "eczaci_ayse".equals(u.getUsername())).findFirst().orElse(users.get(0));

        Customer ahmet = customers.stream().filter(c -> c.getName().contains("Ahmet")).findFirst().orElse(customers.get(0));
        Customer ayse = customers.stream().filter(c -> c.getName().contains("Ayşe")).findFirst().orElse(customers.get(0));
        Customer mehmet = customers.stream().filter(c -> c.getName().contains("Mehmet")).findFirst().orElse(customers.get(0));
        Customer fatma = customers.stream().filter(c -> c.getName().contains("Fatma")).findFirst().orElse(customers.get(0));
        Customer ali = customers.stream().filter(c -> c.getName().contains("Ali")).findFirst().orElse(customers.get(0));

        createSale(cashier1, ahmet, true, LocalDateTime.now().minusDays(5), purchases, 0, 3, 1);
        createSale(cashier2, ayse, false, LocalDateTime.now().minusDays(4), purchases, 3, 2, 1);
        createSale(pharm, mehmet, true, LocalDateTime.now().minusDays(3), purchases, 6, 4, 2);
        createSale(cashier1, fatma, false, LocalDateTime.now().minusDays(2), purchases, 9, 1, 1);
        createSale(cashier2, ali, true, LocalDateTime.now().minusDays(1), purchases, 12, 2, 2);
        createSale(pharm, ahmet, false, LocalDateTime.now().minusHours(6), purchases, 15, 1, 1);
        createSale(cashier1, mehmet, false, LocalDateTime.now().minusHours(3), purchases, 30, 2, 1);
        createSale(cashier2, null, false, LocalDateTime.now().minusHours(1), purchases, 18, 3, 3);
    }

    private void createSale(User user, Customer customer, boolean prescriptionLogged, LocalDateTime date,
                            List<Purchase> allPurchases, int purchaseStartIdx, int itemCount, int qtyEach) {
        Sale sale = Sale.builder()
                .customer(customer)
                .user(user)
                .saleDate(date)
                .isPrescriptionLogged(prescriptionLogged)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < itemCount && (purchaseStartIdx + i) < allPurchases.size(); i++) {
            Purchase batch = allPurchases.get(purchaseStartIdx + i);
            int qty = Math.min(qtyEach, batch.getRemainingQuantity());
            if (qty <= 0) continue;

            batch.setRemainingQuantity(batch.getRemainingQuantity() - qty);
            purchaseRepository.save(batch);

            Drug drug = batch.getDrug();
            SaleItem item = SaleItem.builder()
                    .sale(sale)
                    .purchase(batch)
                    .quantity(qty)
                    .unitPrice(drug.getCurrentSellingPrice())
                    .build();
            sale.getItems().add(item);
            total = total.add(drug.getCurrentSellingPrice().multiply(BigDecimal.valueOf(qty)));
        }

        sale.setTotalAmount(total);
        saleRepository.save(sale);

        if (customer != null) {
            customer.setBalance(customer.getBalance().add(total));
            customerRepository.save(customer);
        }
    }

    private PresType findByName(List<PresType> list, String name) {
        return list.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    private Brand findBrand(List<Brand> list, String name) {
        return list.stream().filter(b -> b.getName().equals(name)).findFirst().orElse(null);
    }

    private Category findCat(List<Category> list, String name) {
        return list.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }
}
