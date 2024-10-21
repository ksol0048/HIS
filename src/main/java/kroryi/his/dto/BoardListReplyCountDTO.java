package kroryi.his.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardListReplyCountDTO {
    private Long bno;
    private String title;
    private String writer;
    private LocalDateTime registerDate;
    private Long replyCount;
}
