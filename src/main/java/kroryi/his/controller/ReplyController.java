package kroryi.his.controller;

import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import kroryi.his.service.ReplyService;
import kroryi.his.dto.PageRequestDTO;
import kroryi.his.dto.PageResponseDTO;
import kroryi.his.dto.ReplyDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/replies")
@Log4j2
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    @ApiOperation(value="Replise POST" ,notes="POST 방식으로 댓글 등록")
    @PostMapping(value="/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> register(@Valid @RequestBody ReplyDTO replyDTO,
                                                     BindingResult bindingResult) throws BindException {

        log.info("replyDTO----->{}", replyDTO);
// replyer을 빼고 전송하면 bindingResult에 아래 메세지를 클라이언트로 전송 한다.
//        {
//            "replyer": "널이어서는 안됩니다"
//        }
        if(bindingResult.hasErrors()) {
//            bindingResult.rejectValue("bno","값을 넣어야 합니다.");
            throw new BindException(bindingResult);
        }

        //백앤드에서 등록이 완료된 후에 프론앤드로 데이터 응답을 할때
        // 응답코드, 응답메세제, 등록된 결과물 등을 전송해서
        // 프론트에서 활용 할 수 있다.

        Map<String,Long> resultMap = new HashMap<>();
        Long rno = replyService.register(replyDTO);

//        resultMap.put("message", "등록에 성공했습니다.");
        resultMap.put("success_code", 500L);
        resultMap.put("rno", rno);

        return resultMap;

    }

    @ApiOperation(value =" Replies of Board", notes = "GET 방식으로 특정 게시물의 댓글 목록")
    @GetMapping(value = "/list/{bno}")
    public PageResponseDTO<ReplyDTO> getList(@PathVariable("bno") Long bno,
                                             PageRequestDTO pageRequestDTO){
        PageResponseDTO<ReplyDTO> responseDTO =
                replyService.getListOfBoard(bno,pageRequestDTO);

        return responseDTO;
    }

    @ApiOperation(value =" Replies of Reply", notes = "GET 방식으로 특정 게시물의 댓글 조회")
    @GetMapping(value = "/{rno}")
    public ReplyDTO getReplyDTO(@PathVariable("rno") Long rno){

        return replyService.read(rno);
    }

    @ApiOperation(value="Delete Reply", notes="DELETE방식으로 특정 댓글 삭제")
    @DeleteMapping("/{rno}")
    public Map<String, String> remove(@PathVariable("rno") Long rno){

        replyService.remove(rno);
        Map<String,String> resultMap = new HashMap<>();
        resultMap.put("success_code", "200");
        resultMap.put("msg", "번 댓글을 삭제 했습니다.");
        resultMap.put("rno", String.valueOf(rno));

        return resultMap;
    }

    @ApiOperation(value = "Modify Reply", notes = "PUT 방식으로 특정 댓글 수정")
    @PutMapping(value = "/{rno}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> remove(@PathVariable("rno") Long rno,
                                     @RequestBody ReplyDTO replyDTO)
    {
        replyDTO.setRno(rno);
        // 수정은 Reply 엔티티에 change 메서드 부분에 replyText만 수정하게 되어 있다.
        // replyer까지 수정 하려면 change 메서드를 수정하면 된다.
        replyService.modify(replyDTO);
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success_code", "200");
        resultMap.put("rno", String.valueOf(rno));
        return resultMap;
    }

}
