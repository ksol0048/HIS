package kroryi.his.domain;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Board extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;

    @Column(length = 200, nullable = false)
    private String title;
    @Column(length = 2000, nullable = false)
    private String content;
    @Column(length = 50, nullable = false)
    private String writer;
    @Column(length = 50, nullable = true)
    private String address;



    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE)
    private List<Reply> replies = new ArrayList<>();

//    Hibernate:
//    select
//    is1_0.board_bno,
//    is1_0.uuid,
//    is1_0.file_name,
//    is1_0.ord
//            from
//    board_image is1_0
//    where
//    is1_0.board_bno in (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)



    public void change(String title, String content) {
        this.title = title;
        this.content = content;
    }

}
