package kroryi.his.controller;


import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
public class HomeController {
    @GetMapping("/home")
    public String home() {
        return "home";
    }

//    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping("/admin_management")
    public String adminManagement() {
        return "admin_management";
    }

    //    환자등록
    @GetMapping("/patient_register")
    public String patient_register() {
        return "patient_register";
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
}
