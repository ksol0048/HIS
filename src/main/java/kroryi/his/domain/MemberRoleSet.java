package kroryi.his.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
//@IdClass(MemberRoleSetId.class)  // 복합 키 클래스 설정
public class MemberRoleSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)  // MemberRole이 Enum일 경우
    private MemberRole roleSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_mid")
    @JsonBackReference  // 무한 순환 방지
    private Member member;
}