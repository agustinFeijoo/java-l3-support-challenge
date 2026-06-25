package com.example.demo.service;

import com.example.demo.dto.TransactionRequestDTO;
import com.example.demo.dto.TransactionResponseDTO;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Transaction;
import com.example.demo.enums.TransactionStatus;
import com.example.demo.exception.CustomerNotFoundException;
import com.example.demo.exception.RefundAlreadyExistsException;
import com.example.demo.exception.TransactionNotFoundException;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PaymentService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public PaymentService(CustomerRepository customerRepository,
                          TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponseDTO processPayment(TransactionRequestDTO request) {

        String maskedCard = maskCard(request.getCreditCardNumber());

        log.info(
                "Iniciando procesamiento de pago. Tarjeta: {}, Monto: {}",
                maskedCard,
                request.getAmount()
        );

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() ->
                        new CustomerNotFoundException(request.getCustomerId()));

        Transaction tx = new Transaction();
        tx.setAmount(request.getAmount());
        tx.setCreditCardNumber(request.getCreditCardNumber());
        tx.setStatus(TransactionStatus.APPROVED);
        tx.setCustomer(customer);

        transactionRepository.save(tx);

        return new TransactionResponseDTO(
                tx.getId(),
                tx.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findByIdWithTransactions(id)
                .orElseThrow(() ->
                        new CustomerNotFoundException(id));
    }

    @Transactional
    public TransactionResponseDTO refund(Long transactionId) {

        Transaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new TransactionNotFoundException(transactionId));

        if (!TransactionStatus.APPROVED.equals(original.getStatus())) {
            throw new IllegalStateException(
                    "Solo se pueden devolver transacciones APPROVED");
        }

        if (transactionRepository.existsByOriginalTransactionId(transactionId)) {
            throw new RefundAlreadyExistsException(transactionId);
        }

        Transaction refund = new Transaction();

        refund.setCustomer(original.getCustomer());
        refund.setAmount(original.getAmount().negate());
        refund.setStatus(TransactionStatus.REFUNDED);
        refund.setCreditCardNumber(original.getCreditCardNumber());
        refund.setOriginalTransaction(original);

        transactionRepository.save(refund);

        return new TransactionResponseDTO(
                refund.getId(),
                refund.getStatus()
        );
    }

    private String maskCard(String cardNumber) {

        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        return "************" +
                cardNumber.substring(cardNumber.length() - 4);
    }
}