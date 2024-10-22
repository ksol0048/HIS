package kroryi.his;

import kroryi.his.domain.Member;
import kroryi.his.domain.MemberRole;
import kroryi.his.domain.PatientRegister;
import kroryi.his.domain.PatientRegisterMemo;
import kroryi.his.repository.MemberRepository;
import kroryi.his.service.MemberService;
import kroryi.his.service.PatientRegisterService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Log4j2
public class UserTests {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    PatientRegisterService patientRegisterService;

    @Test
    public void testSearchName(){

        List<Member> Members = memberService.getMembersByRole(MemberRole.EMP);
        for (Member Member : Members) {
            log.info(Member.toString());
        }
    }

    @Test
    public void testSearchDoctor() {
        // 주어진 역할에 해당하는 회원의 이름을 가져옵니다.
        List<String> doctorNames = patientRegisterService.getDoctorNames();

        // 결과를 로깅합니다.
        doctorNames.forEach(name -> log.info("Doctor Name: {}", name));
    }

}
