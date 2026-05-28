package at.spengergasse.spring_thymeleaf.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    private Patient patient;

    @ManyToOne(optional = false)
    private Device device;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    // zu untersuchende Körperregion
    private String bodyRegion;

    // Kommentar
    @Column(length = 2000)
    private String comment;

    public Integer getId() { return id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    public String getBodyRegion() { return bodyRegion; }
    public void setBodyRegion(String bodyRegion) { this.bodyRegion = bodyRegion; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
