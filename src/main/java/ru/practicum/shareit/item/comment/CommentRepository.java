package ru.practicum.shareit.item.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemIdOrderByCreatedDesc(Long itemId);

    @Query("""
            SELECT COUNT(booking) > 0 FROM Booking booking
            WHERE booking.item.id = :itemId
              AND booking.booker.id = :userId
              AND booking.end <= :now
            """)
    boolean userHasFinishedBooking(@Param("userId") Long userId,
                                   @Param("itemId") Long itemId,
                                   @Param("now") LocalDateTime now);
}
