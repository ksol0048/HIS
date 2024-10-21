package kroryi.his.repository;

import kroryi.his.domain.Board;
import kroryi.his.repository.search.BoardSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardSearch {

    @Query("select b from Board b where b.title like concat('%',:keyword,'%')")
    Page<kroryi.his.domain.Board> findKeyword(String keyword, Pageable pageable);


    @Query(value="select  now()", nativeQuery = true)
    String getTime();

    @Modifying
    @Query("DELETE FROM Reply C WHERE C.board.bno=:bno")
    void deleteReplyByBoardId(@Param("bno") Long bno);

}
