package pl.netia.troubleticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.netia.troubleticket.entity.NoteEntity;

@Repository
public interface NoteRepository
        extends JpaRepository<NoteEntity, String> {
}