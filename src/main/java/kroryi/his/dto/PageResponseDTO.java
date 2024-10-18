package kroryi.his.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;

@Getter
@ToString
public class PageResponseDTO<E> {

    private int page;
    private int size;
    private int total;
    private int start;
    private int end;
    private boolean prev;
    private boolean next;

    private List<E> dtoList;

    private final int pageRange;

    @Value("${paging.range}")
    private int defaultPageRange;


    @Builder(builderMethodName = "withAll")
    public PageResponseDTO(PageRequestDTO pageRequestDTO, List<E> dtoList, int total, int pageRange) {
        if (total <= 0) {
            this.total = 0;
            dtoList = Collections.emptyList();
        }

        this.page = pageRequestDTO.getPage();
        this.size = pageRequestDTO.getSize();
        this.total = total;
        this.dtoList = dtoList;

        // pageRange가 0일 경우 기본값을 설정
        this.pageRange = pageRange == -1 ? (defaultPageRange > 0 ? defaultPageRange : 10) : pageRange;
        if (this.pageRange <= 0) {
            throw new IllegalArgumentException("pageRange must be greater than 0");
        }

        // 전체 페이지 수 계산
        int totalPages = (int) Math.ceil((double) total / size);

        // 끝 페이지 계산 (현재 페이지가 속한 그룹의 마지막 페이지)
        this.end = (int) (Math.ceil((double) this.page / this.pageRange)) * this.pageRange;

        // 시작 페이지 계산 (현재 페이지가 속한 그룹의 첫 페이지)
        this.start = this.end - (this.pageRange - 1);

        // 실제 끝 페이지가 전체 페이지 수보다 클 수 없으므로 조정
        this.end = Math.min(this.end, totalPages);

        // 이전 페이지 버튼이 필요한지 여부
        this.prev = this.start > 1;

        // 다음 페이지 버튼이 필요한지 여부
        this.next = total > this.end * this.size;
    }

}
