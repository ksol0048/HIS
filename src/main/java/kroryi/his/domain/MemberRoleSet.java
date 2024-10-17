package kroryi.his.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@IdClass(MemberRoleSetId.class)  // 복합 키 클래스 설정
public class MemberRoleSet {

    @Id
    @ManyToOne
    @JoinColumn(name = "member_mid")
    @JsonBackReference  // 자식 엔티티에서 역참조 방지
    private Member member;

    @Id
    @Enumerated(EnumType.STRING)
    private MemberRole roleSet;

    // Getters and setters
}