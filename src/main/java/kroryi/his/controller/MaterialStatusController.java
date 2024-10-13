package kroryi.his.controller;

import kroryi.his.dto.MaterialTransactionDTO;
import kroryi.his.service.MaterialStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory_management")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@Log4j2
@RequiredArgsConstructor
public class MaterialStatusController {

    private final MaterialStatusService materialStatusService;



}
