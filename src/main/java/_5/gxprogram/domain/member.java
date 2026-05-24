package _5.gxprogram.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String studentId; // 학번 (로그인 아이디로 활용)

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(nullable = false)
    private String name; // 이름

    private String major; // 전공
    private String familyName; // 직계가족 학생의 학번 (이화가족인 경우)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private memberRole role; // 회원 분류

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private memberStatus status; // 회원 상태
}
