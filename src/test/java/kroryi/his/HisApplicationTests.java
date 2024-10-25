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
    @Autowired
    private BoardService service;

    @Test
    void contextLoads() {
    }

  /*  @Test
    public void getNumber(){
        patientRegisterService.registerPatient();

//        log.info("number---> {}", patientRegisterService.generateChartNum());
    }*/

    @Test
    public void testRegister() {
        log.info("register->{}", service.getClass().getName());

        // 첫 번째 게시물 등록
        BoardDTO boardDTO1 = BoardDTO.builder()
                .title("게시판 등록 1")
                .content("내용 추가 1")
                .writer("작성자 1")
                .build();

        Long bno1 = service.register(boardDTO1);
        log.info("게시물 등록 1 -> {}", bno1);
    }

}
