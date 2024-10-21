package kroryi.his.service;

import kroryi.his.domain.Member;
import kroryi.his.domain.MemberRole;
import kroryi.his.dto.MemberJoinDTO;
import kroryi.his.dto.MemberListAllDTO;
import kroryi.his.dto.PageRequestDTO;
import kroryi.his.dto.PageResponseDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MemberService {

    static class MidExistException extends Exception {

    }
    void join(MemberJoinDTO memberJoinDTO) throws MidExistException;

    //등록된 사용자 정보 삭제
    void saveUserInfoRemove(List<Map<String, Object>> list);

    //신규 사용자 등록 saveUserInfo
    void saveUserInfo(Map<String, Object> map);

    //사용자 정보 수정
    void saveModifyUserInfo(Map<String, Object> map);

    Optional<Member> getUserById(String id);

    void deleteUser(String id);

    List<Member> getMembersByRole(MemberRole role);

    List<MemberJoinDTO> getMembers();

//페이지 검색
    PageResponseDTO<MemberJoinDTO> list(PageRequestDTO pageRequestDTO);

    PageResponseDTO<MemberListAllDTO> listWithAll(PageRequestDTO pageRequestDTO);

    default Member dtoToEntity(MemberJoinDTO memberJoinDTO){

        Member board = Member.builder()
                .mid(memberJoinDTO.getMid())
                .name(memberJoinDTO.getName())
                .email(memberJoinDTO.getEmail())
                .roleSet(memberJoinDTO.getRoles())
                .build();

        return board;
    }
    default MemberJoinDTO entityToDTO(Member member) {
        MemberJoinDTO memberJoinDTO = MemberJoinDTO.builder()
                .mid(member.getMid())
                .name(member.getName())
                .email(member.getEmail())
                .roles(member.getRoleSet())
                .build();

        return memberJoinDTO;
    }

}
