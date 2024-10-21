package kroryi.his.repository;

import kroryi.his.domain.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    @Query("select r from Reply r where r.board.bno = :bno order by r.rno desc")
    Page<Reply> listOfBoard(Long bno, Pageable pageable);

    void deleteByBoard_Bno(Long bno);

}
