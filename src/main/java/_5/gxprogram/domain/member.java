package _5.gxprogram.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //JPA를 위한 기본 생성자는 보호 유지
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

    public member(String name) {
        this.name = name;
    } //테스트에 필요한 public 생성자 (디폴트가 protected여서)

    public member (String name, String pw, String studentId, memberRole role, memberStatus status) {
        this.name = name;
        this.password = pw;
        this.studentId = studentId;
        this.role = role;
        this.status = status;
    }
}
