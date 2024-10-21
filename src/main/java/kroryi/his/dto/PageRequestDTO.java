package kroryi.his.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class PageRequestDTO {
    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;
    private String type;

    private String keyword;


    public String[] getTypes(){
        if(type== null || type.isEmpty()){
            return null;
        }
        // "tw,t,twc"
        // split(",")
        return type.split("");
    }

    // String ... 가변인자
    // getPageble(String "aa", String "bb",,,,)
    public Pageable getPageable(String... props){

        return PageRequest.of(this.page - 1, this.size, Sort.by(props).descending());

    }

    private String link;

    public String getLink(){
        // 페이지 번호에 링크를 걸기 위한 것
        // ?page=${page}&size=10&type=tw
        if(link == null){
            StringBuilder builder = new StringBuilder();
            builder.append("page=" + this.page);
            builder.append("&size=" + this.size);
            if(type != null && type.length() > 0){
                builder.append("&type=" + type);
            }
            if(keyword != null){
                try{
                    // ?page=1&size=10&keyword=세종대왕
                    // 세종대왕 한글 깨지말라고 인코딩함.
                    builder.append("&keyword="+ URLEncoder.encode(keyword, "UTF-8"));
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
            }
            link = builder.toString();

        }
        return link;
    }

}
