package at.spengergasse.spring_thymeleaf.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByDeviceIdOrderByStartDateTimeAsc(Integer deviceId);

    // Prüft, ob es für ein Gerät im angegebenen Zeitraum Überschneidungen gibt
    @Query("select case when count(r) > 0 then true else false end from Reservation r " +
            "where r.device.id = :deviceId and r.startDateTime < :end and r.endDateTime > :start")
    boolean existsOverlapForDevice(@Param("deviceId") Integer deviceId,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    // Prüft, ob es für einen Patienten im angegebenen Zeitraum Überschneidungen gibt
    @Query("select case when count(r) > 0 then true else false end from Reservation r " +
            "where r.patient.id = :patientId and r.startDateTime < :end and r.endDateTime > :start")
    boolean existsOverlapForPatient(@Param("patientId") Integer patientId,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);
}
