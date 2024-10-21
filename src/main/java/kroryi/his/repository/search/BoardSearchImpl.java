package kroryi.his.repository.search;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import kroryi.his.domain.Board;
import kroryi.his.domain.QBoard;
import kroryi.his.domain.QReply;
import kroryi.his.dto.BoardListAllDTO;
import kroryi.his.dto.BoardListReplyCountDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class BoardSearchImpl extends
        QuerydslRepositorySupport
        implements BoardSearch {

    public BoardSearchImpl() {
        super(Board.class);
    }

    @Override
    public Page<Board> search(Pageable pageable) {

        QBoard board = QBoard.board;
        JPQLQuery<Board> query = from(board);

        BooleanBuilder builder = new BooleanBuilder();
        // title = '1'
        builder.or(board.title.contains("1"));
        // or 연결
        // content = '11'
        builder.or(board.content.contains("11"));
        // where ( title ='1' or content='11')
        query.where(builder);

        // and 연결
        // where (      or     ) and bno > 10
        query.where(board.bno.gt(10L));

        // select * from board where ( title = '1' or content = '11 ) and bno > 10;
        // pageable 0, 10

        this.getQuerydsl().applyPagination(pageable, query);
        // select * from board where ( title = '1' or content = '11 ) and bno > 10 limit ? , ?;

        List<Board> list = query.fetch();
        System.out.println("--------->" + list);

        long count = query.fetchCount();

        return null;
    }

    @Override
    public Page<Board> searchAll(String[] types, String keyword, Pageable pageable) {
        QBoard board = QBoard.board;
        JPQLQuery<Board> query = from(board);

        if ((types != null) && (types.length > 0) && keyword != null) {
            BooleanBuilder builder = new BooleanBuilder();
            for (String type : types) {
                switch (type) {
                    case "t":
                        builder.or(board.title.like('%' + keyword + '%'));
                        break;
                    case "c":
                        builder.or(board.content.like('%' + keyword + '%'));
                        break;
                    case "w":
                        builder.or(board.writer.like('%' + keyword + '%'));
                        break;
                    case "tc":
                        builder.or(board.title.like('%' + keyword + '%'));
                        builder.or(board.content.like('%' + keyword + '%'));
                        break;
                    case "tcw":
                        builder.or(board.title.like('%' + keyword + '%'));
                        builder.or(board.content.like('%' + keyword + '%'));
                        builder.or(board.writer.like('%' + keyword + '%'));
                        break;

                }
            }
            query.where(builder);
        }

        query.where(board.bno.gt(0L));

        this.getQuerydsl().applyPagination(pageable, query);

        List<Board> list = query.fetch();
        long count = query.fetchCount();

        Page<Board> boardPage = new PageImpl<>(list, pageable, count);

        return boardPage;
    }

    @Override
    public Page<BoardListReplyCountDTO> searchWithReplyCount(String[] types, String keyword, Pageable pageable) {
        //Querydsl을 사용 하려면 QEntity
        // mvn clean install    -> target Q파일들이 생긴다. 생성겨로는 따로 수정 할 수 있다.
        // QEnitiy Entity등록된 디렉토리로 복사 해서 사용.
        QBoard board = QBoard.board;
        QReply reply = QReply.reply;

        // 복잡한 커리문 주로 조인문 작성에 사요
        // select * from board
        JPQLQuery<Board> query = from(board);

        // select * from board
        // left join repley where board.bno = reply.board_bno
        query.leftJoin(reply).on(reply.board.eq(board));
        query.groupBy(board);

        if ((types != null && types.length > 0) && keyword != null) {

            BooleanBuilder booleanBuilder = new BooleanBuilder();
            for (String type : types) {
                switch (type) {
                    case "t":
                        booleanBuilder.or(board.title.contains(keyword));
                        break;
                    case "c":
                        booleanBuilder.or(board.content.contains(keyword));
                        break;
                    case "w":
                        booleanBuilder.or(board.writer.contains(keyword));
                        break;
                    case "d":
                        booleanBuilder.or(reply.replyText.contains(keyword));
                }
            }
            query.where(booleanBuilder);
        }

        query.where(board.bno.gt(0L));

        //검색된 Board, Reply의 결과를 디비에 Select 를 이용해서 새로 쿼리를 만들어서 데이터를 DTO로 변환
        JPQLQuery<BoardListReplyCountDTO> dtoQuery = query.select(
                Projections.bean(BoardListReplyCountDTO.class,
                        board.bno,
                        board.title,
                        board.writer,
                        board.regDate,
                        reply.count().as("replyCount")
                )
        );

        this.getQuerydsl().applyPagination(pageable, dtoQuery);
        List<BoardListReplyCountDTO> dtoList = dtoQuery.fetch();

        long count = dtoQuery.fetchCount();

        return new PageImpl<>(dtoList, pageable, count);
    }

    @Override
    public Page<BoardListAllDTO> searchWithAll(String[] types, String keyword, Pageable pageable) {

        QBoard board = QBoard.board;
        QReply reply = QReply.reply;

//        // Select * from board left join reply on reply.board_id = board.id
//        JPQLQuery<Board> boardJPQLQuery =
//        from(board);
//        boardJPQLQuery.leftJoin(reply).on(reply.board.eq(board));
//        boardJPQLQuery.groupBy(board);
//        getQuerydsl().applyPagination(pageable, boardJPQLQuery);
//
//        // countDistinct 는 중복을 제거하고 난 후의 개수
//        JPQLQuery<Tuple> tupleJPQLQuery =
//                boardJPQLQuery.select(board,
//                        reply.countDistinct());
//
//        List<Tuple> tupleList = tupleJPQLQuery.fetch();

        // Select * from board left join reply on reply.board_id = board.id, apply pagination and count distinct replies
//        List<Tuple> tupleList =
//                 from(board)
//                .leftJoin(reply).on(reply.board.eq(board))
//                .groupBy(board)
//                .select(board, reply.countDistinct())
//                .fetch();
//
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if ((types != null && types.length > 0) && keyword != null) {
            for (String type : types) {
                switch (type) {
                    case "t":
                        booleanBuilder.or(board.title.contains(keyword));
                        break;
                    case "c":
                        booleanBuilder.or(board.content.contains(keyword));
                        break;
                    case "w":
                        booleanBuilder.or(board.writer.contains(keyword));
                        break;
                    case "d":
                        booleanBuilder.or(reply.replyText.contains(keyword));
                }
            }
        }
        // select * from board
        JPQLQuery<Board> boardJPQLQuery = from(board);

        boardJPQLQuery.leftJoin(reply).on(reply.board.eq(board));
        boardJPQLQuery.where(booleanBuilder);
        boardJPQLQuery.groupBy(board);
        getQuerydsl().applyPagination(pageable,boardJPQLQuery);


        List<Tuple> tupleList =
                getQuerydsl()
                        .applyPagination(pageable, from(board)
                        .leftJoin(reply).on(reply.board.eq(board))
                        .groupBy(board)
                        .where(booleanBuilder)
                        .select(board, reply.countDistinct()))
                .fetch();

        log.info("Tuple-->{}", tupleList);

        List<BoardListAllDTO> dtoList =tupleList.stream().map( tuple -> {
            Board board1 = (Board) tuple.get(board);
            long replyCount = tuple.get(1, Long.class);
            BoardListAllDTO dto = BoardListAllDTO.builder()
                    .bno(board1.getBno())
                    .title(board1.getTitle())
                    .writer(board1.getWriter())
                    .regDate(board1.getRegDate())
                    .replyCount(replyCount)
                    .build();


            return dto;
        }).collect(Collectors.toList());

        long totalCount = boardJPQLQuery.fetchCount();
        log.info("totalCount->{}", totalCount);

        return new PageImpl<>(dtoList, pageable, totalCount);
    }

}
