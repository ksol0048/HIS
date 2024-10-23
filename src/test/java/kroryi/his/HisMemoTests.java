package kroryi.his;

import kroryi.his.domain.PatientAdmission;
import kroryi.his.domain.PatientRegister;
import kroryi.his.domain.PatientRegisterMemo;
import kroryi.his.dto.PatientMemoDTO;
import kroryi.his.repository.PatientMemoRepository;
import kroryi.his.repository.PatientRegisterRepository;
import kroryi.his.service.PatientAdmissionService;
import kroryi.his.service.PatientRegisterMemoService;
import kroryi.his.service.PatientRegisterService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@SpringBootTest
@Log4j2
//@DataJpaTest
public class HisMemoTests {

    @Autowired
    PatientMemoRepository patientMemoRepository;

    @Autowired
    PatientRegisterRepository patientRegisterRepository;

    @Autowired
    PatientRegisterService patientRegisterService;

    @Autowired
    PatientRegisterMemoService patientRegisterMemoService;

    @Autowired
    PatientAdmissionService patientAdmissionService;

    @Test
    public void testMemoList(){
        List<PatientRegisterMemo> patientMemoDTO = patientMemoRepository.findAll();
        for (PatientRegisterMemo memo : patientMemoDTO) {
            log.info("memo---->{}",memo.toString());
        }

    }

    @Test
    public void testSearchName(){

        List<PatientRegister> registers = patientRegisterService.searchNameByKeyword("이미자");
        for (PatientRegister register : registers) {
            log.info(register.toString());
        }
    }

    @Test
    public void testRemove() {
        String chartNum = "241002007";
        patientRegisterService.remove(chartNum);
        log.info("remove->{}", chartNum);
    }


    @Test
    public void testRegister() {
        PatientMemoDTO patientMemoDTO = PatientMemoDTO.builder()
                .content("ReplyDTO Text")
                .regDate(LocalDate.parse("2020-10-01"))
                .memoCharNum("240911002")
                .build();

        Long mmo = patientRegisterMemoService.register(patientMemoDTO);

        log.info("patientRegisterMemoService.register---->{}", mmo);
        log.info("patientMemoDTO.register---->{}", patientMemoDTO.toString());
    }

    @Test
    public void testSearchChartNum(){
        PatientRegister register = patientRegisterService.getPatient("240930001");
            log.info(register.toString());
    }

    @Test
    public void testSearchTopChart(){
        PatientAdmission register = patientAdmissionService.getLatestCompletionTime(240930001);
        log.info(register.toString());
    }
}
