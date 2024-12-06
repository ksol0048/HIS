package kroryi.his.controller;

import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kroryi.his.domain.LoginRequest;
import kroryi.his.domain.LoginResponse;
import kroryi.his.domain.Member;
import kroryi.his.domain.MemberRoleSet;
import kroryi.his.dto.*;
import kroryi.his.exception.UserNotFoundException;
import kroryi.his.jwt.JwtUtil;
import kroryi.his.repository.MemberRepository;
import kroryi.his.repository.MemberRoleSetRepository;
import kroryi.his.service.MemberRoleSetService;
import kroryi.his.service.MemberService;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@Log4j2
@RequiredArgsConstructor
public class UserApiController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final MemberRoleSetService memberRoleSetService;
    private final MemberRoleSetRepository memberRoleSetRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Member> user = memberService.getUserById(request.getMid());
        if (user.isPresent() && passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
            String token = jwtUtil.generateToken(user.get().getMid());
            return ResponseEntity.ok(new LoginResponse(token));
        }
        return ResponseEntity.badRequest().body("Invalid credentials");
    }
    @GetMapping("/")
    @ResponseBody
    public List<MemberJoinDTO> getMembers() {
        return memberService.getMembers(); // DTO 리스트 반환
    }



    @ApiOperation(value = "회원 등록 POST", notes = "POST 방식으로 회원 등록")
    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> saveUser(@Valid @RequestBody MemberJoinDTO memberJoinDTO, BindingResult bindingResult) throws BindException {

        log.info("roleset->", memberJoinDTO.getRoles());
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        Map<String, Object> response = new HashMap<>();
        log.info("response---------{}", response);
        try {
            log.info("~~~~~~~~~~~~: {}", memberJoinDTO);
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

        String id = memberJoinDTO.getMid();

        Member member = memberRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 사용자가 없습니다. id=" + id)
        );

        // 1. 비밀번호 처리
        if (memberJoinDTO.getPassword() == null || memberJoinDTO.getPassword().isEmpty()) {
            // 비밀번호가 null이거나 비어 있을 경우 기존 비밀번호 유지
            memberJoinDTO.setPassword(member.getPassword());
        } else {
            // 비밀번호가 변경된 경우 필요에 따라 암호화 처리
            String encodedPassword = passwordEncoder.encode(memberJoinDTO.getPassword());
            memberJoinDTO.setPassword(encodedPassword);
        }


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



    @GetMapping("/paginglist")
    @ResponseBody
    public PageResponseDTO<MemberListAllDTO> getPagingList(PageRequestDTO pageRequestDTO, Model model, @RequestParam("page") int page) {
        if (pageRequestDTO.getPage() < 1) {
            pageRequestDTO.setPage(1);
        }
        log.info("Paging------> {}", pageRequestDTO);
        PageResponseDTO<MemberListAllDTO> responseDTO =
                memberService.listWithAll(pageRequestDTO);
        return responseDTO; // DTO 리스트 반환
    }

    // JWT를 사용한 회원 정보 관리

    @GetMapping("/getProfile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7); // "Bearer " 제거
        String username = jwtUtil.validateToken(token);
        Optional<Member> user = memberService.getUserById(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(HttpServletRequest request, @RequestBody Member updatedUser) {
        String token = request.getHeader("Authorization").substring(7);
        String username = jwtUtil.validateToken(token);
        Optional<Member> user = memberService.getUserById(username);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.get().setEmail(updatedUser.getEmail());
        user.get().setSocial(updatedUser.getSocial());
        memberRepository.save(user.get());

        return ResponseEntity.ok(user);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAccount(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String username = jwtUtil.validateToken(token);
        Optional<Member> user = memberRepository.findById(username);

        if (user != null) {
            memberRepository.delete(user.get());
            return ResponseEntity.ok("User deleted successfully");
        }
        return ResponseEntity.notFound().build();
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
        Optional<Member> member = Optional.ofNullable(memberService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id)));
        return member;
    }

    @PostMapping("/searchUsers")
    @ResponseBody
    public List<Member> searchUsers(@RequestBody Map<String, String> params) {
        String userId = params.get("mid");
        String userName = params.get("name");
        String email = params.get("email");
        String userRole = params.get("role");
        String startDate = params.get("startDate");
        log.info("searchUsers--> {}", userId);

        // 검색 조건을 사용해 사용자 목록 필터링
        return memberRepository.findByIdOrUsernameOrEmailAndRolesIn(userId, userName, email, userRole);
    }

    @GetMapping("/checkId")
    public ResponseEntity<Boolean> checkDuplicateId(@RequestParam String mid) {
        boolean isDuplicate = memberService.isDuplicateMemberId(mid);
        return ResponseEntity.ok(isDuplicate);  // true: 중복, false: 사용 가능
    }

}


