package pl.netia.troubleticket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.netia.troubleticket.entity.NoteEntity;
import pl.netia.troubleticket.entity.TicketStatus;
import pl.netia.troubleticket.entity.TroubleTicketEntity;
import pl.netia.troubleticket.exception.InvalidStatusTransitionException;
import pl.netia.troubleticket.exception.TroubleTicketNotFoundException;
import pl.netia.troubleticket.mapper.TroubleTicketMapper;
import pl.netia.troubleticket.model.Note;
import pl.netia.troubleticket.model.NoteCreateRequest;
import pl.netia.troubleticket.model.TroubleTicket;
import pl.netia.troubleticket.model.TroubleTicketCreateRequest;
import pl.netia.troubleticket.model.TroubleTicketStatus;
import pl.netia.troubleticket.model.TroubleTicketSummary;
import pl.netia.troubleticket.repository.TroubleTicketRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TroubleTicketService Unit Tests")
class TroubleTicketServiceTest {

    @Mock
    private TroubleTicketRepository repository;

    @Mock
    private TroubleTicketMapper mapper;

    @InjectMocks
    private TroubleTicketService service;

    private static final String TENANT_ID = "TENANT_001";
    private static final String TICKET_ID = "ticket-123";
    private static final String EXTERNAL_ID = "EXT-001";

    private TroubleTicketEntity ticketEntity;
    private TroubleTicket ticketDto;
    private TroubleTicketCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        ticketEntity = TroubleTicketEntity.builder()
                .id(TICKET_ID)
                .tenantId(TENANT_ID)
                .externalId(EXTERNAL_ID)
                .serviceId(123456789L)
                .description("Test description")
                .status(TicketStatus.ACKNOWLEDGED)
                .build();

        ticketDto = new TroubleTicket();
        ticketDto.setId(TICKET_ID);
        ticketDto.setExternalId(EXTERNAL_ID);
        ticketDto.setServiceId(123456789L);
        ticketDto.setDescription("Test description");
        ticketDto.setStatus(TroubleTicket.StatusEnum.ACKNOWLEDGED);
        ticketDto.setNotes(List.of());

