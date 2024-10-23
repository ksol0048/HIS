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

// 카운트 쿼리 (roleSet 조인 제외)
        JPQLQuery<Long> countQuery = queryFactory
                .select(member.mid.countDistinct())
                .from(member)
                .groupBy(member.mid, member.email, member.name)  // 중복 제거를 위해 그룹화
                .leftJoin(roleSet).on(roleSet.member.eq(member))

                .where(booleanBuilder);  // roleSet 조인 없이 카운트만 수행

        long totalCount = countQuery.fetchCount();  // deprecated 메서드는 fetch().size()로 변경 가능

        // Tuple에서 페이징 처리
        List<Tuple> tupleList = getQuerydsl()
                .applyPagination(pageable,
                        queryFactory
                                .select(
                                        member.mid,
                                        member.email,
                                        member.name,
                                        member.tel,
                                        member.phone,
                                        roleSet.roleSet
                                )
                                .from(member)
                                .groupBy(member.mid, member.email, member.name, roleSet.roleSet)  // 중복 제거를 위해 그룹화
                                .where(booleanBuilder)
                                .leftJoin(member.roleSet, roleSet)
                )
                .fetch();

        // Tuple에서 DTO로 변환
        List<MemberListAllDTO> dtoList = tupleList.stream()
                .collect(Collectors.groupingBy(tuple -> tuple.get(member.mid)))  // 회원 ID로 그룹핑
                .entrySet()
                .stream()
                .map(entry -> {
                    Tuple tuple = entry.getValue().get(0);  // 첫 번째 튜플
                    String mid = tuple.get(member.mid);
                    String name = tuple.get(member.name);
                    String email = tuple.get(member.email);
                    String tel = tuple.get(member.tel);
                    String phone = tuple.get(member.phone);

                    // 모든 역할을 Set으로 수집
                    Set<MemberRole> roles = entry.getValue().stream()
                            .map(t -> t.get(roleSet.roleSet))  // 각 튜플에서 역할 가져오기
                            .collect(Collectors.toSet());

                    // DTO 생성
                    return MemberListAllDTO.builder()
                            .mid(mid)
                            .name(name)
                            .email(email)
                            .tel(tel)
                            .phone(phone)
                            .roles(roles)  // 역할을 추가
                            .build();
                })
                .collect(Collectors.toList());

        log.info("totalCount->1 {}",  totalCount);
        log.info("totalCount->1 {}",  pageable);
        log.info("totalCount->2 {}",  dtoList.size());
        log.info("totalCount->3 {}", (long) tupleList.size());


        return new PageImpl<>(dtoList, pageable, totalCount);
    }

}
