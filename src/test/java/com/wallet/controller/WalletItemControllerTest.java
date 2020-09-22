package com.wallet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.dto.WalletItemDTO;
import com.wallet.entity.User;
import com.wallet.entity.UserWallet;
import com.wallet.entity.Wallet;
import com.wallet.entity.WalletItem;
import com.wallet.service.UserService;
import com.wallet.service.UserWalletService;
import com.wallet.service.WalletItemService;
import com.wallet.util.enums.TypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WalletItemControllerTest {

    @MockBean
    WalletItemService service;

    @MockBean
    UserWalletService userWalletService;

    @MockBean
    UserService userService;

    @Autowired
    MockMvc mvc;

    private static final Long ID = 1L;
    public static final Date DATE = new Date();
    public static final LocalDate TODAY = LocalDate.now();
    public static final TypeEnum TYPE = TypeEnum.EN;
    public static final String DESCRIPTION = "Conta de Luz";
    public static final BigDecimal VALUE = BigDecimal.valueOf(65);
    public static final String URL = "/wallet-item";

    @Test
    @WithMockUser
    public void testSave() throws Exception {

        BDDMockito.given(service.save(Mockito.any(WalletItem.class))).willReturn(getMockWalletItem());

        mvc.perform(MockMvcRequestBuilders.post(URL).content(getJsonPayload())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.date").value(TODAY.format(getDateFormater())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.description").value(DESCRIPTION))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.type").value(TYPE.getValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.value").value(VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.wallet").value(ID));
    }

    @Test
    @WithMockUser
    public void testFindBetweenDates() throws Exception {
        List<WalletItem> list = new ArrayList<>();
        list.add(getMockWalletItem());
        Page<WalletItem> page = new PageImpl<>(list);

        String startDate = TODAY.format(getDateFormater());
        String endDate = TODAY.plusDays(5).format(getDateFormater());

        User user = new User();
        user.setId(1L);

        BDDMockito.given(service.findBetweenDates(Mockito.anyLong(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).willReturn(page);
        BDDMockito.given(userService.findByEmail(Mockito.anyString())).willReturn(Optional.of(user));
        BDDMockito.given(userWalletService.findByUsersIdAndWalletId(Mockito.anyLong(), Mockito.anyLong())).willReturn(Optional.of(new UserWallet()));

        mvc.perform(MockMvcRequestBuilders.get(URL + "/1?startDate=" + startDate + "&endDate=" + endDate)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].id").value(ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].date").value(TODAY.format(getDateFormater())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].description").value(DESCRIPTION))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].type").value(TYPE.getValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].value").value(VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].wallet").value(ID));
    }

    @Test
    @WithMockUser
    public void testFindByType() throws Exception {
        List<WalletItem> list = new ArrayList<>();
        list.add(getMockWalletItem());

        BDDMockito.given(service.findByWalletAndType(Mockito.anyLong(), Mockito.any(TypeEnum.class))).willReturn(list);

        mvc.perform(MockMvcRequestBuilders.get(URL + "/type/1?type=ENTRADA")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.[0].id").value(ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.[0].date").value(TODAY.format(getDateFormater())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.[0].description").value(DESCRIPTION))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.[0].type").value(TYPE.getValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.[0].value").value(VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.[0].wallet").value(ID));
    }

    @Test
    @WithMockUser
    public void testSumByWallet() throws Exception {
        BigDecimal value = BigDecimal.valueOf(536.90);

        BDDMockito.given(service.sumByWalletId(Mockito.anyLong())).willReturn(value);

        mvc.perform(MockMvcRequestBuilders.get(URL + "/total/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value("536.9"));
    }

    @Test
    @WithMockUser
    public void testUpdate() throws Exception {
        String description = "Nova descrição";
        Wallet wallet = new Wallet();
        wallet.setId(ID);

        BDDMockito.given(service.findById(Mockito.anyLong())).willReturn(Optional.of(getMockWalletItem()));
        BDDMockito.given(service.save(Mockito.any(WalletItem.class))).willReturn(new WalletItem(1L, wallet, DATE, TypeEnum.SD, description, VALUE));

        mvc.perform(MockMvcRequestBuilders.put(URL).content(getJsonPayload())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.date").value(TODAY.format(getDateFormater())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.description").value(description))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.type").value(TypeEnum.SD.getValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.value").value(VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.wallet").value(ID));
    }

    @Test
    @WithMockUser
    public void testUpdateWalletChanged() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setId(99L);

        WalletItem walletItem = new WalletItem(1L, wallet, DATE, TypeEnum.SD, DESCRIPTION, VALUE);

        BDDMockito.given(service.findById(Mockito.anyLong())).willReturn(Optional.of(walletItem));

        mvc.perform(MockMvcRequestBuilders.put(URL).content(getJsonPayload())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Você não pode alterar a carteira"));
    }

    @Test
    @WithMockUser
    public void testUpdateInvalidId() throws Exception {
        BDDMockito.given(service.findById(Mockito.anyLong())).willReturn(Optional.empty());

        mvc.perform(MockMvcRequestBuilders.put(URL).content(getJsonPayload())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("WalletItem não encontrado"));
    }

    @Test
    @WithMockUser
    public void testDelete() throws JsonProcessingException, Exception {
        BDDMockito.given(service.findById(Mockito.anyLong())).willReturn(Optional.of(new WalletItem()));

        mvc.perform(MockMvcRequestBuilders.delete(URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value("WalletItem de id " + ID + " apagada com sucesso"));
    }

    @Test
    @WithMockUser
    public void testDeleteInvalid() throws Exception {
        BDDMockito.given(service.findById(Mockito.anyLong())).willReturn(Optional.empty());

        mvc.perform(MockMvcRequestBuilders.delete(URL + "/99")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("WalletItem de id " + 99 + " não encontrada"));
    }

    private WalletItem getMockWalletItem() {
        Wallet wallet = new Wallet();

        wallet.setId(1L);

        WalletItem walletItem = new WalletItem(1L, wallet, DATE, TYPE, DESCRIPTION, VALUE);

        return walletItem;
    }

    public String getJsonPayload() throws JsonProcessingException {
        WalletItemDTO walletItemDTO = new WalletItemDTO();
        walletItemDTO.setId(ID);
        walletItemDTO.setDate(DATE);
        walletItemDTO.setDescription(DESCRIPTION);
        walletItemDTO.setType(TYPE.getValue());
        walletItemDTO.setValue(VALUE);
        walletItemDTO.setWallet(ID);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(walletItemDTO);
    }

    private DateTimeFormatter getDateFormater() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return dateTimeFormatter;
    }
}
