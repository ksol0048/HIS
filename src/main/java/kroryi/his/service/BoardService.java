package kroryi.his.service;


import kroryi.his.dto.BoardDTO;
import kroryi.his.dto.PageRequestDTO;
import kroryi.his.dto.PageResponseDTO;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface BoardService {
    Long register(BoardDTO dto);

    BoardDTO readOne(Long id);

    void modify(BoardDTO dto);

    void remove(Long id);

    PageResponseDTO<BoardDTO> list(PageRequestDTO pageRequestDTO);
}
