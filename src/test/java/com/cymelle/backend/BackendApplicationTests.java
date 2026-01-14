package com.cymelle.backend;

import com.cymelle.backend.dto.*;
import com.cymelle.backend.model.OrderStatus;
import com.cymelle.backend.model.Product;
import com.cymelle.backend.model.RideStatus;
import com.cymelle.backend.model.Role;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Helpers
    private String authenticateAndGetToken(String email, String password) throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("token").asText();
    }

    private String registerAndGetToken(String email, Role role) throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password("password123")
                .role(role)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("token").asText();
    }

    // --- 1. User Registration and Authentication ---

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("new@example.com")
                .password("password123")
                .role(Role.CUSTOMER)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldFailRegistrationWithInvalidData() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("") // Invalid
                .lastName("Doe")
                .email("invalid-email") // Invalid
                .password("123") 
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAuthenticateSuccessfully() throws Exception {
        // First register
        registerAndGetToken("auth@example.com", Role.CUSTOMER);

        // Then auth
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("auth@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldFailAuthenticationWithBadCredentials() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("nonexistent@example.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); 
    }

    // --- 2. Product Management ---

    @Test
    void shouldAllowAdminToManageProducts() throws Exception {
        String adminToken = registerAndGetToken("admin@example.com", Role.ADMIN);

        // Add Product
        Product product = Product.builder()
                .name("Test Product")
                .description("Desc")
                .price(BigDecimal.valueOf(100.0))
                .stockQuantity(10)
                .category("Electronics")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        
        JsonNode prodNode = objectMapper.readTree(result.getResponse().getContentAsString());
        Long prodId = prodNode.get("id").asLong();

        // Update Product
        product.setPrice(BigDecimal.valueOf(150.0));
        mockMvc.perform(put("/api/v1/products/" + prodId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(150.0));

        // Delete Product
        mockMvc.perform(delete("/api/v1/products/" + prodId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyCustomerFromManagingProducts() throws Exception {
        String customerToken = registerAndGetToken("cust@example.com", Role.CUSTOMER);

        Product product = Product.builder()
                .name("Hack Product")
                .price(BigDecimal.TEN)
                .stockQuantity(1)
                .category("Hack")
                .build();

        // Add it should be forbidden
        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource."));
    }

    @Test
    void shouldFailProductCreationWithInvalidData() throws Exception {
        String adminToken = registerAndGetToken("admin2@example.com", Role.ADMIN);
        
        Product product = Product.builder()
                .name("")
                .price(BigDecimal.valueOf(-5))
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSearchProducts() throws Exception {
        String adminToken = registerAndGetToken("admin_search@example.com", Role.ADMIN);
        
        // Create products
        Product p1 = Product.builder().name("iPhone 15").price(BigDecimal.valueOf(1000)).stockQuantity(5).category("Mobile").build();
        Product p2 = Product.builder().name("Samsung galaxy").price(BigDecimal.valueOf(900)).stockQuantity(5).category("Mobile").build();
        
        // add products as admin
        mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p1)));
        mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p2)));

        String custToken = registerAndGetToken("cust_search@example.com", Role.CUSTOMER);

        // customer search by name
        mockMvc.perform(get("/api/v1/products?search=iPhone")
                        .header("Authorization", "Bearer " + custToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("iPhone 15"));

        // customer search by category
        mockMvc.perform(get("/api/v1/products?search=Mobile")
                        .header("Authorization", "Bearer " + custToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))));
    }

    // --- 3. Order Management ---

    @Test
    void shouldPlaceAndManageOrders() throws Exception {
        // Setup: Admin creates product, Customer registers
        String adminToken = registerAndGetToken("admin_order@example.com", Role.ADMIN);
        Product product = Product.builder().name("Laptop").price(BigDecimal.valueOf(1000)).stockQuantity(10).category("Tech").build();
        
        MvcResult prodResult = mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(product))).andReturn();
        Long prodId = objectMapper.readTree(prodResult.getResponse().getContentAsString()).get("id").asLong();

        String custToken = registerAndGetToken("cust_order@example.com", Role.CUSTOMER);

        // Place Order
        OrderItemRequest itemReq = OrderItemRequest.builder().productId(prodId).quantity(2).build();
        OrderRequest orderReq = OrderRequest.builder().items(List.of(itemReq)).build();

        MvcResult orderResult = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + custToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(2000.0))
                .andReturn();
        
        Long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString()).get("id").asLong();

        // Get Orders
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + custToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        // Update Status (ADMIN only)
        UpdateOrderStatusRequest updateStatusReq = UpdateOrderStatusRequest.builder().status(OrderStatus.SHIPPED).build();
        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void shouldDenyCustomerFromUpdatingOrderStatus() throws Exception {
        String adminToken = registerAndGetToken("admin_ops@example.com", Role.ADMIN);
        Product product = Product.builder().name("Book").price(BigDecimal.TEN).stockQuantity(100).category("Edu").build();
        MvcResult prodResult = mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(product))).andReturn();
        Long prodId = objectMapper.readTree(prodResult.getResponse().getContentAsString()).get("id").asLong();

        String custToken = registerAndGetToken("cust_ops@example.com", Role.CUSTOMER);
        OrderRequest orderReq = OrderRequest.builder().items(List.of(OrderItemRequest.builder().productId(prodId).quantity(1).build())).build();
        MvcResult orderResult = mockMvc.perform(post("/api/v1/orders").header("Authorization", "Bearer " + custToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq))).andReturn();
        Long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString()).get("id").asLong();

        // Customer tries to update status -> Forbidden
        UpdateOrderStatusRequest updateStatusReq = UpdateOrderStatusRequest.builder().status(OrderStatus.SHIPPED).build();
        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + custToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailOrderWithErrorIfStockInsufficient() throws Exception {
        String adminToken = registerAndGetToken("admin_stock@example.com", Role.ADMIN);
        Product product = Product.builder().name("Rare Item").price(BigDecimal.valueOf(100)).stockQuantity(1).category("Tech").build();
        MvcResult prodResult = mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(product))).andReturn();
        Long prodId = objectMapper.readTree(prodResult.getResponse().getContentAsString()).get("id").asLong();

        String custToken = registerAndGetToken("cust_stock@example.com", Role.CUSTOMER);

        // Order 2 items (Only 1 in stock)
        OrderItemRequest itemReq = OrderItemRequest.builder().productId(prodId).quantity(2).build();
        OrderRequest orderReq = OrderRequest.builder().items(List.of(itemReq)).build();

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + custToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Insufficient stock")));
    }

    // --- 4. Ride Management ---

    @Test
    void shouldRequestAndManageRides() throws Exception {
        String custToken = registerAndGetToken("rider@example.com", Role.CUSTOMER);
        String driverToken = registerAndGetToken("driver@example.com", Role.DRIVER);

        // Request Ride
        RideRequest request = RideRequest.builder()
                .pickupLocation("A")
                .dropoffLocation("B")
                .build();

        MvcResult rideResult = mockMvc.perform(post("/api/v1/rides")
                        .header("Authorization", "Bearer " + custToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn();
        
        Long rideId = objectMapper.readTree(rideResult.getResponse().getContentAsString()).get("id").asLong();

        // Driver searches for REQUESTED rides
        mockMvc.perform(get("/api/v1/rides?status=REQUESTED")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));

        // Driver Accepts Ride
        UpdateRideStatusRequest updateRideReq = UpdateRideStatusRequest.builder().status(RideStatus.ACCEPTED).build();
        mockMvc.perform(patch("/api/v1/rides/" + rideId + "/status")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRideReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.driver").exists()); // Driver should be assigned
    }

    // --- 5. Search Functionality (Orders & Rides) ---

    @Test
    void shouldSearchOrdersByStatusAndUser() throws Exception {
        String custToken = registerAndGetToken("search_orders@example.com", Role.CUSTOMER);
        
        // Setup: Admin creates product
        String adminToken = registerAndGetToken("admin_search_ops@example.com", Role.ADMIN);
        Product product = Product.builder().name("Book").price(BigDecimal.TEN).stockQuantity(100).category("Edu").build();
        MvcResult prodResult = mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(product))).andReturn();
        Long prodId = objectMapper.readTree(prodResult.getResponse().getContentAsString()).get("id").asLong();

        // Place two orders
        OrderRequest orderReq = OrderRequest.builder().items(List.of(OrderItemRequest.builder().productId(prodId).quantity(1).build())).build();
        mockMvc.perform(post("/api/v1/orders").header("Authorization", "Bearer " + custToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq)));
        MvcResult order2 = mockMvc.perform(post("/api/v1/orders").header("Authorization", "Bearer " + custToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq))).andReturn();
        Long order2Id = objectMapper.readTree(order2.getResponse().getContentAsString()).get("id").asLong();

        // Update second order to SHIPPED
        UpdateOrderStatusRequest updateOrderReq = UpdateOrderStatusRequest.builder().status(OrderStatus.SHIPPED).build();
        mockMvc.perform(patch("/api/v1/orders/" + order2Id + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateOrderReq)));

        // Test 1: Search by Status (SHIPPED)
        mockMvc.perform(get("/api/v1/orders?status=SHIPPED")
                        .header("Authorization", "Bearer " + custToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("SHIPPED"));

        // Test 2: Search by User (Default behavior when no status provided)
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + custToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2))); // Should verify we get both orders back for this user

        // Test 3: Admin Search All by Status
        mockMvc.perform(get("/api/v1/orders?status=SHIPPED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));

        // Test 4: Admin Search All (No status)
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)));

        // Test 5: Admin Search by both Email and Status
        mockMvc.perform(get("/api/v1/orders?email=search_orders@example.com&status=SHIPPED")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        // Test 6: Admin Search by Email Only
        mockMvc.perform(get("/api/v1/orders?email=search_orders@example.com")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        // Test 7: Admin Search by Status Only
        mockMvc.perform(get("/api/v1/orders?status=SHIPPED")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void shouldSearchRidesByStatusAndUser() throws Exception {
        String custToken = registerAndGetToken("search_rider@example.com", Role.CUSTOMER);
        String driverToken = registerAndGetToken("search_driver@example.com", Role.DRIVER);

        // Request 2 rides
        RideRequest req1 = RideRequest.builder().pickupLocation("LocA").dropoffLocation("LocB").build();
        RideRequest req2 = RideRequest.builder().pickupLocation("LocC").dropoffLocation("LocD").build();

        mockMvc.perform(post("/api/v1/rides").header("Authorization", "Bearer " + custToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req1)));
        MvcResult r2 = mockMvc.perform(post("/api/v1/rides").header("Authorization", "Bearer " + custToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req2))).andReturn();
        Long r2Id = objectMapper.readTree(r2.getResponse().getContentAsString()).get("id").asLong();

        // Driver accepts 2nd ride
        UpdateRideStatusRequest updateRideReq = UpdateRideStatusRequest.builder().status(RideStatus.ACCEPTED).build();
        mockMvc.perform(patch("/api/v1/rides/" + r2Id + "/status")
                .header("Authorization", "Bearer " + driverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRideReq)));

        // Test 1: Search by Status (ACCEPTED)
        mockMvc.perform(get("/api/v1/rides?status=ACCEPTED")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("ACCEPTED"));

        // Test 2: Search by User (Customer sees their own rides)
        mockMvc.perform(get("/api/v1/rides")
                        .header("Authorization", "Bearer " + custToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldFailRideAssignmentIfSelf() throws Exception {
        String custToken = registerAndGetToken("self_driver@example.com", Role.DRIVER); // Register as DRIVER but also acts as customer
        
        // Request ride
        RideRequest req = RideRequest.builder().pickupLocation("LocA").dropoffLocation("LocB").build();
        MvcResult res = mockMvc.perform(post("/api/v1/rides")
                .header("Authorization", "Bearer " + custToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        Long rideId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        // Attempt to accept own ride
        UpdateRideStatusRequest updateReq = UpdateRideStatusRequest.builder().status(RideStatus.ACCEPTED).build();
        mockMvc.perform(patch("/api/v1/rides/" + rideId + "/status")
                .header("Authorization", "Bearer " + custToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Drivers cannot accept a ride they requested as a customer."));
    }

    @Test
    void shouldFailInvalidRideStatusTransition() throws Exception {
        String custToken = registerAndGetToken("transition_customer@example.com", Role.CUSTOMER);
        String adminToken = registerAndGetToken("transition_admin@example.com", Role.ADMIN);

        RideRequest req = RideRequest.builder().pickupLocation("LocA").dropoffLocation("LocB").build();
        MvcResult res = mockMvc.perform(post("/api/v1/rides")
                .header("Authorization", "Bearer " + custToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        Long rideId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        // Attempt to complete without accepting
        UpdateRideStatusRequest updateReq = UpdateRideStatusRequest.builder().status(RideStatus.COMPLETED).build();
        mockMvc.perform(patch("/api/v1/rides/" + rideId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ride can only be completed after it has been accepted."));
    }
}
