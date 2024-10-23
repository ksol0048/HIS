package kroryi.his.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kroryi.his.dto.MemberSecurityDTO;
import kroryi.his.service.PatientRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor
public class HomeController {
    private final PatientRegisterService patientRegisterService;

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails user, Model model, HttpSession session) {
        model.addAttribute("user",user.getUsername());
        log.info("11111111111111 {}", user.getUsername());
        session.setAttribute("user", user);
        log.info("Current Authentication: {}", SecurityContextHolder.getContext().getAuthentication());

        log.info("세선 값 {}", session.getAttribute("user"));
        return "home";
    }

//    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping("/admin_management")
    public String adminManagement() {
        return "admin_management";
    }

    //    환자등록
    @GetMapping("/patient_register")
    public String patientRegister(Model model) {
        List<String> doctorNames = patientRegisterService.getDoctorNames();
        model.addAttribute("doctorNames", doctorNames); // 의사 이름을 모델에 추가
        return "patient_register"; // Thymeleaf 템플릿 이름
    }

    //    진료예약
    @GetMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    //    진료차트
    @GetMapping("/medical_chart")
    public String medical_chart() {
        return "medical_chart";
    }

    //    진료접수
    @GetMapping("/reception")
    public String reception() {
        return "reception";
    }

    //    재고관리
    @GetMapping("/inventory_management")
    public String inventory_management() {
        return "inventory_management";
    }

    @GetMapping("/list")
    public String list() {
        return "board/list";
    }
}
