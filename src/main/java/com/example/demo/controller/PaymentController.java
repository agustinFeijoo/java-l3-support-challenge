package com.example.demo.controller;

import com.example.demo.enums.CustomerSegment;
import com.example.demo.dto.CustomerSummaryDTO;
import com.example.demo.dto.RefundRequestDTO;
import com.example.demo.dto.TransactionRequestDTO;
import com.example.demo.dto.TransactionResponseDTO;
import com.example.demo.entity.Customer;
import com.example.demo.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<TransactionResponseDTO> process(
            @RequestBody TransactionRequestDTO request) {

        return ResponseEntity.ok(
                paymentService.processPayment(request)
        );
    }

    @PostMapping("/refund")
    public ResponseEntity<TransactionResponseDTO> refund(
            @RequestBody RefundRequestDTO request) {

        return ResponseEntity.ok(
                paymentService.refund(request.getTransactionId())
        );
    }

    @GetMapping("/customer/{id}/summary")
    public ResponseEntity<CustomerSummaryDTO> getCustomerSummary(
            @PathVariable Long id) {

        Customer customer = paymentService.getCustomerById(id);

        int totalTransactions = customer.getTransactions().size();

        return ResponseEntity.ok(
                new CustomerSummaryDTO(
                        customer.getName(),
                        totalTransactions
                )
        );
    }
}