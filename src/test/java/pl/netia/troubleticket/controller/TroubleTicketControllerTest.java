package pl.netia.troubleticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.netia.troubleticket.config.TestContainersConfig;
import pl.netia.troubleticket.model.TroubleTicketCreateRequest;
import pl.netia.troubleticket.model.TroubleTicketCreateStatus;
import pl.netia.troubleticket.repository.TroubleTicketRepository;
import pl.netia.troubleticket.security.JwtTestTokenGenerator;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
@DisplayName("TroubleTicket Controller Integration Tests")
class TroubleTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TroubleTicketRepository repository;

    private static final String TENANT_1 = "TENANT_001";
    private static final String TENANT_2 = "TENANT_002";
    private static final String BASE_URL = "/troubleTicket";

    private String tokenTenant1;
    private String tokenTenant2;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        tokenTenant1 = JwtTestTokenGenerator.generateToken(TENANT_1);
        tokenTenant2 = JwtTestTokenGenerator.generateToken(TENANT_2);
    }

    @Test
    @DisplayName("POST /troubleTicket - powinien zwrócić 201 dla nowego zgłoszenia")
    void createTicket_shouldReturn201() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", tokenTenant1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest("EXT-001"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.externalId", is("EXT-001")))
                .andExpect(jsonPath("$.status", is("acknowledged")))
                .andExpect(jsonPath("$.notes", hasSize(1)));
    }

    @Test
    @DisplayName("POST /troubleTicket - powinien zwrócić 200 dla istniejącego zgłoszenia")
    void createTicket_shouldReturn200ForDuplicate() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", tokenTenant1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest("EXT-002"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", tokenTenant1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest("EXT-002"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId", is("EXT-002")));
    }

    @Test
    @DisplayName("POST /troubleTicket - powinien zwrócić 401 bez tokenu")
    void createTicket_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest("EXT-003"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /troubleTicket - powinien zwrócić tylko zgłoszenia danego tenanta")
    void listTickets_shouldReturnOnlyTenantTickets() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", tokenTenant1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest("EXT-004"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", tokenTenant2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest("EXT-005"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", tokenTenant1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].externalId", is("EXT-004")));
    }

    @Test
    @DisplayName("GET /troubleTicket/{id} - powinien zwrócić 404 dla innego tenanta")
    void getTicketById_shouldReturn404ForDifferentTenant() throws Exception {
        String response = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", tokenTenant1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest("EXT-006"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String ticketId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get(BASE_URL + "/" + ticketId)
                        .header("Authorization", tokenTenant2))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /troubleTicket/{id} - powinien zamknąć zgłoszenie")
    void closeTicket_shouldReturn200WithClosedStatus() throws Exception {
        String response = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", tokenTenant1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest("EXT-007"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String ticketId = objectMapper.readTree(response).get("id").asText();

        String closeRequest = """
                { "status": "closed" }
                """;

        mockMvc.perform(patch(BASE_URL + "/" + ticketId)
                        .header("Authorization", tokenTenant1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(closeRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("closed")));
    }

    @Test
    @DisplayName("POST /troubleTicket/{id}/note - powinien dodać notatkę z id")
    void addNote_shouldReturn201WithNoteId() throws Exception {
        String response = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", tokenTenant1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest("EXT-008"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String ticketId = objectMapper.readTree(response).get("id").asText();

        String noteRequest = """
                { "text": "Testowa notatka integracyjna" }
                """;

        mockMvc.perform(post(BASE_URL + "/" + ticketId + "/note")
                        .header("Authorization", tokenTenant1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is("Testowa notatka integracyjna")));
    }

    private TroubleTicketCreateRequest buildCreateRequest(String externalId) {
        TroubleTicketCreateRequest request = new TroubleTicketCreateRequest();
        request.setExternalId(externalId);
        request.setServiceId(987654321L);
        request.setDescription("Test description");
        request.setStatus(TroubleTicketCreateStatus.NEW);
        request.setNote("Initial note");
        return request;
    }
}