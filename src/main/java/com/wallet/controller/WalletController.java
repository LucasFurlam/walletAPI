package com.wallet.controller;

import com.wallet.dto.WalletDTO;
import com.wallet.entity.Wallet;
import com.wallet.response.Response;
import com.wallet.service.WalletService;
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
@RequestMapping("wallet")
public class WalletController {

    @Autowired
    private WalletService service;

    @PostMapping
    public ResponseEntity<Response<WalletDTO>> create(@Valid @RequestBody WalletDTO walletDTO, BindingResult bindingResult) {

        Response<WalletDTO> response = new Response<WalletDTO>();

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(errors -> response.getErrors().add(errors.getDefaultMessage()));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Wallet wallet = service.save(this.convertDtoToEntity(walletDTO));

        response.setData(this.convertEntityToDto(wallet));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Wallet convertDtoToEntity(WalletDTO walletDTO) {
        Wallet wallet = new Wallet();
        wallet.setId(walletDTO.getId());
        wallet.setName(walletDTO.getName());
        wallet.setValue(walletDTO.getValue());

        return wallet;
    }

    private WalletDTO convertEntityToDto(Wallet wallet) {
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setId(wallet.getId());
        walletDTO.setName(wallet.getName());
        walletDTO.setValue(wallet.getValue());

        return walletDTO;
    }

}
