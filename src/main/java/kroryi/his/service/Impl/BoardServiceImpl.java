package kroryi.his.service.Impl;

import jakarta.transaction.Transactional;
import kroryi.his.domain.Board;
import kroryi.his.dto.BoardDTO;
import kroryi.his.dto.PageRequestDTO;
import kroryi.his.dto.PageResponseDTO;
import kroryi.his.repository.BoardRepository;
import kroryi.his.service.BoardService;
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

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class BoardServiceImpl implements BoardService {
    private final ModelMapper modelMapper;
    private final BoardRepository boardRepository;

    @Value("${paging.range}")
    private int defaultPageRange;

    @Override
    public Long register(BoardDTO boardDTO) {
//        Board board = dtoToEntity(boardDTO);

//        Long bno = boardRepository.save(board).getBno();

        Board board = modelMapper.map(boardDTO, Board.class);
        return boardRepository.save(board).getBno();
//        return bno;
    }

    @Override
    public BoardDTO readOne(Long bno) {
        Optional<Board> result = boardRepository.findById(bno);
        Board board = result.orElseThrow();

        BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);

        return boardDTO;

        /*PropertyMap<Board, BoardDTO> boardMap = new PropertyMap<Board, BoardDTO>() {
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
        Optional<Board> result = boardRepository.findByIdWithImages(bno);

        Board board = result.orElseThrow();

        BoardDTO boardDTO = entityToDTO(board);

        return boardDTO;*/
    }

    @Override
    public void modify(BoardDTO dto) {
        Optional<Board> result = boardRepository.findById(dto.getBno());
        Board board = result.orElseThrow();
        board.change(dto.getTitle(), dto.getContent());

        /*board.clearImages();

        if (dto.getFileNames() != null) {
            dto.getFileNames().forEach(fileName -> {
                try {
                    // UUID와 파일명을 저장할 때 URL 인코딩 처리
                    String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());

                    // 정규식으로 UUID 다음의 파일명을 추출
                    Pattern pattern = Pattern.compile("([0-9a-fA-F\\-]+)_(.+)"); // UUID와 파일명을 분리하는 정규식
                    Matcher matcher = pattern.matcher(encodedFileName);

                    if (matcher.find()) {
                        String uuid = matcher.group(1);    // UUID 부분
                        String originalFileName = URLDecoder.decode(matcher.group(2), StandardCharsets.UTF_8.toString());  // 파일명 부분
                        System.out.println("UUID: " + uuid);
                        System.out.println("Original File Name: " + originalFileName);

                        board.addImage(uuid, originalFileName);
                    } else {
                        System.out.println("파일명 패턴이 일치하지 않습니다: " + fileName);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }*/

        boardRepository.save(board);

        //영속성 영역의 콘텐트내용을 DB와 동기화 하는 명령
        // save는 즉각 동기화는 하지 않음
    }

    @Override
    public void remove(Long id) {
        // 댓글있으면 안지움
        boardRepository.deleteById(id);

        // 댓글있어도 삭제
//        boardRepository.deleteReplyByBoard(id);
    }

    @Override
    public PageResponseDTO<BoardDTO> list(PageRequestDTO pageRequestDTO) {
        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("bno");
        Page<Board> result = boardRepository.searchAll(types, keyword, pageable);

        PropertyMap<Board, BoardDTO> boardMap = new PropertyMap<Board, BoardDTO>() {
            @Override
            protected void configure() {
                map(source.getRegDate()).setRegDate(null);
            }
        };

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(boardMap);

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

}
