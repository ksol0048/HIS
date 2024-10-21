package kroryi.his.service;

import kroryi.his.dto.PageRequestDTO;
import kroryi.his.dto.PageResponseDTO;
import kroryi.his.dto.ReplyDTO;

public interface ReplyService {
    Long register(ReplyDTO replyDTO);

    ReplyDTO read(Long rno);
    void modify(ReplyDTO replyDTO);
    void remove(Long rno);

    PageResponseDTO<ReplyDTO> getListOfBoard(Long bno, PageRequestDTO pageRequestDTO);
}
