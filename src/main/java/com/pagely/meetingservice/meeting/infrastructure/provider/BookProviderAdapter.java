package com.pagely.meetingservice.meeting.infrastructure.provider;

import com.pagely.common.exception.BusinessException;
import com.pagely.meetingservice.meeting.application.port.BookProvider;
import com.pagely.meetingservice.meeting.domain.exception.MeetingErrorCode;
import com.pagely.meetingservice.meeting.infrastructure.client.book.BookApiResponse;
import com.pagely.meetingservice.meeting.infrastructure.client.book.BookClient;
import feign.FeignException;
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
        if (bookId == null || bookId.isBlank()) {
            log.info("Book Service 검증 생략: bookId가 비어 있음");
            return;
        }

        log.info("Book Service 내부 API 호출 시작: bookId={}", bookId);

        BookApiResponse response;

        try {
            response = bookClient.getBook(bookId);
        } catch (FeignException.NotFound e) {
            log.warn("Book Service 도서 미존재: bookId={}", bookId);
            throw new BusinessException(MeetingErrorCode.INVALID_BOOK);
        } catch (FeignException e) {
            log.error("Book Service 호출 실패: bookId={}, status={}", bookId, e.status());
            throw new BusinessException(MeetingErrorCode.INVALID_BOOK);
        }

        if (response == null || !response.success() || response.data() == null) {
            log.warn("Book Service 도서 검증 실패: bookId={}", bookId);
            throw new BusinessException(MeetingErrorCode.INVALID_BOOK);
        }

        log.info("Book Service 도서 검증 완료: bookId={}, success={}",
                bookId,
                response.success());
    }
}