package kroryi.his.controller;

import jakarta.validation.Valid;
import kroryi.his.service.BoardService;
import kroryi.his.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequestMapping("/board")
@Log4j2
@RestController
public class BoardController {

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    @Autowired
    private final BoardService boardService;

    @Autowired
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/list")
    public String list(PageRequestDTO pageRequestDTO, Model model) {
        if (pageRequestDTO.getPage() < 1) {
            pageRequestDTO.setPage(1);
// 페이지 번호가 1보다 작은 경우 1로 설정
        }
// PageResponseDTO<BoardDTO> responseDTO = boardService.list(pageRequestDTO);
//        PageResponseDTO<BoardListReplyCountDTO> responseDTO =
//                boardService.listWithReplyCount(pageRequestDTO);
        //Board Reply BoardImage 3가지 포함된 것
        PageResponseDTO<BoardListAllDTO> responseDTO =
                boardService.listWithAll(pageRequestDTO);

        log.info(responseDTO);

        model.addAttribute("responseDTO", responseDTO);

        return "board/list";
    }

    @PreAuthorize("isAuthenticated()")  // 로그인 인가된 모든 사용자
//    @PreAuthorize("hasAnyRole('EMP','ADMIN','MANAGER')") 이 설정은 or EMP나 ADMIN 중 하나이면 허가
    @GetMapping("/register")
    public String register(Model model) {

        return "board/register";
    }

    @PostMapping("/register")
    public String registerPost(@Valid BoardDTO boardDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("-------register -------");
        if (bindingResult.hasErrors()) {
            log.info("에러 발생");
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/board/register";
        }
        log.info("22222222222222222--{}", boardDTO);
        Long bno = boardService.register(boardDTO);
        redirectAttributes.addFlashAttribute("result", bno);

        //http://localhost:8080/boart/list?bno=xxxx
        return "redirect:/board/list";

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/read")
    public String read(Long bno, PageRequestDTO pageRequestDTO, Model model) {
        log.info("-------read -------");
        BoardDTO boardDTO = boardService.readOne(bno);
        log.info("/board/list ---> {}", boardDTO);
        model.addAttribute("dto", boardDTO);

        return "board/read";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify")
    public String modify(Long bno, PageRequestDTO pageRequestDTO, Model model) {
        log.info("-------modify -------");
        BoardDTO boardDTO = boardService.readOne(bno);
        log.info("/board/modify ---> {}", boardDTO);
        model.addAttribute("dto", boardDTO);

        return "board/modify";
    }


    //    @PreAuthorize("((principal instanceof T(org.springframework.security.core.userdetails.UserDetails) " +
//            "and principal.username == #boardDTO.writer) " +
//            "or (principal instanceof T(org.springframework.security.oauth2.core.user.OAuth2User) " +
//            "and principal.attributes['kakao_account']['email'] == #boardDTO.writer))")
//    @PreAuthorize("@authUtils.getPrincipalUsername(authentication) == #boardDTO.writer")
    @PostMapping("/modify")
    public String modify(@Valid BoardDTO boardDTO,
                         PageRequestDTO pageRequestDTO,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        log.info("-------modify post -------");

        if (bindingResult.hasErrors()) {
            log.info("/modify post 에러 발생 : {}", bindingResult.getAllErrors());
            String link = pageRequestDTO.getLink();
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            redirectAttributes.addAttribute("bno", boardDTO.getBno());
            return "redirect:/board/modify?" + link;
        }

        boardService.modify(boardDTO);
        redirectAttributes.addFlashAttribute("result", "수정됨");
        redirectAttributes.addAttribute("bno", boardDTO.getBno());

        // /board/read?bno=xxxx
        return "redirect:/board/read";
    }

    //    @PreAuthorize("((principal instanceof T(org.springframework.security.core.userdetails.UserDetails) " +
//            "and principal.username == #boardDTO.writer) " +
//            "or (principal instanceof T(org.springframework.security.oauth2.core.user.OAuth2User) " +
//            "and principal.attributes['kakao_account']['email'] == #boardDTO.writer))")
//    @PreAuthorize("@authUtils.getPrincipalUsername(authentication) == #boardDTO.writer")
    @PostMapping("/remove")
    public String remove(BoardDTO boardDTO, RedirectAttributes redirectAttributes) {
        Long bno = boardDTO.getBno();
        log.info("게시물 삭제 {}", bno);
        boardService.remove(bno);
        log.info(boardDTO.getFileNames());
        List<String> fileNames = boardDTO.getFileNames();
        if (fileNames != null && fileNames.size() > 0) {
            removeFiles(fileNames);
        }
        redirectAttributes.addFlashAttribute("result", "removed");
        return "redirect:/board/list";
    }

    public void removeFiles(List<String> files) {
        for (String fileName : files) {
            Resource resource =
                    new FileSystemResource(uploadPath + File.separator + fileName);
            String resourceName = resource.getFilename();
            try {
                String contentType = Files.probeContentType(resource.getFile().toPath());
                resource.getFile().delete();

                if (contentType.startsWith("image")) {
                    File thumbnailFile = new File(uploadPath + File.separator + "S_" + fileName);
                    thumbnailFile.delete();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }


}
