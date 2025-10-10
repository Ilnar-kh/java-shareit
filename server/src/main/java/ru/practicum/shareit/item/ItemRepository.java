package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerIdOrderByIdAsc(Long ownerId);

    @Query("""
            SELECT i FROM Item i
            WHERE i.available = true
              AND (UPPER(i.name) LIKE UPPER(CONCAT('%', :text, '%'))
                OR UPPER(i.description) LIKE UPPER(CONCAT('%', :text, '%')))
            ORDER BY i.id
            """)
    List<Item> search(@Param("text") String text);

    List<Item> findAllByRequestIdIn(Collection<Long> requestIds);
}

