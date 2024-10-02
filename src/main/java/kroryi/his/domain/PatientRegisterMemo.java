package kroryi.his.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "patientRegister")
@Builder
@Getter
@Setter
@Table(name = "memo")
public class PatientRegisterMemo {

    // 메모 ID (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mmo")
    private Long mmo;

    // 차트번호 (PatientRegister와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memo_chartnum", referencedColumnName = "chart_num", nullable = false)
    private PatientRegister patientRegister;

    // 등록 날짜
    @Column(name = "reg_date")
    private LocalDateTime regDate;

    // 내용
    @Column(name = "content", length = 1000)
    private String content;

    // 기타 필드
    // 필요한 경우 여기에 추가 필드를 정의할 수 있습니다.
}
