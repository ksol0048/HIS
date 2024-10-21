package kroryi.his.controller;

import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import kroryi.his.domain.Member;
import kroryi.his.dto.*;
import kroryi.his.repository.MemberRepository;
import kroryi.his.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Controller
@RequestMapping("/admin_management")
@Log4j2
@RequiredArgsConstructor
public class UserController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    @GetMapping("/")
    @ResponseBody
    public List<MemberJoinDTO> getMembers() {
        return memberService.getMembers(); // DTO 리스트 반환
    }

    @GetMapping("/paginglist")
    @ResponseBody
    public PageResponseDTO<MemberListAllDTO> getPagingList(PageRequestDTO pageRequestDTO, Model model,@RequestParam("page") int page) {
        if (pageRequestDTO.getPage() < 1) {
            pageRequestDTO.setPage(1);
        }
        log.info("Paging------> {}", pageRequestDTO);
        PageResponseDTO<MemberListAllDTO> responseDTO =
                memberService.listWithAll(pageRequestDTO);
        return responseDTO; // DTO 리스트 반환
    }


    @ApiOperation(value = "회원 등록 POST", notes = "POST 방식으로 회원 등록")
    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> saveUser(@Valid @RequestBody MemberJoinDTO memberJoinDTO, BindingResult bindingResult) throws BindException {
        log.info("userDTO---------{}", memberJoinDTO);

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        Map<String, Object> response = new HashMap<>();
        log.info("response---------{}", response);
        try {
            log.info("~~~~~~~~~~~~: {}",memberJoinDTO);
            memberService.join(memberJoinDTO);
            response.put("success", true);
            response.put("message", "등록 성공");
        } catch (Exception e) {
            log.error("Error saving user", e); // 에러 로그 출력
            response.put("success", false);
            response.put("message", "에러 발생");
        }
        return response;
    }

    @RequestMapping("/admin/admin_management")
    public String main() {

        return "admin_management";
    }

    //등록된 사용자 삭제
    @RequestMapping("/admin/saveUserInfoRemove.do")
    @ResponseBody
    public Map<String, Object> saveUserInfoRemove(@RequestBody List<Map<String, Object>> param) {

        memberService.saveUserInfoRemove(param);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", true);

        return result;
    }


    //신규 사용자 등록
    @RequestMapping("/admin/saveUserInfo.do")
    @ResponseBody
    public Map<String, Object> saveUserInfo(@RequestBody Map<String, Object> param) {

        memberService.saveUserInfo(param);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", true);

        return result;
    }


    //사용자 정보 수정
    @RequestMapping("/admin/saveModifyUserInfo.do")
    @ResponseBody
    public Map<String, Object> saveModifyUserInfo(@RequestBody Map<String, Object> param) {

        memberService.saveModifyUserInfo(param);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", true);

        return result;
    }

    @GetMapping("/edit/{id}")
    public String showUpdateUserForm(@PathVariable("id") String id, Model model) {
        Optional<Member> member = memberService.getUserById(id);
        model.addAttribute("member");
        return "member-form";
    }

    @GetMapping("/editform/{id}")
    @ResponseBody
    public Optional<Member> getEditUserForm(@PathVariable("id") String id) {
        Optional<Member> member = memberService.getUserById(id);
        log.info("getEditUserForm -> {}", member);
        return member;
    }


    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") String id) {
        memberService.deleteUser(id);
        return "redirect:/users";
    }

   /* @PostMapping("/searchUsers")
    @ResponseBody
    public List<Member> searchUsers(@RequestBody Map<String, String> params,@RequestParam("page") int page) {
        String userId = params.get("mid");
        String userName = params.get("username");
        String userRole = params.get("role");
        String startDate = params.get("startDate");

        // 검색 조건을 사용해 사용자 목록 필터링
        return memberRepository.findUsersByConditions(userId, userName, userRole, startDate);
    }*/
}
