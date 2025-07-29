package com.bajaj.webhook;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Component
public class WebhookRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        RestTemplate restTemplate = new RestTemplate();

        String webhookGenUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Sneha Sharma");
        requestBody.put("regNo", "2210992392");
        requestBody.put("email", "sneha2392.be22@chitkara.edu.in");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody);

        ResponseEntity<Map> response = restTemplate.postForEntity(webhookGenUrl, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String accessToken = (String) response.getBody().get("accessToken");

            String finalQuery = """
                        SELECT
                            e1.EMP_ID,
                            e1.FIRST_NAME,
                            e1.LAST_NAME,
                            d.DEPARTMENT_NAME,
                            COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT
                        FROM
                            EMPLOYEE e1
                        JOIN
                            DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID
                        LEFT JOIN
                            EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT
                            AND e2.DOB > e1.DOB
                        GROUP BY
                            e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME
                        ORDER BY
                            e1.EMP_ID DESC;
                    """;

            String submissionUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

            Map<String, String> answer = new HashMap<>();
            answer.put("finalQuery", finalQuery);

            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.set("Authorization", accessToken);

            HttpEntity<Map<String, String>> authEntity = new HttpEntity<>(answer, authHeaders);

            ResponseEntity<String> submitResponse = restTemplate.postForEntity(submissionUrl, authEntity, String.class);

            System.out.println("Submission Status: " + submitResponse.getStatusCode());
            System.out.println("Submission Body: " + submitResponse.getBody());
        } else {
            System.out.println("Webhook generation failed. Status: " + response.getStatusCode());
        }
    }
}
