package bpmn.com.bpmn.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Process;
import io.camunda.zeebe.client.api.response.Topology;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ZeebeDeploymentConfig {

    private final ZeebeClient zeebeClient;

    private static final String BPMN_PROCESS_ID = "Process_Patient_Registration";
    private static final String BPMN_CLASSPATH = "bpmn/camundaTherapistAssignment.bpmn";

    @PostConstruct
    public void deployProcesses() {
        try {
            System.out.println("📌 Zeebe Deployment başlatılıyor...");

            Topology topology = zeebeClient.newTopologyRequest().send().join();
            if (topology.getBrokers().isEmpty()) {
                System.err.println("❌ Zeebe Broker çalışmıyor veya erişilemiyor!");
                return;
            }
            System.out.println("✅ Zeebe Broker çalışıyor!");

            ClassPathResource resource = new ClassPathResource(BPMN_CLASSPATH);
            if (!resource.exists()) {
                throw new RuntimeException("❌ BPMN dosyası bulunamadı: " + BPMN_CLASSPATH);
            }
            System.out.println("✅ BPMN dosyası bulundu: " + BPMN_CLASSPATH);

            System.out.println("📌 BPMN Süreci Deploy Ediliyor...");
            try (InputStream is = resource.getInputStream()) {
                DeploymentEvent deploymentEvent = zeebeClient.newDeployResourceCommand()
                        .addResourceStream(is, "camundaTherapistAssignment.bpmn")
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

                boolean processExists = deployedProcesses.stream()
                        .anyMatch(p -> BPMN_PROCESS_ID.equals(p.getBpmnProcessId()));
                if (processExists) {
                    System.out.println("✅ Process_Patient_Registration deploy edildi!");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ BPMN Süreci Deploy Edilemedi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}