package kroryi.his.service.Impl;

import kroryi.his.domain.Member;
import kroryi.his.domain.MemberRole;
import kroryi.his.domain.MemberRoleSet;
import kroryi.his.dto.MemberJoinDTO;
import kroryi.his.dto.MemberListAllDTO;
import kroryi.his.dto.PageRequestDTO;
import kroryi.his.dto.PageResponseDTO;
import kroryi.his.mapper.UserMapper;
import kroryi.his.repository.MemberRepository;
import kroryi.his.repository.MemberRoleSetRepository;
import kroryi.his.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberRoleSetRepository memberRoleSetRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private UserMapper userMapper;

    @Value("${paging.range}")
    private int defaultPageRange;

    @Override
    public void join(MemberJoinDTO memberJoinDTO) throws MemberService.MidExistException {
        String mid = memberJoinDTO.getMid();
        boolean exist = memberRepository.existsById(mid);
        if (exist) throw new MidExistException();

        // DTO의 roleSet 가져오기
        Set<MemberRoleSet> roleSetDTO = memberJoinDTO.getRoles();
        log.info("Received roleSetDTO: {}", roleSetDTO);

        Member member = modelMapper.map(memberJoinDTO, Member.class);
        member.changePassword(passwordEncoder.encode(memberJoinDTO.getPassword()));

        Set<MemberRoleSet> roleSetEntities = roleSetDTO.stream().map(roleDTO -> {
            MemberRoleSet memberRoleSet = new MemberRoleSet();
            memberRoleSet.setRoleSet(roleDTO.getRoleSet());  // role 필드 매핑
            memberRoleSet.setMember(member);  // Member 엔티티와 연관 설정
            return memberRoleSet;
        }).collect(Collectors.toSet());

        // Member 객체에 roleSetEntities 추가
        member.setRoleSet(roleSetEntities);

        // Member와 연관된 roleSet 확인
        log.info("Member after roleSet mapping: {}", member);
        log.info("Roles: {}", member.getRoleSet());

        // 저장
        memberRepository.save(member);
    }

    @Override
    public void saveUserInfoRemove(List<Map<String, Object>> list) {

        //체크한 항목이 여러개일 수 있으므로
        //리스트로 전달받고 for문 돌려서 1개씩 삭제
        for (Map<String, Object> map : list) {
            userMapper.saveUserInfoRemove(map);
        }
    }

    @Override
    public void saveUserInfo(Map<String, Object> map) {

        String orgPassword = (String) map.get("userPwd");
        String userId = (String) map.get("userId");
        System.out.println("orgPassword: " + orgPassword);
//        String encPassword = initUserPassword(orgPassword, userId);
//        map.put("userPwd", encPassword);
        userMapper.saveUserInfo(map);
    }

    @Override
    public void saveModifyUserInfo(Map<String, Object> map) {

        String orgPassword = (String) map.get("userPwd");
        String userId = (String) map.get("userId");

        if ("".equals(orgPassword)) {
            //수정된 팝업에서 비밀번호를 입력하지 않은 경우에는 업데이트 되지 않도록 공백을 보내줌
            map.put("userPwd", orgPassword);
        } else {
            //비밀번호를 입력한 경우에는 암호화된 비밀번호로 map에 재세팅해서 보내줌

//            String encPassword = initUserPassword(orgPassword, userId);
//            map.put("userPwd", encPassword);
        }
        userMapper.saveModifyUserInfo(map);
    }

    @Override
    public Optional<Member> getUserById(String id) {
        Optional<Member> member = memberRepository.findById(id);
        return member;
    }

    @Override
    public void deleteUser(String id) {

    }

    @Override
    public List<Member> getMembersByRole(MemberRole role) {
        return memberRepository.findByRoleSet(role);
    }

    @Override
    public List<MemberJoinDTO> getMembers() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(member -> new MemberJoinDTO(
                        member.getMid(),
                        member.getName(),
                        member.getPassword(),
                        member.getEmail(),
                        member.isRetirement(),
                        member.getSocial(),
                        member.getRoleSet()
                ))
                .collect(Collectors.toList());
    }


    @Override
    public PageResponseDTO<MemberJoinDTO> list(PageRequestDTO pageRequestDTO) {
        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("mid");
        Page<Member> result = memberRepository.searchAll(types, keyword, pageable);

        log.info("list---------> {}", result);

        // 아래는 BoardDTO Board 클래스의 필드가 철자가 불일치 할경우 사용
        PropertyMap<Member, MemberJoinDTO> boardMap = new PropertyMap<Member, MemberJoinDTO>() {
            @Override
            protected void configure() {
                map(source.getRoleSet()).setRoles(null);
            }
        };
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(boardMap);

        // resulet에는 total , dtolist,
        List<MemberJoinDTO> dtoList = result.getContent().stream()
                .map(board -> modelMapper.map(board, MemberJoinDTO.class))
                .collect(Collectors.toList());


        return PageResponseDTO.<MemberJoinDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .pageRange(defaultPageRange)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    @Override
    public PageResponseDTO<MemberListAllDTO> listWithAll(PageRequestDTO pageRequestDTO) {

        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("mid");

        Page<MemberListAllDTO> result = memberRepository.searchWithAll(types,keyword,pageable);

        log.info("listWithAll-> {}", result);
        log.info("listWithAll Page-> {}", pageable);

        return PageResponseDTO.<MemberListAllDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(result.getContent())
                .pageRange(defaultPageRange)
                .total((int)result.getTotalElements())
                .build();

    }

}