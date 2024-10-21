package kroryi.his.repository.search;

import kroryi.his.domain.Board;
import kroryi.his.dto.BoardListAllDTO;
import kroryi.his.dto.BoardListReplyCountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardSearch {

    Page<Board> search(Pageable pageable);

    Page<Board> searchAll(String[] types, String keyword, Pageable pageable);

    Page<BoardListReplyCountDTO> searchWithReplyCount(String[] types,
                                                      String keyword, Pageable pageable);


    Page<BoardListAllDTO> searchWithAll(String [] types,
                                        String keyword,
                                        Pageable pageable);
}
