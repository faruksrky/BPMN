package bpmn.com.bpmn.worker;

import bpmn.com.bpmn.service.TherapistFeignClient;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TherapistAssignmentWorker {

    @Autowired
    private TherapistFeignClient therapistFeignClient;

    @Autowired
    private ZeebeClient zeebeClient; // ✅ ZeebeClient doğrudan enjekte ediliyor

    /**
     * ✅ Hasta kaydını gerçekleştiren işçi (Zeebe Worker)
     */
    @JobWorker(type = "register-patient")
    public void registerPatient(final ActivatedJob job) {
        try {
            System.out.println("📌 Worker: register-patient başladı!"); // ✅ Log ekle
            Map<String, Object> patientData = job.getVariablesAsMap();

            // 📌 Therapist microservice üzerinden hastayı kaydet
            Map<String, Object> patientResponse = therapistFeignClient.registerPatient(patientData);

            // 🔥 API'den geçerli bir yanıt alındı mı kontrol et
            if (patientResponse == null || !patientResponse.containsKey("patientId")) {
                throw new RuntimeException("Hasta kaydı başarısız! API'den geçerli bir yanıt alınamadı.");
            }

            String patientId = patientResponse.get("patientId").toString();

            // 📌 Hasta ID'sini Zeebe iş akışına geri gönder
            zeebeClient.newCompleteCommand(job.getKey())
                    .variables(Map.of("patientId", patientId))
                    .send()
                    .join();

        } catch (Exception e) {
            zeebeClient.newFailCommand(job.getKey())
                    .retries(0)
                    .errorMessage("Hasta kaydı başarısız: " + e.getMessage())
                    .send()
                    .join();
        }
    }

    /**
     * ✅ Terapist atama işlemini gerçekleştiren işçi (Zeebe Worker)
     */
    @JobWorker(type = "assign-therapist")
    public void assignTherapist(final ActivatedJob job) {
        try {
            System.out.println("📌 Worker: assign-patient başladı!"); // ✅ Log ekle
            Map<String, Object> variables = job.getVariablesAsMap();

            // 📌 Therapist microservice'inden uygun terapistleri al
            Map<String, Object> therapistData = therapistFeignClient.getAvailableTherapists();

            if (therapistData == null || therapistData.isEmpty()) {
                throw new RuntimeException("Uygun terapist bulunamadı!");
            }

            // 📌 Terapist listesini Zeebe sürecine geri gönder
            zeebeClient.newCompleteCommand(job.getKey())
                    .variables(Map.of("availableTherapists", therapistData))
                    .send()
                    .join();
        } catch (Exception e) {
            zeebeClient.newFailCommand(job.getKey())
                    .retries(0)
                    .errorMessage("Therapist atanamadı: " + e.getMessage())
                    .send()
                    .join();
        }
    }
}
