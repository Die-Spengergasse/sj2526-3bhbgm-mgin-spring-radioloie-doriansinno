package at.spengergasse.spring_thymeleaf.controllers;

import at.spengergasse.spring_thymeleaf.entities.Device;
import at.spengergasse.spring_thymeleaf.entities.DeviceRepository;
import at.spengergasse.spring_thymeleaf.entities.Patient;
import at.spengergasse.spring_thymeleaf.entities.PatientRepository;
import at.spengergasse.spring_thymeleaf.entities.Reservation;
import at.spengergasse.spring_thymeleaf.entities.ReservationRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reservation")
public class ReservationController {
    private static final List<String> BODY_REGIONS = List.of(
            "Kopf",
            "Brust",
            "Abdomen",
            "Wirbels\u00e4ule",
            "Extremit\u00e4ten"
    );

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
        model.addAttribute("reservationForm", new ReservationForm());
        addFormData(model);
        return "add_reservation";
    }

    @PostMapping("/add")
    public String addReservation(@ModelAttribute("reservationForm") ReservationForm form,
                                 BindingResult br,
                                 Model model) {
        if (form.getPatientId() == null || form.getDeviceId() == null || form.getDate() == null
                || form.getStartTime() == null || form.getEndTime() == null) {
            br.reject("invalid", "Bitte alle Pflichtfelder ausf\u00fcllen.");
        }

        if (form.getBodyRegion() == null || form.getBodyRegion().isBlank()) {
            br.rejectValue("bodyRegion", "bodyRegion.required", "K\u00f6rperregion ist erforderlich.");
        } else if (!BODY_REGIONS.contains(form.getBodyRegion())) {
            br.rejectValue("bodyRegion", "bodyRegion.invalid", "Bitte eine g\u00fcltige K\u00f6rperregion ausw\u00e4hlen.");
        }

        if (!br.hasErrors() && !form.getEndTime().isAfter(form.getStartTime())) {
            br.reject("time", "Endzeit muss nach der Startzeit liegen.");
        }

        Optional<Patient> patientOpt = form.getPatientId() != null
                ? patientRepository.findById(form.getPatientId())
                : Optional.empty();
        Optional<Device> deviceOpt = form.getDeviceId() != null
                ? deviceRepository.findById(form.getDeviceId())
                : Optional.empty();

        if (!patientOpt.isPresent() || !deviceOpt.isPresent()) {
            br.reject("notfound", "Ausgew\u00e4hlter Patient oder ausgew\u00e4hltes Ger\u00e4t existiert nicht.");
        }

        if (br.hasErrors()) {
            addFormData(model);
            return "add_reservation";
        }

        LocalDateTime start = LocalDateTime.of(form.getDate(), form.getStartTime());
        LocalDateTime end = LocalDateTime.of(form.getDate(), form.getEndTime());

        if (start.isBefore(LocalDateTime.now())) {
            br.reject("past", "Ein Termin in der Vergangenheit darf nicht reserviert werden.");
        }

        if (!br.hasErrors() && reservationRepository.existsOverlapForDevice(form.getDeviceId(), start, end)) {
            br.reject("overlap_device", "Termine \u00fcberschneiden sich: F\u00fcr dieses Ger\u00e4t existiert bereits eine Reservierung in diesem Zeitraum.");
        }

        if (!br.hasErrors() && reservationRepository.existsOverlapForPatient(form.getPatientId(), start, end)) {
            br.reject("overlap_patient", "Termine \u00fcberschneiden sich: Dieser Patient hat bereits eine Reservierung in diesem Zeitraum.");
        }

        if (br.hasErrors()) {
            addFormData(model);
            return "add_reservation";
        }

        Reservation reservation = new Reservation();
        reservation.setPatient(patientOpt.get());
        reservation.setDevice(deviceOpt.get());
        reservation.setStartDateTime(start);
        reservation.setEndDateTime(end);
        reservation.setBodyRegion(form.getBodyRegion());
        reservation.setComment(form.getComment());
        reservationRepository.save(reservation);

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

    private void addFormData(Model model) {
        model.addAttribute("patients", patientRepository.findAll());
        model.addAttribute("devices", deviceRepository.findAll());
        model.addAttribute("bodyRegions", BODY_REGIONS);
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

        public Integer getPatientId() {
            return patientId;
        }

        public void setPatientId(Integer patientId) {
            this.patientId = patientId;
        }

        public Integer getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(Integer deviceId) {
            this.deviceId = deviceId;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }

        public String getBodyRegion() {
            return bodyRegion;
        }

        public void setBodyRegion(String bodyRegion) {
            this.bodyRegion = bodyRegion;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}
