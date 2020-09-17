package com.wallet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.dto.UserDTO;
import com.wallet.entity.User;
import com.wallet.service.UserService;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.print.attribute.standard.MediaSize;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    private static final Long ID = 1L;
    private static final String EMAIL = "email@teste.com";
    private static final String NAME = "User Test";
    private static final String PASSWORD = "123456";
    private static final String URL = "/user";

    @MockBean
    UserService service;

    @Autowired
    MockMvc mvc;

    @Test
    public void testSave() throws Exception {

        BDDMockito.given(service.save(Mockito.any(User.class))).willReturn(getMockUser());
        mvc.perform(MockMvcRequestBuilders.post(URL).content(getJsonPayload(ID, EMAIL, NAME, PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(EMAIL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.password").value(PASSWORD));

    }

    @Test
    public void testSaveInvalidUser() throws JsonProcessingException, Exception {

        mvc.perform(MockMvcRequestBuilders.post(URL).content(getJsonPayload(ID, "email", NAME, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Email inválido"));
    }

    public User getMockUser() {
        User user = new User();
        user.setId(ID);
        user.setEmail(EMAIL);
        user.setName(NAME);
        user.setPassword(PASSWORD);

        return user;
    }

    public String getJsonPayload(Long id, String email, String name, String passaword) throws JsonProcessingException {
        UserDTO userDto = new UserDTO();
        userDto.setEmail(email);
        userDto.setName(name);
        userDto.setPassword(passaword);
        userDto.setId(id);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(userDto);
    }
}
