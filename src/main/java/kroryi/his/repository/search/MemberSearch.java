package kroryi.his.repository.search;

import kroryi.his.domain.Member;
import kroryi.his.dto.MemberListAllDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberSearch {
    Page<Member> search(Pageable pageable);

    Page<Member> searchAll(String[] types, String keyword, Pageable pageable);


    Page<MemberListAllDTO> searchWithAll(String [] types,
                                         String keyword,
                                         Pageable pageable);

}
