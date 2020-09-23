package com.wallet.controller;

import com.wallet.dto.WalletItemDTO;
import com.wallet.entity.UserWallet;
import com.wallet.entity.Wallet;
import com.wallet.entity.WalletItem;
import com.wallet.response.Response;
import com.wallet.service.UserWalletService;
import com.wallet.service.WalletItemService;
import com.wallet.util.Util;
import com.wallet.util.enums.TypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("wallet-item")
public class WalletItemController {

    @Autowired
    private WalletItemService service;

    @Autowired
    private UserWalletService userWalletService;

    private static Logger log = LoggerFactory.getLogger(WalletItemController.class);

    @PostMapping
    public ResponseEntity<Response<WalletItemDTO>> create(@Valid @RequestBody WalletItemDTO walletItemDTO, BindingResult bindingResult) {

        Response<WalletItemDTO> response = new Response<>();

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(errors -> response.getErrors().add(errors.getDefaultMessage()));

            return ResponseEntity.badRequest().body(response);
        }

        WalletItem walletItem = service.save(this.convertDtoToEntity(walletItemDTO));

        response.setData(this.convertEntityToDto(walletItem));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/{wallet}")
    public ResponseEntity<Response<Page<WalletItemDTO>>> findBetweenDates(@PathVariable("wallet") Long wallet,
                                                                          @RequestParam("startDate") @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
                                                                          @RequestParam("endDate") @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate,
                                                                          @RequestParam(name = "page", defaultValue = "0") int page) {

        Response<Page<WalletItemDTO>> response = new Response<>();

        Optional<UserWallet> userWallet = userWalletService.findByUsersIdAndWalletId(Util.getAuthenticatedUserId(), wallet);

        if (!userWallet.isPresent()) {
            response.getErrors().add("Você não tem acesso a essa carteira");
            return ResponseEntity.badRequest().body(response);
        }

        Page<WalletItem> items = service.findBetweenDates(wallet, startDate, endDate, page);
        Page<WalletItemDTO> walletItemDTOS = items.map(it -> this.convertEntityToDto(it));
        response.setData(walletItemDTOS);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/type/{wallet}")
    public ResponseEntity<Response<List<WalletItemDTO>>> findByWalletIdAndType(@PathVariable("wallet") Long wallet, @RequestParam("type") String type) {

        log.info("Buscando por carteira {} e tipo {}", wallet, type);

        Response<List<WalletItemDTO>> response = new Response<>();
        List<WalletItem> list = service.findByWalletAndType(wallet, TypeEnum.getEnum(type));

        List<WalletItemDTO> walletItemDTOS = new ArrayList<>();
        list.forEach(it -> walletItemDTOS.add(this.convertEntityToDto(it)));
        response.setData(walletItemDTOS);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/total/{wallet}")
    public ResponseEntity<Response<BigDecimal>> sumByWalletId(@PathVariable("wallet") Long wallet) {
        Response<BigDecimal> response = new Response<>();
        BigDecimal value = service.sumByWalletId(wallet);
        response.setData(value == null ? BigDecimal.ZERO : value);

        return ResponseEntity.ok().body(response);
    }

    @PutMapping
    public ResponseEntity<Response<WalletItemDTO>> update(@Valid @RequestBody WalletItemDTO walletItemDTO, BindingResult bindingResult) {
        Response<WalletItemDTO> response = new Response<>();

        Optional<WalletItem> walletItem = service.findById(walletItemDTO.getId());

        if (!walletItem.isPresent()) {
            bindingResult.addError(new ObjectError("WalletItem", "WalletItem não encontrado"));
        } else if (walletItem.get().getWallet().getId().compareTo(walletItemDTO.getWallet()) != 0) {
            bindingResult.addError(new ObjectError("WalletItemChanged", "Você não pode alterar a carteira"));
        }

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(errors -> response.getErrors().add(errors.getDefaultMessage()));

            return ResponseEntity.badRequest().body(response);
        }

        WalletItem saved = service.save(this.convertDtoToEntity(walletItemDTO));

        response.setData(this.convertEntityToDto(saved));
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping(value = "/{walletItemId}")
    public ResponseEntity<Response<String>> delete(@PathVariable("walletItemId") Long walletItemId) {
        Response<String> response = new Response<>();

        Optional<WalletItem> walletItem = service.findById(walletItemId);

        if (!walletItem.isPresent()) {
            response.getErrors().add("WalletItem de id " + walletItemId + " não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        service.deleteById(walletItemId);
        response.setData("WalletItem de id " + walletItemId + " apagada com sucesso");
        return ResponseEntity.ok().body(response);
    }

    private WalletItem convertDtoToEntity(WalletItemDTO walletItemDTO) {
        WalletItem walletItem = new WalletItem();
        walletItem.setDate(walletItemDTO.getDate());
        walletItem.setDescription(walletItemDTO.getDescription());
        walletItem.setId(walletItemDTO.getId());
        walletItem.setType(TypeEnum.getEnum(walletItemDTO.getType()));
        walletItem.setValue(walletItemDTO.getValue());

        Wallet wallet = new Wallet();
        wallet.setId(walletItemDTO.getWallet());
        walletItem.setWallet(wallet);

        return walletItem;

    }

    private WalletItemDTO convertEntityToDto(WalletItem walletItem) {
        WalletItemDTO walletItemDTO = new WalletItemDTO();
        walletItemDTO.setDate(walletItem.getDate());
        walletItemDTO.setDescription(walletItem.getDescription());
        walletItemDTO.setId(walletItem.getId());
        walletItemDTO.setType(walletItem.getType().getValue());
        walletItemDTO.setValue(walletItem.getValue());
        walletItemDTO.setWallet(walletItem.getWallet().getId());

        return walletItemDTO;
    }
}
