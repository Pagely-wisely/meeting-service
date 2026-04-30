package com.pagely.meetingservice.meeting.infrastructure.provider;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.port.BookProvider;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.infrastructure.client.book.BookApiResponse;
import com.pagely.meetingservice.meeting.infrastructure.client.book.BookClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// Book Service 연동 Adapter
@Slf4j
@Component
@RequiredArgsConstructor
public class BookProviderAdapter implements BookProvider {

    private final BookClient bookClient;

    @Override
    public void validateBook(String bookId) {
        // bookId가 비어 있으면 검증하지 않는다.
        if (bookId == null || bookId.isBlank()) {
            log.info("Book Service 검증 생략: bookId가 비어 있음");
            return;
        }

        log.info("Book Service 내부 API 호출 시작: bookId={}", bookId);

        BookApiResponse response = bookClient.getBook(bookId);

        log.info(
                "Book Service 내부 API 호출 완료: bookId={}, success={}, bookTitle={}",
                bookId,
                response != null && response.success(),
                response != null && response.data() != null ? response.data().title() : null
        );

        if (response == null || !response.success() || response.data() == null) {
            log.warn("Book Service 도서 검증 실패: bookId={}", bookId);
            throw new BusinessException(MeetingErrorCode.INVALID_BOOK);
        }
    }
}