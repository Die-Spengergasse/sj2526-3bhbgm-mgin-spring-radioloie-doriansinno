package at.spengergasse.spring_thymeleaf.controllers;

import at.spengergasse.spring_thymeleaf.entities.Patient;
import at.spengergasse.spring_thymeleaf.entities.PatientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/patient")
public class PatientController {
    private final PatientRepository patientRepository;

    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @GetMapping("/list")
    public String patients(Model model) {
        model.addAttribute("patients", patientRepository.findAll());
        return "patlist";
    }

    @GetMapping("/add")
    public String addPatient(Model model) {
        model.addAttribute("patient", new Patient());
        return "add_patient";
    }

    @PostMapping("/add")
    public String addPatient(@ModelAttribute("patient") Patient patient, BindingResult br, Model model) {
        if (patient.getSvnr() == null || !patient.getSvnr().matches("\\d{10}")) {
            br.rejectValue("svnr", "svnr.invalid", "Die Sozialversicherungsnummer muss genau 10 Ziffern haben.");
        } else if (patientRepository.existsBySvnr(patient.getSvnr())) {
            br.rejectValue("svnr", "svnr.duplicate", "Dieser Patient ist mit dieser Sozialversicherungsnummer bereits registriert.");
        }

        if (patient.getFirstName() == null || patient.getFirstName().isBlank()) {
            br.rejectValue("firstName", "firstName.required", "Vorname ist erforderlich.");
        }

        if (patient.getLastName() == null || patient.getLastName().isBlank()) {
            br.rejectValue("lastName", "lastName.required", "Nachname ist erforderlich.");
        }

        if (patient.getGender() == null || patient.getGender().isBlank()) {
            br.rejectValue("gender", "gender.required", "Geschlecht ist erforderlich.");
        } else if (!patient.getGender().matches("M|W|D")) {
            br.rejectValue("gender", "gender.invalid", "Bitte ein g\u00fcltiges Geschlecht ausw\u00e4hlen.");
        }

        if (patient.getBirth() == null) {
            br.rejectValue("birth", "birth.required", "Geburtsdatum ist erforderlich.");
        } else if (patient.getBirth().isAfter(LocalDate.now())) {
            br.rejectValue("birth", "birth.future", "Geburtsdatum darf nicht in der Zukunft liegen.");
        }

        if (br.hasErrors()) {
            model.addAttribute("patient", patient);
            return "add_patient";
        }

        patientRepository.save(patient);
        return "redirect:/patient/list";
    }
}
