package at.spengergasse.spring_thymeleaf.config;

import at.spengergasse.spring_thymeleaf.entities.Device;
import at.spengergasse.spring_thymeleaf.entities.DeviceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDevices(DeviceRepository deviceRepository) {
        return args -> {
            if (deviceRepository.count() == 0) {
                Device mr = new Device();
                mr.setName("MR-1");
                mr.setType("MR");
                mr.setLocation("R101");

                Device ct = new Device();
                ct.setName("CT-1");
                ct.setType("CT");
                ct.setLocation("R102");

                Device roentgen = new Device();
                roentgen.setName("XR-1");
                roentgen.setType("R\u00f6ntgen");
                roentgen.setLocation("R103");

                deviceRepository.save(mr);
                deviceRepository.save(ct);
                deviceRepository.save(roentgen);
            }
        };
    }
}
