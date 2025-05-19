package bpmn.com.bpmn.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Process;
import io.camunda.zeebe.client.api.response.Topology;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ZeebeDeploymentConfig {

    private final ZeebeClient zeebeClient;

    // ✅ BPMN Süreç ID'leri
    private static final String THERAPIST_BPMN_PROCESS_ID = "Process_Patient_Therapist_ID";

    // ✅ BPMN Dosya Yolları
    private static final String THERAPIST_BPMN_FILE_PATH = "src/main/resources/bpmn/camundaTherapistAssignment.bpmn";

    @PostConstruct
    public void deployProcesses() {
        try {
            System.out.println("📌 Zeebe Deployment başlatılıyor...");

            // 📌 **Zeebe Broker'ın çalıştığını kontrol et**
            System.out.println("📌 Zeebe Broker kontrolü yapılıyor...");
            Topology topology = zeebeClient.newTopologyRequest().send().join();
            if (topology.getBrokers().isEmpty()) {
                System.err.println("❌ Zeebe Broker çalışmıyor veya erişilemiyor!");
                return;
            }
            System.out.println("✅ Zeebe Broker çalışıyor!");

            // **BPMN dosyalarının mevcut olup olmadığını kontrol et**
            System.out.println("📌 BPMN dosyası kontrolü yapılıyor...");
            File bpmnFile = new File(THERAPIST_BPMN_FILE_PATH);
            if (!bpmnFile.exists()) {
                throw new RuntimeException("❌ BPMN dosyası bulunamadı: " + bpmnFile.getAbsolutePath());
            }
            System.out.println("✅ BPMN dosyası bulundu: " + bpmnFile.getAbsolutePath());

            // **Süreçleri deploy et**
            System.out.println("📌 BPMN Süreci Deploy Ediliyor...");
            try (FileInputStream fis = new FileInputStream(bpmnFile)) {
                DeploymentEvent deploymentEvent = zeebeClient.newDeployResourceCommand()
                        .addResourceStream(fis, bpmnFile.getName())
                        .send()
                        .join();

                List<Process> deployedProcesses = deploymentEvent.getProcesses();
                System.out.println("📊 Deploy edilen process sayısı: " + deployedProcesses.size());

                deployedProcesses.forEach(process -> {
                    System.out.println("✅ BPMN Süreci Başarıyla Deploy Edildi:");
                    System.out.println("   - Process ID: " + process.getBpmnProcessId());
                    System.out.println("   - Version: " + process.getVersion());
                    System.out.println("   - Key: " + process.getProcessDefinitionKey());
                });
            }

            // **Process'in deploy edildiğini doğrula**
            System.out.println("📌 Process deploy durumu kontrol ediliyor...");
            List<Process> processes = zeebeClient.newDeployResourceCommand()
                    .addResourceFile(THERAPIST_BPMN_FILE_PATH)
                    .send()
                    .join()
                    .getProcesses();

            boolean processExists = processes.stream()
                    .anyMatch(p -> THERAPIST_BPMN_PROCESS_ID.equals(p.getBpmnProcessId()));

            if (processExists) {
                System.out.println("✅ Process başarıyla deploy edildi ve Operate'de görünür olmalı!");
            } else {
                System.out.println("⚠️ Process deploy edildi ancak Operate'de görünmüyor olabilir!");
            }

        } catch (Exception e) {
            System.err.println("❌ BPMN Süreci Deploy Edilemedi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}