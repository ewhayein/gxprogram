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
public class program {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private centerType centerType; // 센터 종류

    @Enumerated(EnumType.STRING)
    private programCategory category; // 종목 분류

    @Column(nullable = false)
    private String name; // 프로그램명 (예: 헬스(1개월), G.X 주3회)

    private Integer price; // 금액 (결제 기준)

    private String difficulty; // 난이도

    private String remarks; // 비고란

    // 양방향 1:N 연관관계 매핑 (하나의 프로그램에 여러 강좌가 속함)
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL)
    private List<course> courses = new ArrayList<>();
}
