package com.sos.payment_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.payment_service.services.MercadoPagoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MercadoPagoServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MercadoPagoService mercadoPagoService;

    

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa os mocks
        ReflectionTestUtils.setField(mercadoPagoService, "accessToken", "fakeAccessToken12345"); // Simula o valor do accessToken
    }

    @Test
    void testCreatePixPayment_success() throws Exception {
        // Defina o comportamento esperado do RestTemplate e ObjectMapper
        String paymentResponse = "{\"point_of_interaction\": {\"transaction_data\": {\"qr_code_base64\": \"qrCodeData\"}}}";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(paymentResponse);

        // Simula a resposta do RestTemplate
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Simula o parsing do JSON pela ObjectMapper
        JsonNode mockJsonNode = mock(JsonNode.class);
        JsonNode mockTransactionData = mock(JsonNode.class);
        JsonNode mockQrCode = mock(JsonNode.class);

        when(objectMapper.readTree(paymentResponse)).thenReturn(mockJsonNode);
        when(mockJsonNode.get("point_of_interaction")).thenReturn(mockTransactionData);
        when(mockTransactionData.get("transaction_data")).thenReturn(mockQrCode);
        when(mockQrCode.get("qr_code_base64")).thenReturn(mockQrCode);
        when(mockQrCode.asText()).thenReturn("qrCodeData");

        // Chama o método a ser testado
        String result = mercadoPagoService.createPixPayment(100.0);

        // Verifica se o resultado é o esperado
        assertEquals("qrCodeData", result);
    }

    @Test
    void testCreatePixPayment_failure() throws Exception {
        // Simula uma resposta de erro do RestTemplate
        ResponseEntity<String> responseEntity = ResponseEntity.status(500).body("Erro no servidor");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Chama o método a ser testado
        String result = mercadoPagoService.createPixPayment(100.0);

        // Verifica se o resultado é nulo devido ao erro
        assertNull(result);
    }

    @Test
    void testFetchPaymentDetails_success() {
        String paymentId = "12345";
        String paymentDetails = "{\"status\":\"approved\"}";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(paymentDetails);

        // Simula a resposta do RestTemplate
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Chama o método a ser testado
        String result = mercadoPagoService.fetchPaymentDetails(paymentId);

        // Verifica se o resultado é o esperado
        assertEquals(paymentDetails, result);
    }

    @Test
    void testFetchPaymentDetails_failure() {
        String paymentId = "12345";
        ResponseEntity<String> responseEntity = ResponseEntity.status(500).body("Erro ao buscar pagamento");

        // Simula a resposta de erro do RestTemplate
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Chama o método a ser testado
        String result = mercadoPagoService.fetchPaymentDetails(paymentId);

        // Verifica se o resultado é nulo devido ao erro
        assertNull(result);
    }
}

