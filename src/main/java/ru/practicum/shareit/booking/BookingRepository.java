package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // для booker
    List<Booking> findAllByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    @Query("select booking from Booking booking " +
            "where booking.booker.id = :bookerId " +
            "and booking.start <= :now and booking.end >= :now")
    List<Booking> findCurrentForBooker(@Param("bookerId") Long bookerId,
                                       @Param("now") LocalDateTime now,
                                       Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    // для owner
    @Query("select booking from Booking booking where booking.item.owner.id = :ownerId")
    List<Booking> findAllForOwner(@Param("ownerId") Long ownerId, Sort sort);

    @Query("select booking from Booking booking where booking.item.owner.id = :ownerId and booking.end < :now")
    List<Booking> findPastForOwner(@Param("ownerId") Long ownerId,
                                   @Param("now") LocalDateTime now,
                                   Sort sort);

    @Query("select booking from Booking booking where booking.item.owner.id = :ownerId and booking.start > :now")
    List<Booking> findFutureForOwner(@Param("ownerId") Long ownerId,
                                     @Param("now") LocalDateTime now,
                                     Sort sort);

    @Query("select booking from Booking booking where booking.item.owner.id = :ownerId and booking.start <= :now and booking.end >= :now")
    List<Booking> findCurrentForOwner(@Param("ownerId") Long ownerId,
                                      @Param("now") LocalDateTime now,
                                      Sort sort);

    @Query("select booking from Booking booking where booking.item.owner.id = :ownerId and booking.status = :status")
    List<Booking> findByOwnerAndStatus(@Param("ownerId") Long ownerId,
                                       @Param("status") BookingStatus status,
                                       Sort sort);

    @Query("select booking from Booking booking " +
            "where booking.item.id = :itemId and booking.start <= current_timestamp " +
            "order by booking.end desc")
    List<Booking> findLastBooking(@Param("itemId") Long itemId);

    @Query("select booking from Booking booking " +
            "where booking.item.id = :itemId and booking.start > current_timestamp " +
            "order by booking.start asc")
    List<Booking> findNextBooking(@Param("itemId") Long itemId);
}
