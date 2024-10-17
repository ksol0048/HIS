package kroryi.his.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @Column
    private String rno;

    @Column
    private int rv_time;

    @Column
    private String name;

    @Column
    private String content;
}
