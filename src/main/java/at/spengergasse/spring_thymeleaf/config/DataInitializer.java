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
                Device d1 = new Device();
                d1.setName("MR-1");
                d1.setType("MR");
                d1.setLocation("R101");

                Device d2 = new Device();
                d2.setName("CT-1");
                d2.setType("CT");
                d2.setLocation("R102");

                Device d3 = new Device();
                d3.setName("XR-1");
                d3.setType("Röntgen");
                d3.setLocation("R103");

                deviceRepository.save(d1);
                deviceRepository.save(d2);
                deviceRepository.save(d3);
            }
        };
    }
}
