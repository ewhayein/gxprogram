package _5.gxprogram.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** 장바구니 일괄 결제 신청 요청 DTO */
@Getter @Setter
public class PaymentRequestDTO {

    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @NotEmpty(message = "결제 신청할 강좌를 1개 이상 선택해주세요.")
    private List<Long> applyIds;
}
