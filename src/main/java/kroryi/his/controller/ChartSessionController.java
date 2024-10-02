package kroryi.his.controller;

import jakarta.servlet.http.HttpSession;
import kroryi.his.dto.RequestData;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Log4j2
@RequiredArgsConstructor
@RestController
public class ChartSessionController {

    List<List<List<String>>> piList = null;
    List<List<String>> toothList = null;
    List<List<String>> symptomList = null;
    List<List<String>> memoList = null;
    List<Integer> arrayList = null;

    @PostMapping(value = "/his/adds-to-sublist")
    private ResponseEntity<?> addsToSubList(@RequestBody RequestData data, HttpSession session) {
        List<String> newValues = data.getNewValues();
        int subListIndex = data.getSubListIndex();
        List<List<String>> nestedList = getSessionItems(session);
        List<String> innerList = nestedList.get(subListIndex);
        for (int i = 0; i < newValues.size(); i++) {
            boolean check = (innerList).contains(newValues.get(i));
            if (check) {
                i++;
            } else {
                addToSubList(subListIndex, newValues.get(i), session);
            }
        }
        return ResponseEntity.ok(newValues);
    }

    @PostMapping("/his/addDelete-to-subList")
    private ResponseEntity<?> addDeleteToSubList(@RequestBody RequestData data, HttpSession session) {
        List<String> newValues = data.getNewValues();
        int subListIndex = data.getSubListIndex();
        boolean addDel = data.isAddOrDelete();
        if(addDel) {
            List<List<String>> nestedList = getSessionItems(session);
            List<String> innerList = nestedList.get(subListIndex);
            for (int i = 0; i < newValues.size(); i++) {
                boolean check = (innerList).contains(newValues.get(i));
                if (check) {
                    i++;
                } else {
                    addToSubList(subListIndex, newValues.get(i), session);
                }
            }
            return ResponseEntity.ok(newValues);
        } else {

            for (int i = 0; i < newValues.size(); i++) {
                deleteToSubList(subListIndex, newValues.get(i), session);
            }
            return ResponseEntity.ok(newValues);
        }
    }

    @PostMapping(value = "/his/deletes-to-sublist", consumes = "application/json")
    private ResponseEntity<?> deletesToSubList(@RequestBody RequestData data, HttpSession session) {

        log.info("requestData ------> {}", data.getNewValues());
        log.info("requestData ------> {}", data.getSubListIndex());
        log.info("requestData ------> {}", data.getListIndex());
        log.info("requestData -----> {}",data.isAddOrDelete());
        List<String> newValue = data.getNewValues();
        int subListIndex = data.getSubListIndex();

        log.info(data.getNewValues());
        for (int i = 0; i < newValue.size(); i++) {
            deleteToSubList(subListIndex, newValue.get(i), session);
        }
        return ResponseEntity.ok(newValue);
    }


    @PostMapping("/his/add-to-sublist")
    @ResponseBody
    private String addToSubList(@RequestParam int subListIndex, @RequestParam String newValue, HttpSession session) {
        // 세션에서 이중 리스트 가져오기
        List<List<String>> nestedList = getSessionItems(session);

        if (nestedList == null) {
            return "No data found in session.";
        }

        // 원하는 subList가 존재하는지 확인하고 값 추가
        if (subListIndex < nestedList.size() && nestedList.get(subListIndex) != null) {
            List<String> innerList = nestedList.get(subListIndex); // subList[0] 배열 가져오기
            innerList.add(newValue); // 값 추가 (push와 동일한 동작)
        } else {
            return "Sublist not found.";
        }

        // 변경된 이중 리스트를 세션에 다시 저장
        session.setAttribute("nestedList", nestedList);
        return "Value added to sublist.";
    }

    @PostMapping("/his/change-to-sublist")
    @ResponseBody
    public String deleteToSubList(@RequestParam int subListIndex, @RequestParam String newValue, HttpSession session) {
        // 세션에서 이중 리스트 가져오기
        List<List<String>> nestedList = getSessionItems(session);

        if (nestedList == null) {
            return "No data found in session.";
        }
        // 원하는 subList가 존재하는지 확인하고 값 추가
        if (subListIndex < nestedList.size() && nestedList.get(subListIndex) != null) {
            List<String> innerList = nestedList.get(subListIndex); // subList[0] 배열 가져오기
            innerList.removeIf(item -> item.equals(newValue)); // 값 추가 (push와 동일한 동작)
        } else {
            return "Sublist not found.";
        }

        // 변경된 이중 리스트를 세션에 다시 저장
        session.setAttribute("nestedList", nestedList);
        return "Value added to sublist.";
    }

    // 세션에서 배열을 가져오는 엔드포인트
    @GetMapping("/his/get-session-items")
    public List<List<String>> getSessionItems(HttpSession session) {
        // 세션에서 "nestedList" 가져오기
        List<List<String>> nestedList = (List<List<String>>) session.getAttribute("nestedList");
        // 세션에 값이 없을 경우 빈 이중 리스트를 반환
        if (nestedList == null) {
            nestedList = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                nestedList.add(new ArrayList<>());
                if (i == 3) {
                    nestedList.get(i).add("0");
                }
            }
        }


        return nestedList;
    }

    // 환자의 선택된 치아별 증상을 3중 배열에 저장. [[1,2,3치아의 상태], [5,6 치아의 상태},.....]
    public List<?> piList(List<List<String>> nestedList, int arrayIndex) {
        if (piList == null) {
            piList = new ArrayList<>();
        }
        if (!piList.contains(nestedList)) {
            piList.add(nestedList);
        }


        List<List<String>> piArray = piList.get(arrayIndex);


        return piArray;
    }

    private ResponseEntity<?> dataArray(@RequestBody RequestData data) {
        List<String> newValues = data.getNewValues();
        int subListIndex = data.getSubListIndex();

        if (toothList == null) {
            toothList = new ArrayList<>();
        }
        ;
        if (symptomList == null) {
            symptomList = new ArrayList<>();
        }
        ;
        if (memoList == null) {
            memoList = new ArrayList<>();
        }
        if (arrayList == null) {
            arrayList = new ArrayList<>();
        }



        return null;
    }

}