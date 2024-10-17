package kroryi.his.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserSearchCriteria {

    private String id;
    private String name;
    private String role;
    private String startDate; // 날짜는 문자열로 받아서 나중에 변환

}

