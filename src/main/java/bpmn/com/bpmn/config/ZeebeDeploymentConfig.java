package bpmn.com.bpmn.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Topology;
import io.camunda.zeebe.client.api.response.Process;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ZeebeDeploymentConfig {

    private final ZeebeClient zeebeClient;

    // ✅ BPMN Süreç ID'leri
    private static final String THERAPIST_BPMN_PROCESS_ID = "Process_Patient_Therapist_ID";
    private static final String GOOGLE_CALENDAR_BPMN_PROCESS_ID = "Google_Calendar_Process";

    // ✅ BPMN Dosya Yolları
    private static final String THERAPIST_BPMN_FILE_PATH = "src/main/resources/bpmn/camundaAssignTherapist.bpmn";
    private static final String GOOGLE_CALENDAR_BPMN_FILE_PATH = "src/main/resources/bpmn/googleCalendar.bpmn";

    @PostConstruct
    public void deployProcesses() {
        try {
            // 📌 **Zeebe Broker'ın çalıştığını kontrol et**
            Topology topology = zeebeClient.newTopologyRequest().send().join();
            if (topology.getBrokers().isEmpty()) {
                System.err.println("❌ Zeebe Broker çalışmıyor veya erişilemiyor!");
                return;
            }

            // 📌 **Zaten deploy edilmiş süreçleri kontrol et**
            List<Process> deployedProcesses = zeebeClient.newDeployResourceCommand()
                    .addResourceFile(THERAPIST_BPMN_FILE_PATH)
                    .addResourceFile(GOOGLE_CALENDAR_BPMN_FILE_PATH)
                    .send()
                    .join()
                    .getProcesses();

            boolean therapistProcessExists = deployedProcesses.stream()
                    .anyMatch(p -> THERAPIST_BPMN_PROCESS_ID.equals(p.getBpmnProcessId()));

            boolean googleCalendarProcessExists = deployedProcesses.stream()
                    .anyMatch(p -> GOOGLE_CALENDAR_BPMN_PROCESS_ID.equals(p.getBpmnProcessId()));

            // Eğer her iki süreç de deploy edilmişse, tekrardan deploy etme
            if (therapistProcessExists && googleCalendarProcessExists) {
                System.out.println("✅ Süreçler zaten deploy edilmiş!");
                return;
            }

            System.out.println("📌 BPMN Süreçleri Deploy Ediliyor...");

            // **BPMN dosyalarının mevcut olup olmadığını kontrol et**
            validateFileExists(THERAPIST_BPMN_FILE_PATH);
            validateFileExists(GOOGLE_CALENDAR_BPMN_FILE_PATH);

            // **Süreçleri deploy et**
            DeploymentEvent deploymentEvent = zeebeClient.newDeployResourceCommand()
                    .addResourceFile(THERAPIST_BPMN_FILE_PATH)
                    .addResourceFile(GOOGLE_CALENDAR_BPMN_FILE_PATH)
                    .send()
                    .join();

            deploymentEvent.getProcesses().forEach(process ->
                    System.out.println("✅ BPMN Süreci Başarıyla Deploy Edildi: " + process.getBpmnProcessId())
            );

        } catch (Exception e) {
            System.err.println("❌ BPMN Süreci Deploy Edilemedi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 📌 Belirtilen dosyanın var olup olmadığını kontrol eder.
     */
    private void validateFileExists(String filePath) {
        File bpmnFile = new File(filePath);
        if (!bpmnFile.exists()) {
            throw new RuntimeException("❌ BPMN dosyası bulunamadı: " + bpmnFile.getAbsolutePath());
        }
    }
}
