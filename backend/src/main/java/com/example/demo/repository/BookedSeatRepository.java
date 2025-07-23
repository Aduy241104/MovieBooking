package com.example.demo.repository;

import com.example.demo.model.BookedSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookedSeatRepository extends JpaRepository<BookedSeat, Long> {

    // get all BookedSeat of a show to display time in seat selection
    @Query("SELECT bs FROM BookedSeat bs JOIN bs.booking b WHERE b.screening.id = :screeningId")
    List<BookedSeat> findByScreeningId(@Param("screeningId") Long screeningId);
}
