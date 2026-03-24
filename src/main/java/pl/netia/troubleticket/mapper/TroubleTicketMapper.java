package pl.netia.troubleticket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.netia.troubleticket.entity.NoteEntity;
import pl.netia.troubleticket.entity.TicketStatus;
import pl.netia.troubleticket.entity.TroubleTicketEntity;
import pl.netia.troubleticket.model.Note;
import pl.netia.troubleticket.model.TroubleTicket;
import pl.netia.troubleticket.model.TroubleTicketStatus;
import pl.netia.troubleticket.model.TroubleTicketSummary;

@Mapper(componentModel = "spring")
public interface TroubleTicketMapper {

    @Mapping(target = "status", source = "status")
    TroubleTicket toDto(TroubleTicketEntity entity);

    @Mapping(target = "status", source = "status")
    TroubleTicketSummary toSummaryDto(TroubleTicketEntity entity);

    @Mapping(target = "date", source = "createdAt")
    Note toNoteDto(NoteEntity entity);

    default TroubleTicketStatus toApiStatus(TicketStatus status) {
        return switch (status) {
            case NEW -> TroubleTicketStatus.NEW;
            case ACKNOWLEDGED -> TroubleTicketStatus.ACKNOWLEDGED;
            case IN_PROGRESS -> TroubleTicketStatus.IN_PROGRESS;
            case RESOLVED -> TroubleTicketStatus.RESOLVED;
            case CLOSED -> TroubleTicketStatus.CLOSED;
            case REJECTED -> TroubleTicketStatus.REJECTED;
        };
    }

    default TicketStatus toEntityStatus(TroubleTicketStatus status) {
        return switch (status) {
            case NEW -> TicketStatus.NEW;
            case ACKNOWLEDGED -> TicketStatus.ACKNOWLEDGED;
            case IN_PROGRESS -> TicketStatus.IN_PROGRESS;
            case RESOLVED -> TicketStatus.RESOLVED;
            case CLOSED -> TicketStatus.CLOSED;
            case REJECTED -> TicketStatus.REJECTED;
        };
    }
}