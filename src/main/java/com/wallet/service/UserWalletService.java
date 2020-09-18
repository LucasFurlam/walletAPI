package com.wallet.service;

import com.wallet.entity.UserWallet;
import org.springframework.stereotype.Service;

public interface UserWalletService {

    UserWallet save(UserWallet userWallet);
}
