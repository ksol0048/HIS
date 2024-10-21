package kroryi.his.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "board")
@Table(name="Reply", indexes = {
        @Index(name="idx_reply_board_bno", columnList = "board_bno")
})
public class Reply extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rno;

    @ManyToOne
    @JoinColumn(name = "board_bno", nullable = false)
    private Board board;

    private String replyText;
    private String replyer;


    public void change(String replyText) {
        this.replyText = replyText;
    }

}
