package com.pabloverdejo.stockpilot;

import com.jayway.jsonpath.JsonPath;
import com.pabloverdejo.stockpilot.common.RequestTraceFilter;
import com.pabloverdejo.stockpilot.product.ProductRepository;
import com.pabloverdejo.stockpilot.stock.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ApiSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private RequestTraceFilter requestTraceFilter;
    @Autowired
    private StockMovementRepository movementRepository;
    @Autowired
    private ProductRepository productRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(requestTraceFilter)
                .apply(springSecurity())
                .build();
        movementRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void issuesSignedTokensAndEnforcesRolePermissions() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .header(RequestTraceFilter.HEADER, "missing-token-123"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(RequestTraceFilter.HEADER, "missing-token-123"))
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.type").value("https://stockpilot.dev/problems/authentication"))
                .andExpect(jsonPath("$.instance").value("/api/v1/products"))
                .andExpect(jsonPath("$.requestId").value("missing-token-123"));

        mockMvc.perform(post("/api/v1/auth/token")
                        .header(RequestTraceFilter.HEADER, "failed-login-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"viewer","password":"wrong-password"}
                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://stockpilot.dev/problems/authentication"))
                .andExpect(jsonPath("$.requestId").value("failed-login-123"));

        var viewerToken = token("viewer", "viewer-local");
        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + viewerToken)
                        .header(RequestTraceFilter.HEADER, "viewer-forbidden-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson("VIEWER-DENIED")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://stockpilot.dev/problems/authorization"))
                .andExpect(jsonPath("$.requestId").value("viewer-forbidden-123"));

        var managerToken = token("manager", "manager-local");
        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson("MANAGER-ALLOWED")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("MANAGER-ALLOWED"));

        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer not-a-valid-jwt")
                        .header(RequestTraceFilter.HEADER, "invalid-token-123"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.type").value("https://stockpilot.dev/problems/authentication"))
                .andExpect(jsonPath("$.requestId").value("invalid-token-123"));
    }

    @Test
    void corsPreflightAllowsProductDeactivationFromTheDashboard() throws Exception {
        mockMvc.perform(options("/api/v1/products/11111111-1111-1111-1111-111111111111")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "DELETE")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("DELETE")))
                .andExpect(header().exists(RequestTraceFilter.HEADER));
    }

    @Test
    void normalizesJsonPathAndPaginationBindingFailures() throws Exception {
        var managerToken = token("manager", "manager-local");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + managerToken)
                        .header(RequestTraceFilter.HEADER, "malformed-json-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://stockpilot.dev/problems/invalid-body"))
                .andExpect(jsonPath("$.requestId").value("malformed-json-123"));

        mockMvc.perform(post("/api/v1/products/11111111-1111-1111-1111-111111111111/movements")
                        .header("Authorization", "Bearer " + managerToken)
                        .header(RequestTraceFilter.HEADER, "invalid-enum-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"UNKNOWN","quantity":1,"reason":"Invalid enum"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://stockpilot.dev/problems/invalid-body"))
                .andExpect(jsonPath("$.requestId").value("invalid-enum-123"));

        mockMvc.perform(get("/api/v1/products/not-a-uuid")
                        .header("Authorization", "Bearer " + managerToken)
                        .header(RequestTraceFilter.HEADER, "invalid-uuid-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://stockpilot.dev/problems/invalid-parameter"))
                .andExpect(jsonPath("$.parameter").value("id"))
                .andExpect(jsonPath("$.requestId").value("invalid-uuid-123"));

        mockMvc.perform(get("/api/v1/products?sort=unknownField")
                        .header("Authorization", "Bearer " + managerToken)
                        .header(RequestTraceFilter.HEADER, "invalid-sort-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://stockpilot.dev/problems/validation"))
                .andExpect(jsonPath("$.errors.sort").exists())
                .andExpect(jsonPath("$.requestId").value("invalid-sort-123"));

        mockMvc.perform(get("/api/v1/products?page=-1&size=101")
                        .header("Authorization", "Bearer " + managerToken)
                        .header(RequestTraceFilter.HEADER, "invalid-page-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://stockpilot.dev/problems/validation"))
                .andExpect(jsonPath("$.errors.page").exists())
                .andExpect(jsonPath("$.errors.size").exists())
                .andExpect(jsonPath("$.requestId").value("invalid-page-123"));
    }

    @Test
    void exposesStableDeterministicPaginationMetadata() throws Exception {
        var managerToken = token("manager", "manager-local");
        for (var sku : List.of("PAGE-A", "PAGE-B", "PAGE-C")) {
            mockMvc.perform(post("/api/v1/products")
                            .header("Authorization", "Bearer " + managerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validProductJson(sku)))
                    .andExpect(status().isCreated());
        }

        var firstPage = mockMvc.perform(get("/api/v1/products?page=0&size=2&sort=name&direction=asc")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andReturn();
        var repeatedFirstPage = mockMvc.perform(get("/api/v1/products?page=0&size=2&sort=name&direction=asc")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andReturn();
        var secondPage = mockMvc.perform(get("/api/v1/products?page=1&size=2&sort=name&direction=asc")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andReturn();

        List<String> firstIds = JsonPath.read(firstPage.getResponse().getContentAsString(), "$.content[*].id");
        List<String> repeatedIds = JsonPath.read(
                repeatedFirstPage.getResponse().getContentAsString(), "$.content[*].id");
        List<String> secondIds = JsonPath.read(secondPage.getResponse().getContentAsString(), "$.content[*].id");
        assertThat(repeatedIds).isEqualTo(firstIds);
        assertThat(firstIds).doesNotContainAnyElementsOf(secondIds);
    }

    @Test
    void validationProblemsCarryTheSameRequestIdAsTheResponse() throws Exception {
        var managerToken = token("manager", "manager-local");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + managerToken)
                        .header(RequestTraceFilter.HEADER, "portfolio-test-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "invalid sku",
                                  "name": "",
                                  "unitPrice": 0,
                                  "initialStock": -1,
                                  "reorderLevel": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(RequestTraceFilter.HEADER, "portfolio-test-123"))
                .andExpect(jsonPath("$.type").value("https://stockpilot.dev/problems/validation"))
                .andExpect(jsonPath("$.instance").value("/api/v1/products"))
                .andExpect(jsonPath("$.requestId").value("portfolio-test-123"))
                .andExpect(jsonPath("$.errors.sku").exists())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void persistsMovementsAndRejectsNegativeStock() throws Exception {
        var managerToken = token("manager", "manager-local");
        var createResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson("STOCK-FLOW")))
                .andExpect(status().isCreated())
                .andReturn();
        String productId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/v1/products/{productId}/movements", productId)
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "OUTBOUND",
                                  "quantity": 3,
                                  "reason": "Integration test order",
                                  "reference": "ORDER-TEST-1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultingStock").value(2));

        mockMvc.perform(post("/api/v1/products/{productId}/movements", productId)
                        .header("Authorization", "Bearer " + managerToken)
                        .header(RequestTraceFilter.HEADER, "stock-conflict-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "OUTBOUND",
                                  "quantity": 3,
                                  "reason": "Would create negative stock"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://stockpilot.dev/problems/business-rule"))
                .andExpect(jsonPath("$.requestId").value("stock-conflict-123"));

        mockMvc.perform(get("/api/v1/products/{productId}/movements", productId)
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private String token(String username, String password) throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(300))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private String validProductJson(String sku) {
        return """
                {
                  "sku": "%s",
                  "name": "Mechanical keyboard",
                  "description": "Created by an integration test",
                  "unitPrice": 89.90,
                  "initialStock": 5,
                  "reorderLevel": 2
                }
                """.formatted(sku);
    }
}
