package kroryi.his.service.Impl;

import kroryi.his.domain.Board;
import kroryi.his.domain.Reply;
import kroryi.his.dto.PageRequestDTO;
import kroryi.his.dto.PageResponseDTO;
import kroryi.his.dto.ReplyDTO;
import kroryi.his.repository.BoardRepository;
import kroryi.his.repository.ReplyRepository;
import kroryi.his.service.ReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReplyServiceImpl implements ReplyService {

    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final ModelMapper modelMapper;

    @Value("${paging.range}")
    private int defaultPageRange;


    @Override
    public Long register(ReplyDTO replyDTO) {
        Board board = boardRepository.findById(replyDTO.getBno())
                        .orElseThrow(()-> new IllegalArgumentException("게시물 없음."));

        Reply reply = modelMapper.map(replyDTO, Reply.class);
        reply.setBoard(board);

        //reply 객체에 Board.bno 번호가 없는 글을 가지고 있다면
        //DataIntegrityViolationException 발생하면 CustomAdvice에 작성한 핸들러 동작해서 예외 문자를 클라이언트에 전송
        Long rno = replyRepository.save(reply).getRno();
        return rno;
    }

    @Override
    public ReplyDTO read(Long rno) {
        Optional<Reply> replyOptional = replyRepository.findById(rno);
        Reply reply = replyOptional.orElseThrow();


        return modelMapper.map(reply, ReplyDTO.class);
    }

    @Override
    public void modify(ReplyDTO replyDTO) {
        Optional<Reply> replyOptional = replyRepository.findById(replyDTO.getRno());
        Reply reply = replyOptional.orElseThrow();

        // 책에는 Reply.java change 메서드 추가 해야 함.
        reply.change(replyDTO.getReplyText());
        replyRepository.save(reply);

    }

    @Override
    public void remove(Long rno) {

        replyRepository.deleteById(rno);
    }

    @Override
    public PageResponseDTO<ReplyDTO> getListOfBoard(Long bno, PageRequestDTO pageRequestDTO) {

        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() <0 ? 0 : pageRequestDTO.getPage() -1,
                pageRequestDTO.getSize(),
                Sort.by("rno").ascending());
        Page<Reply> result = replyRepository.listOfBoard(bno, pageable);
        List<ReplyDTO> dtoList =
                result.getContent().stream().map( reply -> modelMapper.map(reply, ReplyDTO.class))
                        .collect(Collectors.toList());


        return PageResponseDTO.<ReplyDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .pageRange(defaultPageRange) // 책에 없는것 추가 한것
                .build();
    }
}
