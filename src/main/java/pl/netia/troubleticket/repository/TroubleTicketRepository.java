package pl.netia.troubleticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.netia.troubleticket.entity.TroubleTicketEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TroubleTicketRepository
        extends JpaRepository<TroubleTicketEntity, String> {

    List<TroubleTicketEntity> findAllByTenantId(String tenantId);

    Optional<TroubleTicketEntity> findByIdAndTenantId(
            String id,
            String tenantId
    );

    Optional<TroubleTicketEntity> findByExternalIdAndTenantId(
            String externalId,
            String tenantId
    );
}