package pl.netia.troubleticket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.netia.troubleticket.api.TroubleTicketNoteApi;
import pl.netia.troubleticket.model.Note;
import pl.netia.troubleticket.model.NoteCreateRequest;
import pl.netia.troubleticket.security.TenantContext;
import pl.netia.troubleticket.service.TroubleTicketService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class TroubleTicketNoteController implements TroubleTicketNoteApi {

    private final TroubleTicketService service;
    private final TenantContext tenantContext;

    @Override
    public ResponseEntity<Note> addTroubleTicketNote(
            String id,
            NoteCreateRequest request) {

        Note note = service.addNote(id, tenantContext.getTenantId(), request);
        return ResponseEntity
                .created(URI.create(
                        "/api/v1/troubleTicket/" + id + "/note/" + note.getId()
                ))
                .body(note);
    }
}