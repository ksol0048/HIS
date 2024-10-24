package kroryi.his.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientDTO{
    private String chartNum;
    private String name;
    private String firstPaResidentNum;
    private String lastPaResidentNum;
    private String gender;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String homeNum;
    private String phoneNum;
    private String email;
    private String defaultAddress;
    private String detailedAddress;
    private String mainDoc;
//    private String dentalHygienist;
    private String visitPath;
    private String category;
    private String tendency;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate firstVisit;
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDate lastVisit;
    private List<PatientMemoDTO> memos;
}