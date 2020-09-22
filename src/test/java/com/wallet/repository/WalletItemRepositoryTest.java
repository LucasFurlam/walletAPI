package com.wallet.repository;

import com.wallet.entity.Wallet;
import com.wallet.entity.WalletItem;
import com.wallet.util.enums.TypeEnum;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class WalletItemRepositoryTest {

    public static final Date DATE = new Date();
    private static final TypeEnum TYPE = TypeEnum.EN;
    private static final String DESCRIPTION = "Conta de Luz";
    private static final BigDecimal VALUE = BigDecimal.valueOf(65);
    private Long savedWalletItemID = null;
    private Long savedWalletId = null;

    @Autowired
    WalletItemRepository repository;

    @Autowired
    WalletRepository walletRepository;

    @Before
    public void setUp() {
        Wallet wallet = new Wallet();
        wallet.setName("Carteira Teste");
        wallet.setValue(BigDecimal.valueOf(250));
        walletRepository.save(wallet);

        WalletItem walletItem = new WalletItem(null, wallet, DATE, TYPE, DESCRIPTION, VALUE);
        repository.save(walletItem);

        savedWalletItemID = walletItem.getId();
        savedWalletId = wallet.getId();
    }

    @After
    public void tearDown() {
        repository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    public void testSave() {
        Wallet wallet = new Wallet();
        wallet.setName("Carteira 1");
        wallet.setValue(BigDecimal.valueOf(500));
        walletRepository.save(wallet);

        WalletItem walletItem = new WalletItem(1L, wallet, DATE, TYPE, DESCRIPTION, VALUE);
        WalletItem response = repository.save(walletItem);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getDescription(), DESCRIPTION);
        Assert.assertEquals(response.getType(), TYPE);
        Assert.assertEquals(response.getValue(), VALUE);
        Assert.assertEquals(response.getWallet().getId(), wallet.getId());
    }

    @Test(expected = ConstraintViolationException.class)
    public void testSaveInvalidWalletItem() {
        WalletItem walletItem = new WalletItem(null, null, DATE, null, DESCRIPTION, null);
        repository.save(walletItem);
    }

    @Test
    public void testUpdate() {
        Optional<WalletItem> walletItem = repository.findById(savedWalletItemID);

        String description = "Descrição alterada";

        WalletItem changed = walletItem.get();
        changed.setDescription(description);

        repository.save(changed);

        Optional<WalletItem> newWalletItem = repository.findById(savedWalletItemID);

        Assert.assertEquals(description, newWalletItem.get().getDescription());
    }

    @Test
    public void deleteWalletItem() {
        Optional<Wallet> wallet = walletRepository.findById(savedWalletId);
        WalletItem walletItem = new WalletItem(null, wallet.get(), DATE, TYPE, DESCRIPTION, VALUE);

        repository.save(walletItem);

        repository.deleteById(walletItem.getId());

        Optional<WalletItem> response = repository.findById(walletItem.getId());

        Assert.assertFalse(response.isPresent());
    }

    @Test
    public void testFindBetweenDates() {
        Optional<Wallet> wallet = walletRepository.findById(savedWalletId);

        LocalDateTime localDateTime = DATE.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        Date currentDatePlusFiveDays = Date.from(localDateTime.plusDays(5).atZone(ZoneId.systemDefault()).toInstant());
        Date currentDatePlusSevenDays = Date.from(localDateTime.plusDays(7).atZone(ZoneId.systemDefault()).toInstant());

        repository.save(new WalletItem(null, wallet.get(), currentDatePlusFiveDays, TYPE, DESCRIPTION, VALUE));
        repository.save(new WalletItem(null, wallet.get(), currentDatePlusSevenDays, TYPE, DESCRIPTION, VALUE));

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<WalletItem> response = repository.findAllByWalletIdAndDateGreaterThanEqualAndDateLessThanEqual(savedWalletId, DATE, currentDatePlusSevenDays, pageRequest);

        Assert.assertEquals(response.getContent().size(), 2);
        Assert.assertEquals(response.getTotalElements(), 2);
        Assert.assertEquals(response.getContent().get(0).getWallet().getId(), savedWalletId);
    }

    @Test
    public void testFindByType() {
        List<WalletItem> response = repository.findByWalletIdAndType(savedWalletId, TYPE);

        Assert.assertEquals(response.size(), 1);
        Assert.assertEquals(response.get(0).getType(), TYPE);
    }

    @Test
    public void testFindByTypeSd() {
        Optional<Wallet> wallet = walletRepository.findById(savedWalletId);

        repository.save(new WalletItem(null, wallet.get(), DATE, TypeEnum.SD, DESCRIPTION, VALUE));

        List<WalletItem> response = repository.findByWalletIdAndType(savedWalletId, TypeEnum.SD);

        Assert.assertEquals(response.size(), 1);
        Assert.assertEquals(response.get(0).getType(), TypeEnum.SD);
    }

    @Test
    public void testSumByWallet() {
        Optional<Wallet> wallet = walletRepository.findById(savedWalletId);

        repository.save(new WalletItem(null, wallet.get(), DATE, TYPE, DESCRIPTION, VALUE));

        BigDecimal response = repository.sumByWalletId(savedWalletId);

        Assert.assertEquals(response.compareTo(BigDecimal.valueOf(130)), 0);
    }
}
