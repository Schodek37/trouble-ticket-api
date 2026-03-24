package pl.netia.troubleticket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import pl.netia.troubleticket.model.TroubleTicketSummary;
import pl.netia.troubleticket.repository.TroubleTicketRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TroubleTicketService {

    private final TroubleTicketRepository repository;
    private final TroubleTicketMapper mapper;

    // ================================================================
    // CREATE — idempotencja na (tenantId, externalId)
    // ================================================================
    @Transactional
    public TroubleTicketCreationResult create(
            TroubleTicketCreateRequest request,
            String tenantId) {

        // Sprawdź idempotencję - czy zgłoszenie już istnieje
        return repository
                .findByExternalIdAndTenantId(request.getExternalId(), tenantId)
                .map(existing -> {
                    log.info("Returning existing ticket for externalId={} tenantId={}",
                            request.getExternalId(), tenantId);
                    // Zwróć istniejące zgłoszenie z flagą że już istniało
                    return new TroubleTicketCreationResult(
                            mapper.toDto(existing),
                            false
                    );
                })
                .orElseGet(() -> {
                    // Stwórz nowe zgłoszenie
                    NoteEntity firstNote = NoteEntity.builder()
                            .text(request.getNote())
                            .createdAt(OffsetDateTime.now())
                            .build();

                    TroubleTicketEntity entity = TroubleTicketEntity.builder()
                            .tenantId(tenantId)
                            .externalId(request.getExternalId())
                            .serviceId(request.getServiceId())
                            .description(request.getDescription())
                            .status(TicketStatus.ACKNOWLEDGED)
                            .build();

                    // Powiąż notatkę ze zgłoszeniem
                    firstNote.setTroubleTicket(entity);
                    entity.getNotes().add(firstNote);

                    TroubleTicketEntity saved = repository.save(entity);
                    log.info("Created new ticket id={} tenantId={}",
                            saved.getId(), tenantId);

                    return new TroubleTicketCreationResult(
                            mapper.toDto(saved),
                            true
                    );
                });
    }

    // ================================================================
    // LIST — wszystkie zgłoszenia w tenant scope
    // ================================================================
    @Transactional(readOnly = true)
    public List<TroubleTicketSummary> listAll(String tenantId) {
        return repository.findAllByTenantId(tenantId)
                .stream()
                .map(mapper::toSummaryDto)
                .toList();
    }

    // ================================================================
    // GET — pojedyncze zgłoszenie w tenant scope
    // ================================================================
    @Transactional(readOnly = true)
    public TroubleTicket getById(String id, String tenantId) {
        return repository.findByIdAndTenantId(id, tenantId)
                .map(mapper::toDto)
                .orElseThrow(() -> new TroubleTicketNotFoundException(id));
    }

    // ================================================================
    // CLOSE — zmiana statusu na closed
    // ================================================================
    @Transactional
    public TroubleTicket close(String id, String tenantId) {
        TroubleTicketEntity entity = repository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new TroubleTicketNotFoundException(id));

        // Kontrakt dopuszcza tylko przejście na closed
        if (entity.getStatus() == TicketStatus.CLOSED) {
            throw new InvalidStatusTransitionException(
                    "Ticket is already closed"
            );
        }

        entity.setStatus(TicketStatus.CLOSED);
        TroubleTicketEntity saved = repository.save(entity);
        log.info("Closed ticket id={} tenantId={}", id, tenantId);

        return mapper.toDto(saved);
    }

    // ================================================================
    // ADD NOTE — dodaj notatkę do zgłoszenia
    // ================================================================
    @Transactional
    public Note addNote(
            String id,
            String tenantId,
            NoteCreateRequest request) {

        TroubleTicketEntity entity = repository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new TroubleTicketNotFoundException(id));

        NoteEntity note = NoteEntity.builder()
                .text(request.getText())
                .createdAt(OffsetDateTime.now())
                .troubleTicket(entity)
                .build();

        entity.getNotes().add(note);
        repository.save(entity);
        log.info("Added note to ticket id={} tenantId={}", id, tenantId);

        return mapper.toNoteDto(note);
    }

    // ================================================================
    // Wewnętrzny record dla wyniku create
    // ================================================================
    public record TroubleTicketCreationResult(
            TroubleTicket ticket,
            boolean created
    ) {}
}