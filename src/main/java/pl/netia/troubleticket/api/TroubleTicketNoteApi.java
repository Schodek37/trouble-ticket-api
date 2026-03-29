package pl.netia.troubleticket.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;
import pl.netia.troubleticket.model.*;
import pl.netia.troubleticket.model.Error;

import java.util.Optional;

@Validated
@Tag(name = "TroubleTicketNote",
        description = "Operacje dodawania notatek do zgłoszeń Trouble Ticket.")
public interface TroubleTicketNoteApi {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    String PATH_ADD_TROUBLE_TICKET_NOTE = "/troubleTicket/{id}/note";
    /**
     * POST /troubleTicket/{id}/note : Dodaj notatkę do zgłoszenia Trouble Ticket
     * Tworzy nową notatkę dla istniejącego zgłoszenia widocznego w tenant scope użytkownika.  Operacja tworzy subresource notatki i nie służy do zmiany statusu zgłoszenia.
     *
     * @param id Unikalny identyfikator zgłoszenia Trouble Ticket. (required)
     * @param noteCreateRequest  (required)
     * @return Notatka została dodana. (status code 201)
     *         or Żądanie jest niepoprawne lub wykracza poza dozwolony kontrakt v1. (status code 400)
     *         or Brak uwierzytelnienia albo niepoprawny token. (status code 401)
     *         or Użytkownik jest uwierzytelniony, ale nie ma wymaganych uprawnień do wykonania operacji. (status code 403)
     *         or Zgłoszenie nie istnieje albo nie jest widoczne w tenant scope użytkownika. (status code 404)
     */
    @Operation(
            operationId = "addTroubleTicketNote",
            summary = "Dodaj notatkę do zgłoszenia Trouble Ticket",
            description = "Tworzy nową notatkę dla istniejącego zgłoszenia widocznego w tenant scope użytkownika.  Operacja tworzy subresource notatki i nie służy do zmiany statusu zgłoszenia. ",
            tags = { "TroubleTicketNote" },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Notatka została dodana.", content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Note.class), examples = {
                                    @ExampleObject(
                                            name = "NoteResponse",
                                            value = "{\"id\":\"NOTE-000003\",\"text\":\"Klient prosi o potwierdzenie planowanego terminu zamknięcia.\",\"date\":\"2026-03-19T10:45:00Z\"}"
                                    )
                            })

                    }),
                    @ApiResponse(responseCode = "400", description = "Żądanie jest niepoprawne lub wykracza poza dozwolony kontrakt v1.", content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class), examples = {
                                    @ExampleObject(
                                            name = "BadRequestError",
                                            value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"Pole status ma niedozwoloną wartość dla tej operacji.\",\"requestId\":\"req-9f8e7d6c\"}"
                                    )
                            })

                    }),
                    @ApiResponse(responseCode = "401", description = "Brak uwierzytelnienia albo niepoprawny token.", content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class), examples = {
                                    @ExampleObject(
                                            name = "UnauthorizedError",
                                            value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"Brak poprawnego Bearer tokenu.\",\"requestId\":\"req-9f8e7d6c\"}"
                                    )
                            })

                    }),
                    @ApiResponse(responseCode = "403", description = "Użytkownik jest uwierzytelniony, ale nie ma wymaganych uprawnień do wykonania operacji.", content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class), examples = {
                                    @ExampleObject(
                                            name = "ForbiddenError",
                                            value = "{\"code\":\"FORBIDDEN\",\"message\":\"Użytkownik nie ma uprawnień do wykonania tej operacji.\",\"requestId\":\"req-9f8e7d6c\"}"
                                    )
                            })

                    }),
                    @ApiResponse(responseCode = "404", description = "Zgłoszenie nie istnieje albo nie jest widoczne w tenant scope użytkownika.", content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class), examples = {
                                    @ExampleObject(
                                            name = "TroubleTicketNotFoundError",
                                            value = "{\"code\":\"TROUBLE_TICKET_NOT_FOUND\",\"message\":\"Zgłoszenie nie istnieje albo nie jest widoczne w tenant scope użytkownika.\",\"requestId\":\"req-9f8e7d6c\"}"
                                    )
                            })

                    })
            },
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            }
    )
    @RequestMapping(
            method = RequestMethod.POST,
            value = TroubleTicketNoteApi.PATH_ADD_TROUBLE_TICKET_NOTE,
            produces = { "application/json" },
            consumes = { "application/json" }
    )
    default ResponseEntity<Note> addTroubleTicketNote(
            @NotNull @Size(min = 1) @Parameter(name = "id", description = "Unikalny identyfikator zgłoszenia Trouble Ticket.", required = true, in = ParameterIn.PATH) @PathVariable("id") String id,
            @Parameter(name = "NoteCreateRequest", description = "", required = true) @Valid @RequestBody NoteCreateRequest noteCreateRequest
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"date\" : \"2000-01-23T04:56:07.000+00:00\", \"id\" : \"id\", \"text\" : \"text\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"code\" : \"code\", \"requestId\" : \"requestId\", \"message\" : \"message\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"code\" : \"code\", \"requestId\" : \"requestId\", \"message\" : \"message\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"code\" : \"code\", \"requestId\" : \"requestId\", \"message\" : \"message\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"code\" : \"code\", \"requestId\" : \"requestId\", \"message\" : \"message\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }
}