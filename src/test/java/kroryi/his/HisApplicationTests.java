package kroryi.his;

import kroryi.his.dto.BoardDTO;
import kroryi.his.service.BoardService;
import kroryi.his.service.PatientRegisterService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Log4j2
class HisApplicationTests {

    @Autowired
    PatientRegisterService patientRegisterService;

}
