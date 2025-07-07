package com.fintech.api.service;
import org.springframework.stereotype.Service;

import com.fintech.api.domain.Bank;
import com.fintech.api.repository.BankRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BankService {

    private final BankRepository bankRepository;

    public List<Bank> getAllBanks() {
        return bankRepository.findAll();
    }

    public Optional<Bank> getBankById(Long id) {
        return bankRepository.findById(id);
    }

    public Bank createBank(Bank bank) {
        return bankRepository.save(bank);
    }

    public void deleteBank(Long id) {
        bankRepository.deleteById(id);
    }

    public Optional<Bank> getBankByName (String bankName) {
        return bankRepository.findByBankName(bankName);
    }
}
