package com.hospital.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/** Full prescription with its line items. */
@Data
public class PrescriptionResponse {
    private Long                       id;
    private Long                       appointmentId;
    private Long                       doctorId;
    private String                     doctorName;
    private Long                       patientId;
    private String                     patientName;
    private String                     diagnosis;
    private String                     advice;
    private LocalDateTime              createdAt;
    private List<PrescriptionItemResponse> items;

    @Data
    public static class PrescriptionItemResponse {
        private Long   id;
        private String medicineName;
        private String dosage;
        private String frequency;
        private Integer durationDays;
        private String instructions;
    }
}
