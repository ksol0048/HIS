package kroryi.his.controller;

import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import kroryi.his.domain.Member;
import kroryi.his.domain.MemberRoleSet;
import kroryi.his.dto.*;
import kroryi.his.repository.MemberRepository;
import kroryi.his.repository.MemberRoleSetRepository;
import kroryi.his.service.MemberRoleSetService;
import kroryi.his.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Controller
@RequestMapping("/admin_management")
@Log4j2
@RequiredArgsConstructor
public class UserController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final MemberRoleSetService memberRoleSetService;
    private final MemberRoleSetRepository memberRoleSetRepository;

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

    //사용자 정보 수정
    @ApiOperation(value = "회원 수정 POST", notes = "POST 방식으로 회원 수정")
    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> updateUser(@Valid @RequestBody MemberJoinDTO memberJoinDTO, BindingResult bindingResult) throws BindException, MemberService.MidExistException {

        log.info("update1---> {}", memberJoinDTO);

        String id = memberJoinDTO.getMid();

        Member member = memberRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 사용자가 없습니다. id=" + id)
        );
        // 2. 기존 역할 세트를 가져옴
        Set<MemberRoleSet> existingRoleSets = member.getRoleSet();

        memberRoleSetService.deleteRolesByMemberId(member.getMid());

        // 3. 새로운 역할 세트 가져오기
        Set<MemberRoleSet> newRoleSetsFrom = memberJoinDTO.getRoles();

        // 4. 역할 업데이트 처리
        for (MemberRoleSet newRole : newRoleSetsFrom) {
            Optional<MemberRoleSet> existingRoleSet = existingRoleSets.stream()
                    .filter(roleSet -> roleSet.getRoleSet().equals(newRole.getRoleSet()))
                    .findFirst();

            if (existingRoleSet.isPresent()) {
                // 기존 역할이 존재할 경우 추가적으로 수정이 필요하면 여기에 처리
                MemberRoleSet roleSetToUpdate = existingRoleSet.get();
                roleSetToUpdate.setRoleSet(newRole.getRoleSet()); // 필요한 경우 필드 업데이트
                memberRoleSetRepository.save(roleSetToUpdate);
            } else {
                // 기존 역할이 없을 경우 추가
                MemberRoleSet newRoleSet = new MemberRoleSet();
                newRoleSet.setRoleSet(newRole.getRoleSet());
                newRoleSet.setMember(member);
                memberRoleSetRepository.save(newRoleSet);  // 새로운 역할 세트 저장
                existingRoleSets.add(newRoleSet);
            }
        }

        // 5. 불필요한 역할 제거 (DTO에 없는 역할은 삭제)
        existingRoleSets.removeIf(existingRole ->
                newRoleSetsFrom.stream().noneMatch(newRole ->
                        newRole.getRoleSet().equals(existingRole.getRoleSet())));


        ModelMapper modelMapper = new ModelMapper();
        // 기본 필드 자동 매핑
        Member memberMapper = modelMapper.map(memberJoinDTO, Member.class);

        memberRepository.save(memberMapper);


        log.info("update---1> {}", memberMapper);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", true);

        return result;
    }

    //등록된 사용자 삭제
    //사용자 정보 수정
    @ApiOperation(value = "회원 삭제 POST", notes = "POST 방식으로 회원 삭제")
    @PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> delete(@RequestBody MemberJoinDTO memberJoinDTO, BindingResult bindingResult) throws MemberService.MidExistException {

        memberService.deleteUser(memberJoinDTO);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", true);

        return result;
    }




    @RequestMapping("/admin/admin_management")
    public String main() {

        return "admin_management";
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


    @PostMapping("/searchUsers")
    @ResponseBody
    public List<Member> searchUsers(@RequestBody Map<String, String> params,@RequestParam("page") int page) {
        String userId = params.get("mid");
        String userName = params.get("username");
        String userRole = params.get("role");
        String startDate = params.get("startDate");

        // 검색 조건을 사용해 사용자 목록 필터링
        return memberRepository.findByIdOrUsernameOrEmailAndRolesIn(userId, userName, userRole);
    }
}
