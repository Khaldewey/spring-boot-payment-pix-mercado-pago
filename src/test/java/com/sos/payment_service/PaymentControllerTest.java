package com.sos.payment_service;

import com.sos.payment_service.controllers.PaymentController;
import com.sos.payment_service.models.PaymentRequest;
import com.sos.payment_service.services.MercadoPagoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MercadoPagoService mercadoPagoService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build(); // Configura o MockMvc
    }

    @Test
    void testGetPaymentDetails_Success() throws Exception {
        // Simula o retorno do serviço MercadoPagoService
        String paymentId = "12345";
        String paymentDetails = "{\"status\":\"approved\"}";
        when(mercadoPagoService.fetchPaymentDetails(paymentId)).thenReturn(paymentDetails);

        // Verifica se o método retorna o status 200 com o corpo correto
        mockMvc.perform(get("/api/payments/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(content().string(paymentDetails));
    }

    @Test
    void testGetPaymentDetails_NotFound() throws Exception {
        // Simula o retorno nulo do serviço MercadoPagoService
        String paymentId = "12345";
        when(mercadoPagoService.fetchPaymentDetails(paymentId)).thenReturn(null);

        // Verifica se o método retorna o status 404 com a mensagem de erro
        mockMvc.perform(get("/api/payments/{paymentId}", paymentId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Pagamento não encontrado"));
    }

    @Test
    void testCreatePixPayment_Success() throws Exception {
        // Cria o objeto PaymentRequest
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(100.0);

        // Simula o retorno do serviço MercadoPagoService
        String qrCodeBase64 = "qrCodeData";
        when(mercadoPagoService.createPixPayment(paymentRequest.getAmount())).thenReturn(qrCodeBase64);

        // Verifica se o método retorna o status 200 com o QR Code em base64
        mockMvc.perform(post("/api/payments/create-pix")
                        .contentType("application/json")
                        .content("{\"amount\": 100.0}"))
                .andExpect(status().isOk())
                .andExpect(content().string(qrCodeBase64));
    }

    @Test
    void testCreatePixPayment_BadRequest() throws Exception {
        // Cria o objeto PaymentRequest com valor inválido
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(0.0);

        // Verifica se o método retorna o status 400 com a mensagem de erro
        mockMvc.perform(post("/api/payments/create-pix")
                        .contentType("application/json")
                        .content("{\"amount\": 0.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("O valor do pagamento deve ser maior que 0"));
    }

    @Test
    void testCreatePixPayment_InternalServerError() throws Exception {
        // Cria o objeto PaymentRequest
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(100.0);

        // Simula uma falha no serviço MercadoPagoService
        when(mercadoPagoService.createPixPayment(paymentRequest.getAmount())).thenReturn(null);

        // Verifica se o método retorna o status 500 com a mensagem de erro
        mockMvc.perform(post("/api/payments/create-pix")
                        .contentType("application/json")
                        .content("{\"amount\": 100.0}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro ao criar pagamento Pix"));
    }
}