        createRequest = new TroubleTicketCreateRequest();
        createRequest.setExternalId(EXTERNAL_ID);
        createRequest.setServiceId(123456789L);
        createRequest.setDescription("Test description");
        createRequest.setStatus(TroubleTicketCreateRequest.StatusEnum.NEW);
        createRequest.setNote("Initial note");
    }

    @Test
    @DisplayName("create - powinien zwrócić nowe zgłoszenie z flagą created=true")
    void create_shouldReturnNewTicketWithCreatedFlag() {
        when(repository.findByExternalIdAndTenantId(EXTERNAL_ID, TENANT_ID))
                .thenReturn(Optional.empty());
        when(repository.save(any(TroubleTicketEntity.class)))
                .thenReturn(ticketEntity);
        when(mapper.toDto(ticketEntity))
                .thenReturn(ticketDto);

        var result = service.create(createRequest, TENANT_ID);

        assertThat(result.created()).isTrue();
        assertThat(result.ticket().getId()).isEqualTo(TICKET_ID);
        assertThat(result.ticket().getStatus())
                .isEqualTo(TroubleTicketStatus.ACKNOWLEDGED);
        verify(repository).save(any(TroubleTicketEntity.class));
    }

    @Test
    @DisplayName("create - powinien zwrócić istniejące zgłoszenie z flagą created=false")
    void create_shouldReturnExistingTicketWithIdempotency() {
        when(repository.findByExternalIdAndTenantId(EXTERNAL_ID, TENANT_ID))
                .thenReturn(Optional.of(ticketEntity));
        when(mapper.toDto(ticketEntity))
                .thenReturn(ticketDto);

        var result = service.create(createRequest, TENANT_ID);

        assertThat(result.created()).isFalse();
        assertThat(result.ticket().getId()).isEqualTo(TICKET_ID);
    }

    @Test
    @DisplayName("getById - powinien zwrócić zgłoszenie dla prawidłowego id i tenanta")
    void getById_shouldReturnTicket() {
        when(repository.findByIdAndTenantId(TICKET_ID, TENANT_ID))
                .thenReturn(Optional.of(ticketEntity));
        when(mapper.toDto(ticketEntity))
                .thenReturn(ticketDto);

        TroubleTicket result = service.getById(TICKET_ID, TENANT_ID);

        assertThat(result.getId()).isEqualTo(TICKET_ID);
    }

    @Test
    @DisplayName("getById - powinien rzucić wyjątek gdy zgłoszenie nie istnieje")
    void getById_shouldThrowWhenNotFound() {
        when(repository.findByIdAndTenantId(TICKET_ID, TENANT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(TICKET_ID, TENANT_ID))
                .isInstanceOf(TroubleTicketNotFoundException.class);
    }

    @Test
    @DisplayName("getById - powinien rzucić wyjątek gdy zgłoszenie należy do innego tenanta")
    void getById_shouldThrowWhenDifferentTenant() {
        when(repository.findByIdAndTenantId(TICKET_ID, "OTHER_TENANT"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(TICKET_ID, "OTHER_TENANT"))
                .isInstanceOf(TroubleTicketNotFoundException.class);
    }

    @Test
    @DisplayName("listAll - powinien zwrócić tylko zgłoszenia danego tenanta")
    void listAll_shouldReturnOnlyTenantTickets() {
        TroubleTicketSummary summary = new TroubleTicketSummary();
        summary.setExternalId(EXTERNAL_ID);

        when(repository.findAllByTenantId(TENANT_ID))
                .thenReturn(List.of(ticketEntity));
        when(mapper.toSummaryDto(ticketEntity))
                .thenReturn(summary);

        var result = service.listAll(TENANT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalId()).isEqualTo(EXTERNAL_ID);
    }

    @Test
    @DisplayName("close - powinien zmienić status na CLOSED")
    void close_shouldChangeStatusToClosed() {
        when(repository.findByIdAndTenantId(TICKET_ID, TENANT_ID))
                .thenReturn(Optional.of(ticketEntity));
        when(repository.save(any(TroubleTicketEntity.class)))
                .thenReturn(ticketEntity);

        ticketDto.setStatus(TroubleTicket.StatusEnum.CLOSED);
        when(mapper.toDto(ticketEntity))
                .thenReturn(ticketDto);

        TroubleTicket result = service.close(TICKET_ID, TENANT_ID);

        assertThat(result.getStatus()).isEqualTo(TroubleTicketStatus.CLOSED);
        verify(repository).save(ticketEntity);
    }

    @Test
    @DisplayName("close - powinien rzucić wyjątek gdy zgłoszenie już zamknięte")
    void close_shouldThrowWhenAlreadyClosed() {
        ticketEntity.setStatus(TicketStatus.CLOSED);
        when(repository.findByIdAndTenantId(TICKET_ID, TENANT_ID))
                .thenReturn(Optional.of(ticketEntity));

        assertThatThrownBy(() -> service.close(TICKET_ID, TENANT_ID))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("addNote - powinien dodać notatkę do zgłoszenia")
    void addNote_shouldAddNoteToTicket() {
        NoteCreateRequest noteRequest = new NoteCreateRequest();
        noteRequest.setText("Test note");

        Note noteDto = new Note();
        noteDto.setId("note-123");
        noteDto.setText("Test note");
        noteDto.setDate(OffsetDateTime.now());

        NoteEntity noteEntity = NoteEntity.builder()
                .id("note-123")
                .text("Test note")
                .createdAt(OffsetDateTime.now())
                .build();

        ticketEntity.getNotes().add(noteEntity);

        when(repository.findByIdAndTenantId(TICKET_ID, TENANT_ID))
                .thenReturn(Optional.of(ticketEntity));
        when(repository.saveAndFlush(any(TroubleTicketEntity.class)))
                .thenReturn(ticketEntity);
        when(mapper.toNoteDto(any(NoteEntity.class)))
                .thenReturn(noteDto);

        Note result = service.addNote(TICKET_ID, TENANT_ID, noteRequest);

        assertThat(result.getId()).isEqualTo("note-123");
        assertThat(result.getText()).isEqualTo("Test note");
    }
}