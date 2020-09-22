package com.wallet.service.impl;

import com.wallet.entity.UserWallet;
import com.wallet.repository.UserWalletRepository;
import com.wallet.service.UserWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserWalletServiceImpl implements UserWalletService {

    @Autowired
    UserWalletRepository repository;

    @Override
    public UserWallet save(UserWallet userWallet) {
        return repository.save(userWallet);
    }

    @Override
    public Optional<UserWallet> findByUsersIdAndWalletId(Long user, Long wallet) {
        return repository.findByUsersIdAndWalletId(user, wallet);
    }

}
