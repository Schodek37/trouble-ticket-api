package pl.netia.troubleticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.netia.troubleticket.entity.TroubleTicketEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TroubleTicketRepository
        extends JpaRepository<TroubleTicketEntity, String> {

    // Znajdź wszystkie zgłoszenia dla danego tenanta
    List<TroubleTicketEntity> findAllByTenantId(String tenantId);

    // Znajdź zgłoszenie po ID i tenantId - izolacja tenant scope
    Optional<TroubleTicketEntity> findByIdAndTenantId(
            String id,
            String tenantId
    );

    // Znajdź zgłoszenie po externalId i tenantId - idempotencja
    Optional<TroubleTicketEntity> findByExternalIdAndTenantId(
            String externalId,
            String tenantId
    );
}