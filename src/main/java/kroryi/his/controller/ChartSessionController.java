package kroryi.his.controller;

import jakarta.servlet.http.HttpSession;
import kroryi.his.dto.RequestData;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Log4j2
@RequiredArgsConstructor
@RestController
public class ChartSessionController {
    //치아의 List를 관리하긴 2중 List
    List<List<String>> toothList = null;
    //치아의 증상을 관리하기위한 2중 List
    List<List<String>> symptomList = null;
    //치아의 메모를 관리하기위한 2중 List
    List<List<String>> memoList = null;
    //위 List들의 데이터 위치를 관리하기위한 List
    List<List<String>> arrayList = null;
    // List들의 종합적인 데이터 관리를 위한 2중List
    List<List<String>> piList = null;
    List<List<String>> nestedList;


    @PostMapping("/his/addDelete-to-subList")
    private ResponseEntity<?> addDeleteToSubList(@RequestBody RequestData data, HttpSession session) {
        List<String> newValues = data.getNewValues();
        int subListIndex = data.getSubListIndex();
        boolean addDel = data.isAddOrDelete();

        List<String> innerList = nestedList.get(subListIndex);

        if (nestedList == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No data found in session.");
        }
        if (addDel) {
            for (int i = 0; i < newValues.size(); i++) {
                boolean check = innerList.contains(newValues.get(i));
                if (!check) {

                    // 원하는 subList가 존재하는지 확인하고 값 추가
                    if (subListIndex < nestedList.size() && nestedList.get(subListIndex) != null) {
                        innerList.add(newValues.get(i)); // 값 추가 (push와 동일한 동작)
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sublist not found.");
                    }
                }
            }
            dataArray(innerList, subListIndex, data.getListIndex());
//            session.setAttribute("nestedList", nestedList);
            return ResponseEntity.ok(newValues);
        } else {

            for (int i = 0; i < newValues.size(); i++) {
                if (subListIndex < nestedList.size() && nestedList.get(subListIndex) != null) {

                    int finalI = i;
                    innerList.removeIf(item -> item.equals(newValues.get(finalI))); // 값 추가 (push와 동일한 동작)
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sublist not found.");
                }
            }
            dataArray(innerList, subListIndex, data.getListIndex());
//            session.setAttribute("nestedList", nestedList);
            return ResponseEntity.ok(newValues);
        }
    }


    // 세션에서 배열을 가져오는 엔드포인트
    @GetMapping("/his/get-session-items")
    public List<List<String>> getSessionItems(HttpSession session) {

        // 세션에서 "nestedList" 가져오기
        nestedList = (List<List<String>>) session.getAttribute("nestedList");
        // 세션에 값이 없을 경우 빈 이중 리스트를 반환
        if (nestedList == null) {
            nestedList = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                nestedList.add(new ArrayList<>());
            }
        }

        if (piList == null) {
            piList = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                piList.add(new ArrayList<>());
            }
        }
        nestedList = piList;

        return piList;
    }


    private ResponseEntity<?> dataArray(List<String> subArrayList, int subListIndex, int listIndex) {
        int listnum = listIndex + 3;
        List<String> listIndexList = new ArrayList<>();
        listIndexList.add(String.valueOf(listIndex));

        if (toothList == null) {
            toothList = new ArrayList<>();
            toothList.add(new ArrayList<>());
        }
        ;
        if (symptomList == null) {
            symptomList = new ArrayList<>();
            symptomList.add(new ArrayList<>());
        }
        ;
        if (memoList == null) {
            memoList = new ArrayList<>();
            memoList.add(new ArrayList<>());
        }
        if (arrayList == null) {
            arrayList = new ArrayList<>();
            arrayList.add(new ArrayList<>());
        }
        int piSize = toothList.size() + symptomList.size() + memoList.size() + arrayList.size();
        if (piList == null) {
            piList = new ArrayList<>();
            for (int i = 0; i < piSize; i++) {
                piList.add(new ArrayList<>());
            }
            log.info("piList.size() ----->{}", piList.size());
        }
        log.info("12312312313{}", listIndexList);

        switch (subListIndex) {
            case 0:
                toothList.set(listIndex, subArrayList);
                piList.set(listIndex, toothList.get(listIndex));
                arrayList.set(listIndex, listIndexList);
                piList.set(listnum, arrayList.get(listIndex));
                break;
            case 1:
                symptomList.set(listIndex, subArrayList);
                piList.set(subListIndex, symptomList.get(listIndex));
                arrayList.set(listIndex, subArrayList);
                piList.set(listnum, arrayList.get(listIndex));
                break;
            case 2:
                memoList.set(listIndex, subArrayList);
                piList.set(subListIndex, memoList.get(listIndex));
                arrayList.set(listIndex, subArrayList);
                piList.set(listnum, arrayList.get(listIndex));
                break;

            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid subListIndex.");
        }

        log.info("piList ------> {}", piList);

        return null;
    }

}