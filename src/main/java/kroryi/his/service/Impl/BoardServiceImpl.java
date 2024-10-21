package kroryi.his.service.Impl;

import jakarta.transaction.Transactional;
import kroryi.his.service.BoardService;
import kroryi.his.domain.Board;
import kroryi.his.dto.*;
import kroryi.his.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static kroryi.his.domain.QBoard.board;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {
    private final ModelMapper modelMapper;
    private final BoardRepository boardRepository;

    @Value("${paging.range}")
    private int defaultPageRange;

    @Override
    public Long register(BoardDTO dto) {

//        Board board = modelMapper.map(dto, Board.class);
        log.info("regist2222->{}", dto);
        Board board = dtoToEntity(dto);
        log.info("regist33333->{}", board);
        return boardRepository.save(board).getBno();
    }

    @Override
    public BoardDTO readOne(Long id) {
        Optional<Board> result = boardRepository.findById(id);

//        // 아래는 BoardDTO Board 클래스의 필드가 철자가 불일치 할경우 사용
        PropertyMap<Board, BoardDTO> boardMap = new PropertyMap<Board, BoardDTO>() {
            @Override
            protected void configure() {
                map(source.getRegDate()).setRegisterDate(null);
                map(source.getModDate()).setModifyDate(null);
            }
        };

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(boardMap);

        BoardDTO dto = modelMapper.map(board, BoardDTO.class);


        return dto;
    }

    @Override
    public void modify(BoardDTO dto) {

        log.info("modify- dto {}", dto);
        Optional<Board> result = boardRepository.findById(dto.getBno());
        Board board = result.orElseThrow();
        board.change(dto.getTitle(), dto.getContent());

        boardRepository.saveAndFlush(board);
        //영속성 영역의 콘텐트내용을 DB와 동기화 하는 명령
        // save는 즉각 동기화는 하지 않음.
    }

    @Override
    public void remove(Long id) {

        //아래는 댓글도 지우는 것
//        boardRepository.deleteReplyByBoardId(id);
        //댓글은 않지우는 것
        boardRepository.deleteById(id);
    }

    @Override
    public PageResponseDTO<BoardDTO> list(PageRequestDTO pageRequestDTO) {
        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("bno");
        Page<Board> result = boardRepository.searchAll(types, keyword, pageable);

        log.info("list---------> {}", result);

        // 아래는 BoardDTO Board 클래스의 필드가 철자가 불일치 할경우 사용
        PropertyMap<Board, BoardDTO> boardMap = new PropertyMap<Board, BoardDTO>() {
            @Override
            protected void configure() {
                map(source.getRegDate()).setRegisterDate(null);
            }
        };

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(boardMap);

        // resulet에는 total , dtolist,
        List<BoardDTO> dtoList = result.getContent().stream()
                .map(board -> modelMapper.map(board, BoardDTO.class))
                .collect(Collectors.toList());


        return PageResponseDTO.<BoardDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .pageRange(defaultPageRange)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    @Override
    public PageResponseDTO<BoardListReplyCountDTO> listWithReplyCount(PageRequestDTO pageRequestDTO) {
        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("bno");
        Page<BoardListReplyCountDTO> result =
                boardRepository.searchWithReplyCount(types, keyword, pageable);

        // 아래는 BoardDTO Board 클래스의 필드가 철자가 불일치 할경우 사용
        PropertyMap<Board, BoardListReplyCountDTO> boardMap = new PropertyMap<Board, BoardListReplyCountDTO>() {
            @Override
            protected void configure() {
                map(source.getRegDate()).setRegisterDate(null);
            }
        };

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(boardMap);

        // resulet에는 total , dtolist,
        List<BoardListReplyCountDTO> dtoList = result.getContent().stream()
                .map(board -> modelMapper.map(board, BoardListReplyCountDTO.class))
                .collect(Collectors.toList());

        return PageResponseDTO.<BoardListReplyCountDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .pageRange(defaultPageRange)
                .dtoList(result.getContent())
                .total((int)result.getTotalElements())
                .build();
    }

    @Override
    public PageResponseDTO<BoardListAllDTO> listWithAll(PageRequestDTO pageRequestDTO) {

        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("bno");

        Page<BoardListAllDTO> result = boardRepository.searchWithAll(types,keyword,pageable);

        log.info("listWithAll-> {}", result);
        log.info("listWithAll Page-> {}", pageable);

        return PageResponseDTO.<BoardListAllDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(result.getContent())
                .pageRange(defaultPageRange)
                .total((int)result.getTotalElements())
                .build();

    }

}
