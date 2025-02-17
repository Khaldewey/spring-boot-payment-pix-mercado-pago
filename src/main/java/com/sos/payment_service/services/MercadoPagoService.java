package com.sos.payment_service.services;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class MercadoPagoService {
    
    //Aqui é colocado o token obtido na plataforma do mercado pago
    @Value("SEU-TOKEN-AQUI")
    private String accessToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
     
    public MercadoPagoService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper; 
    }
    

    public String createPixPayment(double amount) {
    String url = "https://api.mercadopago.com/v1/payments";
    
    // Garantir que o valor de amount tenha ponto como separador decimal
    String formattedAmount = String.format("%.2f", amount).replace(",", ".");
    
    // Gerar um Idempotency Key único para a requisição
    String idempotencyKey = UUID.randomUUID().toString();

    // Calcular o tempo de expiração: 10 minutos após o momento atual, com 1 hora a mais
    ZonedDateTime expirationTime = ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(10).plusHours(1);

    // Formatar a data de expiração no formato esperado: yyyy-MM-dd'T'HH:mm:ss.SSSZ
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    String expirationTimeFormatted = expirationTime.format(formatter);
    
    // Montando o corpo da requisição
    String requestBody = String.format(
        "{" +
        "\"transaction_amount\": %s, " +
        "\"description\": \"Bilhete Cash Prêmio\", " +
        "\"payment_method_id\": \"pix\", " +
        "\"date_of_expiration\": \"%s\", " +
        "\"payer\": {" +
            "\"email\": \"%s\", " +
            "\"first_name\": \"%s\", " +
            "\"last_name\": null, " +
            "\"identification\": {" +
                "\"type\": \"CPF\", " +
                "\"number\": null" +
            "}, " +
            "\"address\": {" +
                "\"zip_code\": null, " +
                "\"street_name\": null, " +
                "\"street_number\": null, " +
                "\"neighborhood\": null, " +
                "\"city\": null, " +
                "\"federal_unit\": null" +
            "}" +
        "}}", 
        formattedAmount, expirationTimeFormatted, "teste@teste.com", "Teste"
    );
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    headers.set("Authorization", "Bearer " + accessToken);
    headers.set("X-Idempotency-Key", idempotencyKey);

    HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

    if (response.getStatusCode().is2xxSuccessful()) {
        try {
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            // String paymentId = responseJson.get("id").asText();
            String qrCodeBase64 = responseJson.get("point_of_interaction")
                                               .get("transaction_data")
                                               .get("qr_code_base64")
                                               .asText();
            return qrCodeBase64; // Ou qualquer outro dado que precise
        } catch (Exception e) {
            // Tratar erros de parsing
            e.printStackTrace();
        }
    } else {
        System.err.println("Erro ao criar pagamento Pix: " + response.getStatusCode());
        return null;
    }
    return null;
}

    public String fetchPaymentDetails(String paymentId) {
        String url = "https://api.mercadopago.com/v1/payments/" + paymentId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            // Log or handle error
            System.err.println("Erro ao buscar detalhes do pagamento: " + response.getStatusCode());
            return null;
        }
    }

    
}
