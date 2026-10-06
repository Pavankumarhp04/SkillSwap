package com.qsp.skillswap_backend_project.dto;

import java.util.List;

public record WalletResponse(int balance, List<TransactionResponse> transactions) {

}
