package _5.gxprogram.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class account {

    public account(member member, String accountNumber, Integer balance) {
        this.member = member;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private member member;

    @Column(length = 10, nullable = false)
    private String accountNumber; // 10자리 계좌번호

    @Column(nullable = false)
    private Integer balance; // 잔액 (결제 검증용)
}
