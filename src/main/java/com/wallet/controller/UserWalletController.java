package com.wallet.controller;

import com.wallet.dto.UserWalletDTO;
import com.wallet.entity.User;
import com.wallet.entity.UserWallet;
import com.wallet.entity.Wallet;
import com.wallet.response.Response;
import com.wallet.service.UserWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("user-wallet")
public class UserWalletController {

    @Autowired
    UserWalletService service;

    @PostMapping
    public ResponseEntity<Response<UserWalletDTO>> create(@Valid @RequestBody UserWalletDTO userWalletDTO, BindingResult result) {

        Response<UserWalletDTO> response = new Response<UserWalletDTO>();

        if (result.hasErrors()) {
            result.getAllErrors().forEach(errors -> response.getErrors().add(errors.getDefaultMessage()));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        UserWallet userWallet = service.save(this.convertDtoToEntity(userWalletDTO));

        response.setData(this.convertEntityToDto(userWallet));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public UserWallet convertDtoToEntity(UserWalletDTO userWalletDTO) {
        UserWallet userWallet = new UserWallet();

        User user = new User();
        user.setId(userWalletDTO.getUsers());

        Wallet wallet = new Wallet();
        wallet.setId(userWalletDTO.getWallet());

        userWallet.setId(userWalletDTO.getId());
        userWallet.setUsers(user);
        userWallet.setWallet(wallet);

        return userWallet;
    }

    public UserWalletDTO convertEntityToDto(UserWallet userWallet) {
        UserWalletDTO userWalletDTO = new UserWalletDTO();
        userWalletDTO.setId(userWallet.getId());
        userWalletDTO.setUsers(userWallet.getUsers().getId());
        userWalletDTO.setWallet(userWallet.getWallet().getId());

        return userWalletDTO;
    }
}
