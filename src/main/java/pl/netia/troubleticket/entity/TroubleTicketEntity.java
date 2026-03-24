package pl.netia.troubleticket.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "trouble_tickets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_tenant_external_id",
                        columnNames = {"tenant_id", "external_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TroubleTicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @OneToMany(
            mappedBy = "troubleTicket",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<NoteEntity> notes = new ArrayList<>();
}