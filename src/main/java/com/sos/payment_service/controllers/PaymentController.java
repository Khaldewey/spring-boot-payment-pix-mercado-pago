package com.sos.payment_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sos.payment_service.models.PaymentRequest;
import com.sos.payment_service.services.MercadoPagoService;


@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private final MercadoPagoService mercadoPagoService;

    public PaymentController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    // Endpoint para consultar os detalhes de um pagamento
    @GetMapping("/{paymentId}")
    public ResponseEntity<String> getPaymentDetails(@PathVariable String paymentId) {
        String paymentDetails = mercadoPagoService.fetchPaymentDetails(paymentId);
        
        if (paymentDetails != null) {
            return ResponseEntity.ok(paymentDetails);
        } else {
            return ResponseEntity.status(404).body("Pagamento não encontrado");
        }
    }

    // Endpoint para criar o pagamento Pix
    @PostMapping("/create-pix")
    public ResponseEntity<String> createPixPayment(@RequestBody PaymentRequest paymentRequest) {
        if (paymentRequest.getAmount() <= 0) {
            return ResponseEntity.badRequest().body("O valor do pagamento deve ser maior que 0");
        }

        String qrCodeBase64 = mercadoPagoService.createPixPayment(paymentRequest.getAmount());
        
        if (qrCodeBase64 != null) {
            return ResponseEntity.ok(qrCodeBase64); // Retorna o QR code em base64
        } else {
            return ResponseEntity.status(500).body("Erro ao criar pagamento Pix");
        }
    }
}
