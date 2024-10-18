package kroryi.his.repository.search;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kroryi.his.domain.*;
import kroryi.his.dto.MemberListAllDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
public class MemberSearchImpl extends
        QuerydslRepositorySupport
        implements MemberSearch {

    private final JPAQueryFactory queryFactory;

    public MemberSearchImpl(JPAQueryFactory queryFactory) {
        super(Member.class);
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<Member> search(Pageable pageable) {

        QMember member = QMember.member;
        JPQLQuery<Member> query = from(member);

        BooleanBuilder builder = new BooleanBuilder();
        // name = '1'
        builder.or(member.name.contains("1"));
        // or 연결
        // content = '11'
        builder.or(member.email.contains("11"));
        // where ( title ='1' or content='11')
        query.where(builder);

        // and 연결
        // where (      or     ) and bno > 10
        query.where(member.mid.like("%10L%"));

        // select * from board where ( title = '1' or content = '11 ) and bno > 10;
        // pageable 0, 10

        this.getQuerydsl().applyPagination(pageable, query);
        // select * from board where ( title = '1' or content = '11 ) and bno > 10 limit ? , ?;

        List<Member> list = query.fetch();
        System.out.println("--------->" + list);

        long count = query.fetchCount();

        return null;
    }

    @Override
    public Page<Member> searchAll(String[] types, String keyword, Pageable pageable) {
        QMember member = QMember.member;
        JPQLQuery<Member> query = from(member);

        if ((types != null) && (types.length > 0) && keyword != null) {
            BooleanBuilder builder = new BooleanBuilder();
            for (String type : types) {
                switch (type) {
                    case "n":
                        builder.or(member.name.like('%' + keyword + '%'));
                        break;
                    case "e":
                        builder.or(member.email.like('%' + keyword + '%'));
                        break;
                    case "r":
                        builder.or(member.roleSet.any().roleSet.eq(MemberRole.valueOf(keyword)));
                        break;
                    case "d":
                        try {
                            // keyword를 LocalDate로 변환
                            LocalDate dateKeyword = LocalDate.parse(keyword, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            // 특정 날짜 범위로 검색 (해당 날짜의 시작과 끝)
                            LocalDateTime startOfDay = dateKeyword.atStartOfDay();
                            LocalDateTime endOfDay = dateKeyword.atTime(23, 59, 59);
                            builder.or(member.regDate.between(startOfDay, endOfDay));
                        } catch (DateTimeParseException e) {
                            // 날짜 형식이 맞지 않으면 처리 (필요에 따라 예외 처리)
                            System.out.println("Invalid date format: " + keyword);
                        }
                        break;
                    case "nerd":
                        builder.or(member.name.like('%' + keyword + '%'));
                        builder.or(member.email.like('%' + keyword + '%'));
                        builder.or(member.roleSet.any().roleSet.eq(MemberRole.valueOf(keyword)));
                        try {
                            // keyword를 LocalDate로 변환
                            LocalDate dateKeyword = LocalDate.parse(keyword, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            // 특정 날짜 범위로 검색 (해당 날짜의 시작과 끝)
                            LocalDateTime startOfDay = dateKeyword.atStartOfDay();
                            LocalDateTime endOfDay = dateKeyword.atTime(23, 59, 59);
                            builder.or(member.regDate.between(startOfDay, endOfDay));
                        } catch (DateTimeParseException e) {
                            // 날짜 형식이 맞지 않으면 처리 (필요에 따라 예외 처리)
                            System.out.println("Invalid date format: " + keyword);
                        }
                        break;

                }
            }
            query.where(builder);
        }

        query.where(member.mid.like("%0L%"));

        this.getQuerydsl().applyPagination(pageable, query);

        List<Member> list = query.fetch();
        long count = query.fetchCount();

        Page<Member> memberPage = new PageImpl<>(list, pageable, count);

        return memberPage;
    }

    @Override
    public Page<MemberListAllDTO> searchWithAll(String[] types, String keyword, Pageable pageable) {

        QMember member = QMember.member;
        QMemberRoleSet roleSet = QMemberRoleSet.memberRoleSet;

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if ((types != null && types.length > 0) && keyword != null) {
            for (String type : types) {
                switch (type) {
                    case "n":
                        booleanBuilder.or(member.name.contains(keyword));
                        break;
                    case "e":
                        booleanBuilder.or(member.email.contains(keyword));
                        break;
                    case "r":
                        booleanBuilder.or(member.roleSet.any().roleSet.eq(MemberRole.valueOf(keyword)));
                        break;
                }
            }
        }
        // 전체 개수 계산을 위한 쿼리 (페이징 미적용)
        JPQLQuery<Member> memberJPQLQuery = from(member);

        memberJPQLQuery.leftJoin(roleSet).on(roleSet.member.eq(member));
        memberJPQLQuery.where(booleanBuilder);
        memberJPQLQuery.groupBy(member);
        getQuerydsl().applyPagination(pageable,memberJPQLQuery);


//        JPQLQuery<Member> memberJPQLQuery = from(member)
//                .where(booleanBuilder)
//                .groupBy(member);

        long totalCount = memberJPQLQuery.fetchCount();  // deprecated 메서드는 fetch().size()로 변경 가능

        // Tuple에서 페이징 처리
        List<Tuple> tupleList = getQuerydsl()
                .applyPagination(pageable,
                        queryFactory
                                .select(
                                        member.mid,
                                        member.email,
                                        member.name,
                                        roleSet.roleSet
                                )
                                .from(member)
//                                .groupBy(member)
                                .where(booleanBuilder)
                                .leftJoin(member.roleSet, roleSet)
                )
                .fetch();

        System.out.println("2222222222222222222222222->" + tupleList);




        // Tuple에서 DTO로 변환
        List<MemberListAllDTO> dtoList = tupleList.stream().map(tuple -> {
            String mid = tuple.get(member.mid);   // 개별 필드 추출
            String name = tuple.get(member.name);
            String email = tuple.get(member.email);

            MemberRole roles = tuple.get(roleSet.roleSet);
            System.out.println("22222222211111112333331111->" + tuple.get(member.mid)+ ":" + tuple.get(member.roleSet));

            MemberRoleSet roleSetEntity = new MemberRoleSet();
            roleSetEntity.setRoleSet(roles);  // ADMIN, EMP 등 역할에 맞는 Enum 변환

            // 역할 리스트 생성
            Set<MemberRoleSet> roleSetList = new HashSet<>();
            roleSetList.add(roleSetEntity);  // 각 사용자의 역할을 리스트에 추가

            System.out.println("2222222221111111233333->" + roles);
//            System.out.println("2222222221111111233334->" + roleNames);



            // DTO로 변환
            return MemberListAllDTO.builder()
                    .mid(mid)
                    .name(name)
                    .email(email)
                    .role(roleSetList)
                    .build();
        }).collect(Collectors.toList());

        log.info("totalCount->{}", totalCount);


        return new PageImpl<>(dtoList, pageable, totalCount);
    }

}
