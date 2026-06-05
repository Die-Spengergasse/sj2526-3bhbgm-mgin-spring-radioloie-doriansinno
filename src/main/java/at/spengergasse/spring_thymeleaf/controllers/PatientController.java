package at.spengergasse.spring_thymeleaf.controllers;

import at.spengergasse.spring_thymeleaf.entities.Patient;
import at.spengergasse.spring_thymeleaf.entities.PatientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.validation.BindingResult;
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
            br.reject("svnr.invalid", "Ungültige Sozialversicherungsnummer: Es müssen genau 10 Ziffern sein");
        }

        if (patient.getBirth() != null && patient.getBirth().isAfter(LocalDate.now())) {
            br.reject("birth.future", "Geburtsdatum darf nicht in der Zukunft liegen");
        }

        if (br.hasErrors()) {
            model.addAttribute("patient", patient);
            return "add_patient";
        }

        patientRepository.save(patient);
        return  "redirect:/patient/list";
    }
}
