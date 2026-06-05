package at.spengergasse.spring_thymeleaf.controllers;

import at.spengergasse.spring_thymeleaf.entities.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reservation")
public class ReservationController {
    private final ReservationRepository reservationRepository;
    private final PatientRepository patientRepository;
    private final DeviceRepository deviceRepository;

    public ReservationController(ReservationRepository reservationRepository,
                                 PatientRepository patientRepository,
                                 DeviceRepository deviceRepository) {
        this.reservationRepository = reservationRepository;
        this.patientRepository = patientRepository;
        this.deviceRepository = deviceRepository;
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("patients", patientRepository.findAll());
        model.addAttribute("devices", deviceRepository.findAll());
        model.addAttribute("reservationForm", new ReservationForm());
        model.addAttribute("bodyRegions", List.of("Kopf", "Brust", "Abdomen", "Wirbelsäule", "Extremitäten"));
        return "add_reservation";
    }

    @PostMapping("/add")
    public String addReservation(@ModelAttribute("reservationForm") ReservationForm form,
                                 BindingResult br,
                                 Model model) {
        if (form.getPatientId() == null || form.getDeviceId() == null || form.getDate() == null
                || form.getStartTime() == null || form.getEndTime() == null) {
            br.reject("invalid", "Bitte alle Pflichtfelder ausfüllen");
        }

        if (!br.hasErrors() && form.getStartTime() != null && form.getEndTime() != null
                && !form.getEndTime().isAfter(form.getStartTime())) {
            br.reject("time", "Endzeit muss nach der Startzeit liegen");
        }

        Optional<Patient> patientOpt = form.getPatientId() != null ? patientRepository.findById(form.getPatientId()) : Optional.empty();
        Optional<Device> deviceOpt = form.getDeviceId() != null ? deviceRepository.findById(form.getDeviceId()) : Optional.empty();

        if (!patientOpt.isPresent() || !deviceOpt.isPresent()) {
            br.reject("notfound", "Ausgewählter Patient oder Gerät existiert nicht");
        }

        if (br.hasErrors()) {
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("devices", deviceRepository.findAll());
            model.addAttribute("bodyRegions", List.of("Kopf", "Brust", "Abdomen", "Wirbelsäule", "Extremitäten"));
            return "add_reservation";
        }

        LocalDateTime start = LocalDateTime.of(form.getDate(), form.getStartTime());
        LocalDateTime end = LocalDateTime.of(form.getDate(), form.getEndTime());

        if (start.isBefore(LocalDateTime.now())) {
            br.reject("past", "Ein Termin in der Vergangenheit darf nicht reserviert werden");
        }

        if (!br.hasErrors()) {
            boolean deviceOverlap = reservationRepository.existsOverlapForDevice(form.getDeviceId(), start, end);
            if (deviceOverlap) {
                br.reject("overlap_device", "Termine überschneiden sich: Für dieses Gerät existiert bereits eine Reservierung in diesem Zeitraum");
            }
        }
        if (!br.hasErrors()) {
            boolean patientOverlap = reservationRepository.existsOverlapForPatient(form.getPatientId(), start, end);
            if (patientOverlap) {
                br.reject("overlap_patient", "Termine überschneiden sich: Dieser Patient hat bereits eine Reservierung in diesem Zeitraum");
            }
        }

        if (br.hasErrors()) {
            model.addAttribute("patients", patientRepository.findAll());
            model.addAttribute("devices", deviceRepository.findAll());
            model.addAttribute("bodyRegions", List.of("Kopf", "Brust", "Abdomen", "Wirbelsäule", "Extremitäten"));
            return "add_reservation";
        }

        Reservation res = new Reservation();
        res.setPatient(patientOpt.get());
        res.setDevice(deviceOpt.get());
        res.setStartDateTime(start);
        res.setEndDateTime(end);
        res.setBodyRegion(form.getBodyRegion());
        res.setComment(form.getComment());
        reservationRepository.save(res);

        return "redirect:/reservation/list/" + form.getDeviceId();
    }

    @GetMapping("/list/{deviceId}")
    public String listForDevice(@PathVariable Integer deviceId, Model model) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            return "redirect:/device/list";
        }
        model.addAttribute("device", deviceOpt.get());
        model.addAttribute("reservations", reservationRepository.findByDeviceIdOrderByStartDateTimeAsc(deviceId));
        return "reservation_list";
    }

    public static class ReservationForm {
        private Integer patientId;
        private Integer deviceId;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate date;

        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        private LocalTime startTime;

        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        private LocalTime endTime;

        private String bodyRegion;
        private String comment;

        public Integer getPatientId() { return patientId; }
        public void setPatientId(Integer patientId) { this.patientId = patientId; }
        public Integer getDeviceId() { return deviceId; }
        public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
        public String getBodyRegion() { return bodyRegion; }
        public void setBodyRegion(String bodyRegion) { this.bodyRegion = bodyRegion; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}
