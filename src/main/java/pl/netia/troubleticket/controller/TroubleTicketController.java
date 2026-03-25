package pl.netia.troubleticket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.netia.troubleticket.api.TroubleTicketApi;
import pl.netia.troubleticket.model.Note;
import pl.netia.troubleticket.model.NoteCreateRequest;
import pl.netia.troubleticket.model.TroubleTicket;
import pl.netia.troubleticket.model.TroubleTicketCloseStatusRequest;
import pl.netia.troubleticket.model.TroubleTicketCreateRequest;
import pl.netia.troubleticket.model.TroubleTicketSummary;
import pl.netia.troubleticket.service.TroubleTicketService;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TroubleTicketController implements TroubleTicketApi {

    private final TroubleTicketService service;

    private static final String TEMP_TENANT_ID = "tenant-1";

    @Override
    public ResponseEntity<TroubleTicket> createTroubleTicket(
            TroubleTicketCreateRequest request) {

        var result = service.create(request, TEMP_TENANT_ID);

        if (result.created()) {
            return ResponseEntity
                    .created(URI.create(
                            "/api/v1/troubleTicket/" + result.ticket().getId()
                    ))
                    .body(result.ticket());
        } else {
            return ResponseEntity
                    .ok()
                    .location(URI.create(
                            "/api/v1/troubleTicket/" + result.ticket().getId()
                    ))
                    .body(result.ticket());
        }
    }

    @Override
    public ResponseEntity<List<TroubleTicketSummary>> listTroubleTickets() {
        return ResponseEntity.ok(service.listAll(TEMP_TENANT_ID));
    }

    @Override
    public ResponseEntity<TroubleTicket> getTroubleTicketById(String id) {
        return ResponseEntity.ok(service.getById(id, TEMP_TENANT_ID));
    }

    @Override
    public ResponseEntity<TroubleTicket> closeTroubleTicket(
            String id,
            TroubleTicketCloseStatusRequest request) {

        if (request.getStatus() == null) {
            throw new IllegalArgumentException(
                    "Status is required"
            );
        }

        return ResponseEntity.ok(service.close(id, TEMP_TENANT_ID));
    }

    @Override
    public ResponseEntity<Note> addTroubleTicketNote(
            String id,
            NoteCreateRequest request) {

        Note note = service.addNote(id, TEMP_TENANT_ID, request);
        return ResponseEntity
                .created(URI.create(
                        "/api/v1/troubleTicket/" + id + "/note/" + note.getId()
                ))
                .body(note);
    }
}